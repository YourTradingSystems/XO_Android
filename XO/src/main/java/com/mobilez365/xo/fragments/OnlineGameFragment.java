package com.mobilez365.xo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;


import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.mobilez365.xo.GameServiceUtil.AchievementUnlokUtil;
import com.mobilez365.xo.GameServiceUtil.AppStateManagerUtil;
import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.XOApplication;
import com.mobilez365.xo.activity.GameActivity;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.ai.GameChecker;
import com.mobilez365.xo.util.AppSettings;
import com.mobilez365.xo.util.Constant;
import com.mobilez365.xo.util.GlobalHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by BruSD on 06.05.2014.
 */
public class OnlineGameFragment extends Fragment {

    private FromActivityBroadcastReceiver mIntReceiver = new FromActivityBroadcastReceiver();
    private boolean isMyTurn;
    private String mWasInvitedID;
    private String mInvitorID;
    private View rootView;
    private Activity parentActivity;
    private int mySign;
    private FieldValue [][] fieldValuesMatrix;
    private FieldValue [] fieldValuesArray;
    //private GridView gridview;
    private  CustomViewXOField viewXOField;
    private Bitmap winLineBitmap;
    private  int winLine=0; //(=1,2,3,4,5,6,7,8 or 0)
    private  boolean gameType; // true - game og trurh / fallse = quick game
    HashMap<String,Bitmap> id_and_img;
    String user_id;

    private String isGameContinueByMe, isGameContinueByOpponent =  new String();

    private Timer gameTimer,  waitinTimer;
    private static final int timeToStrock = 30;
    private static final int timeToContinueGame = 10;
    private int timerCountToStrock;
    private int timerCountToContinueGame;
    private boolean isGameFinish;
    private TextView myUserNameTextView, oponentUserNameTextView, mySignTextView, oponentSignTextView, timerTextView;
    private TextView myScoreFirstCounterTextView, myScoreSecondCounterTextView, opponentScoreFirstCounterTextView, opponentScoreSecondCounterTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView;//, winLineImageView;
    private Button noContinueButton, yesContinueButton;
    private TextView infoYourTheyTornTextView, continueTextView;
    private int myWinsThisGame, oponentWinsThisGame;
    private Tracker traker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_test_layout, container, false);
        parentActivity = getActivity();

        traker= ((XOApplication)parentActivity.getApplication()).getTracker(
                XOApplication.TrackerName.APP_TRACKER);

        AppStateManagerUtil.init(((GameActivity)parentActivity).getGameHelper().getApiClient(), parentActivity);

        timerCountToStrock = timeToStrock;
        myWinsThisGame = 0;
        oponentWinsThisGame = 0;
        SoundManager.initSound(parentActivity, Constant.LOSE_SOUND);
        SoundManager.initSound(parentActivity, Constant.WIN_SOUND);
        SoundManager.initSound(parentActivity, Constant.GOES_X__SOUND);
        SoundManager.initSound(parentActivity, Constant.GOES_O_SOUND);
        initialAllView();

        initFieldValues();

        Bundle bundle = this.getArguments();
        if(bundle.getBoolean(Constant.INTENT_KEY_IS_MY_TURN)){
            mySign = Constant.MY_SYMBOLE_X;
            isMyTurn = true;
        }else {
            mySign = Constant.MY_SYMBOLE_O;
            isMyTurn = false;
        }

        if(bundle.getString(Constant.INTENT_KEY_WAS_INVITED)!=null){
            mWasInvitedID = bundle.getString(Constant.INTENT_KEY_WAS_INVITED);
           // Toast.makeText(getActivity().getApplicationContext(),"mWasInvitedID="+mWasInvitedID,Toast.LENGTH_LONG).show();
        }
        if(bundle.getString(Constant.INTENT_KEY_INVITOR)!=null){
            mInvitorID = bundle.getString(Constant.INTENT_KEY_INVITOR);
           // Toast.makeText(getActivity().getApplicationContext(),"mInvitorID="+mInvitorID,Toast.LENGTH_LONG).show();
        }

        if(bundle.getBoolean(Constant.INTENT_KEY_IS_GAME_OF_TRUTH)) {
           gameType=true;
        } else {
           gameType=false;
        }
       // Toast.makeText(getActivity().getApplicationContext(),"game type ="+ gameType,Toast.LENGTH_LONG).show();
        //  bundle.clear();//taras

        fillDataInView();
        fillPlayersData();
        initialGameField();


        return rootView;
    }
    private void sendScreenView(String screen){
        // Set screen name.
        // Where path is a String representing the screen name.
        traker.setScreenName(screen);

        // Send a screen view.
        traker.send(new HitBuilders.AppViewBuilder().build());
    }
    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.FF_OPONENT_STROK);
        filter.addAction(Constant.FF_IS_GAME_CONTINUE_OPPONENT_OPINION);
        filter.addAction(Constant.FF_OPPONENT_LEFT_GAME);

        parentActivity.registerReceiver(mIntReceiver, filter);

    }



    @Override
    public void onPause() {
        meLeftGAme();


        if (mIntReceiver != null)
            parentActivity.unregisterReceiver(mIntReceiver);
        super.onPause();
    }

    private void meLeftGAme() {
        //when press power butt
        isGameContinueByMe="no";
        //  isGameContinueByOpponent="yes";
        oponentWinsThisGame++;
        fillScoreView();

        isMyTurn=true;
        isGameFinish=true;
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.lose_string));
        noContinueButton.setText(parentActivity.getString(R.string.back_string));
        noContinueButton.setOnClickListener(new NoOnClickButtonListener());
        noContinueButton.setVisibility(View.VISIBLE);
        yesContinueButton.setVisibility(View.INVISIBLE);
        if (waitinTimer != null){
            waitinTimer.cancel();
        }

        if(gameTimer != null){
            gameTimer.cancel();
        }
        // checkOpponentOpinion();
    }

    private void initialAllView() {
        isGameFinish = false;

       // gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);

        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.game_field_relative_layout_test);//taras
        viewXOField = new CustomViewXOField(rootView.getContext()); //taras
        relativeLayout.addView(viewXOField, 0);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment_test);
        oponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment_test);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment_test);
        oponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment_test);

        myAvatarImageView = (ImageView)rootView.findViewById(R.id.my_avatar_image_view_game_fragment_test);
        oponentAvatarImageView = (ImageView)rootView.findViewById(R.id.oponent_avatar_image_view_game_fragment_test);

       // winLineImageView = (ImageView)rootView.findViewById(R.id.win_line_image_view);
        timerTextView = (TextView)rootView.findViewById(R.id.timer_text_view_game_fragment_test);

        // notification panel
        noContinueButton = (Button)rootView.findViewById(R.id.no_continue_button_test);
        yesContinueButton = (Button)rootView.findViewById(R.id.yes_continue_button_test);
        infoYourTheyTornTextView = (TextView) rootView.findViewById(R.id.info_your_they_torn_text_view_test);
        continueTextView = (TextView) rootView.findViewById(R.id.continue_text_view_test);

        //score counter
        myScoreFirstCounterTextView = (TextView)rootView.findViewById(R.id.first_count_my_score_text_view_test);
        myScoreSecondCounterTextView =  (TextView)rootView.findViewById(R.id.second_count_my_score_text_view_test);
        opponentScoreFirstCounterTextView = (TextView)rootView.findViewById(R.id.first_count_opponent_score_text_view_test);
        opponentScoreSecondCounterTextView = (TextView)rootView.findViewById(R.id.second_count_opponent_score_text_view_test);
    }

    private void fillScoreView(){
        if (myWinsThisGame < 10){
            myScoreFirstCounterTextView.setText(String.valueOf(myWinsThisGame));
        }else {
            String second   = String.valueOf(myWinsThisGame).substring(0 ,1);
            String ferst = String.valueOf(myWinsThisGame).substring(1);
            myScoreFirstCounterTextView.setText(ferst);
            myScoreSecondCounterTextView.setText(second);
        }

        if (oponentWinsThisGame < 10){
            opponentScoreFirstCounterTextView.setText(String.valueOf(oponentWinsThisGame));
        }else {
            String second   = String.valueOf(oponentWinsThisGame).substring(0 ,1);
            String ferst = String.valueOf(oponentWinsThisGame).substring(1);
            opponentScoreFirstCounterTextView.setText(ferst);
            opponentScoreSecondCounterTextView.setText(second);
        }
    }

    private void fillDataInView() {
        timerCountToStrock = timeToStrock;
        gameTimer = new Timer();


        if (mySign == Constant.MY_SYMBOLE_X){
            mySignTextView.setText("X");
            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.your_torn_string));
            oponentSignTextView.setText("O");
        }else {
            mySignTextView.setText("O");
            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));
            oponentSignTextView.setText("X");
        }

//        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));
      //  winLineBitmap=null;//taras

        noContinueButton.setVisibility(View.INVISIBLE);
        yesContinueButton.setVisibility(View.INVISIBLE);
        continueTextView.setVisibility(View.INVISIBLE);

        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerGameMethod();
            }
        }, 0, 1000);
    }


    /*private void setAvatar (String url, final ImageView imgView){

        String photoLinkGPlus = url;

        imgView.refreshDrawableState();

        Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imgView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                imgView.refreshDrawableState();
            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {
                Log.d("TAG", "FAILED");
            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                Log.d("TAG", "Prepare Load");
            }
        });
    }*/

    private void fillPlayersData()  {
        Room room = ((GameActivity) parentActivity).mXORoom;

        if (room != null) {
            ArrayList<Participant> mParticipants = room.getParticipants();

                // start taras
        if(!gameType) {
            for (Participant p : mParticipants) {
                if (!p.getParticipantId().equals(room.getCreatorId())) {

                    oponentUserNameTextView.setText(p.getDisplayName());

                    String photoLinkGPlus = p.getIconImageUrl();

                    Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            oponentAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                        }

                        @Override
                        public void onBitmapFailed(final Drawable errorDrawable) {
                            Log.d("TAG", "FAILED");
                        }

                        @Override
                        public void onPrepareLoad(final Drawable placeHolderDrawable) {
                            Log.d("TAG", "Prepare Load");
                        }
                    });


                } else {

                    myUserNameTextView.setText(p.getDisplayName());

                    String photoLinkGPlus = p.getIconImageUrl();

                    Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            myAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                        }

                        @Override
                        public void onBitmapFailed(final Drawable errorDrawable) {
                            Log.d("TAG", "FAILED");
                        }

                        @Override
                        public void onPrepareLoad(final Drawable placeHolderDrawable) {
                            Log.d("TAG", "Prepare Load");
                        }
                    });
                }

            }
        }
            if(gameType) {

                for (Participant p : mParticipants) {
                    if (p.getParticipantId().equals(mWasInvitedID) && !p.getParticipantId().equals(room.getCreatorId())) {
                        Participant p_temp = p;

                        for (Participant p1 : mParticipants) {
                            if (!p_temp.getParticipantId().equals(p1.getParticipantId())) {
                                oponentUserNameTextView.setText(p1.getDisplayName());

                               String photoLinkGPlus = p1.getIconImageUrl();

                                Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        oponentAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
    //                            oponentAvatarImageView.setImageBitmap(id_and_img.get(p1.getParticipantId().toString()));
                                        oponentUserNameTextView.requestLayout();
                                        oponentAvatarImageView.requestLayout();
                                    }

                                    @Override
                                    public void onBitmapFailed(final Drawable errorDrawable) {
                                        Log.d("TAG", "FAILED");
                                    }

                                    @Override
                                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                        Log.d("TAG", "Prepare Load");
                                    }
                                });

                                myUserNameTextView.setText(p.getDisplayName());

                                String photoLinkGPlus1 = p.getIconImageUrl();

                                Picasso.with(parentActivity).load(photoLinkGPlus1).into(new Target() {

                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        myAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                                   //   myAvatarImageView.setImageBitmap(id_and_img.get(p.getParticipantId().toString()));
                                        myAvatarImageView.requestLayout();
                                        myUserNameTextView.requestLayout();
                                    }

                                    @Override
                                    public void onBitmapFailed(final Drawable errorDrawable) {
                                        Log.d("TAG", "FAILED");
                                    }

                                    @Override
                                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                        Log.d("TAG", "Prepare Load");
                                    }
                                });
                            }
                        }
                       /* myUserNameTextView.setText(p.getDisplayName());
                       // myAvatarImageView.setImageDrawable(oponentAvatarImageView.getDrawable());

                        //setAvatar(p.getIconImageUrl(),myAvatarImageView );

                        String photoLinkGPlus = p.getIconImageUrl();

                        Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                myAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                            }

                            @Override
                            public void onBitmapFailed(final Drawable errorDrawable) {
                                Log.d("TAG", "FAILED");
                            }

                            @Override
                            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                Log.d("TAG", "Prepare Load");
                            }
                        });*/
                    }

                }
            }
                //end

               /*
                for (Participant p : mParticipants) {
                if (!p.getParticipantId().equals(room.getCreatorId())) {

                    oponentUserNameTextView.setText(p.getDisplayName());

                    String photoLinkGPlus = p.getIconImageUrl();

                    Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            oponentAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                        }

                        @Override
                        public void onBitmapFailed(final Drawable errorDrawable) {
                            Log.d("TAG", "FAILED");
                        }

                        @Override
                        public void onPrepareLoad(final Drawable placeHolderDrawable) {
                            Log.d("TAG", "Prepare Load");
                        }
                    });

                } else {

                    myUserNameTextView.setText(p.getDisplayName());

                    String photoLinkGPlus = p.getIconImageUrl();

                    Picasso.with(parentActivity).load(photoLinkGPlus).into(new Target() {

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            myAvatarImageView.setImageBitmap(GlobalHelper.getRoundedShape(bitmap));
                        }

                        @Override
                        public void onBitmapFailed(final Drawable errorDrawable) {
                            Log.d("TAG", "FAILED");
                        }

                        @Override
                        public void onPrepareLoad(final Drawable placeHolderDrawable) {
                            Log.d("TAG", "Prepare Load");
                        }
                    });
                }

            }*/

        }else {
         //   Toast.makeText(getActivity().getApplicationContext(),"Room is null",Toast.LENGTH_LONG).show();
        }
    }




//taras
//    public class XOImageAdapter extends BaseAdapter {
//        private Context mContext;
//        private int mySymbole;
//        private FieldValue[] fieldValuesG;
//
//        public XOImageAdapter(Context c, int mySymbole, FieldValue[] fieldValues) {
//            mContext = c;
//            this.mySymbole = mySymbole;
//            this.fieldValuesG = fieldValues;
//        }
//
//        public int getCount() {
//            return fieldValuesG.length;
//        }
//
//        public Object getItem(int position) {
//            return null;
//        }
//
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        // create a new ImageView for each item referenced by the Adapter
//        public View getView(final int position,  View convertView, ViewGroup parent) {
//
//
//
//            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
//            convertView = inflater.inflate(R.layout.grid_view_cell_layout, parent, false);
//
//
//
//            final ImageView imageView = (ImageView) convertView.findViewById(R.id.xo_cell_image_view);
//            FieldValue id = fieldValuesG[position];
//            switch (id){
//                case Empty:{
//                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.zero_field));
//                    break;
//                }
//                case X:{
//                    String crossName = "cross" + 1 + "_img" ;
//                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
//                    break;
//                }
//                case O:{
//
//                    String zeroName = "zero" + 1 + "_img";
//                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
//                    break;
//                }
//
//            }
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isGameFinish)
//                        return;
//                    if(isMyTurn && fieldValuesArray[position] == FieldValue.Empty ) {
//                        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));
//                        if (mySymbole == Constant.MY_SYMBOLE_O) {
//
//                            Random random = new Random();
//                            int randomID = random.nextInt(3) + 1;
//
//                            String zeroName = "zero" + 1 + "_img";
//
//                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
//                            fieldValuesArray[position] = FieldValue.O;
//                            myTurnWinChecker(position);
//                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
//                        } else if (mySymbole == Constant.MY_SYMBOLE_X) {
//                            Random random = new Random();
//                            int randomID = random.nextInt(4) + 1;
//
//                            String crossName = "cross" + 1 + "_img";
//                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
//                            fieldValuesArray[position] = FieldValue.X;
//                            myTurnWinChecker(position);
//                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);
//
//                        }
//
//                        v.setEnabled(false);
//                    }
//                }
//            });
//
//
//            return convertView;
//        }
//
//    }




    private void myTurnWinChecker(int position){
        winChecker();

        int xPosition =  position / 3;
        int yPosition = position - xPosition*3;
        String message = String.valueOf(xPosition)+ String.valueOf(yPosition);

        Intent intent =  new Intent(Constant.FILTER_SEND_MY_STROK);
        intent.putExtra(Constant.INTENT_KEY_MY_STROK, String.valueOf(message) );
        parentActivity.sendBroadcast(intent);

        isMyTurn = false;
       // viewXOField.invalidate();//taras

    }



    public void putOponentStrok(String oponentStrok) {

        int xPosition  = Integer.valueOf(oponentStrok.substring(0 ,1));
        int yPosition = Integer.valueOf(oponentStrok.substring(1));
        int arrayIndex = xPosition*3 + yPosition;

        if(mySign == Constant.MY_SYMBOLE_O) {
            fieldValuesArray[arrayIndex] = FieldValue.X;
        }else {
            fieldValuesArray[arrayIndex] = FieldValue.O;
        }


        //((XOImageAdapter)gridview.getAdapter()).notifyDataSetChanged();//taras not
       // viewXOField.invalidate();//taras

        isMyTurn = true;
        winChecker();

    }

    private void winChecker() {
        timerCountToStrock = timeToStrock;
        fieldArrayToMarix();
        Bundle bundle = GameChecker.chechForWinCombination(fieldValuesMatrix, 3);
        if (bundle != null){
            String winerSymbole = bundle.getString(GameChecker.WIN_SYMBOL);

            writWinRedLine(bundle);

            if(winerSymbole.equals("X")){
                showEndGameDialog(Constant.MY_SYMBOLE_X);
            }else if (winerSymbole.equals("O")){
                showEndGameDialog(Constant.MY_SYMBOLE_O);
            }
            gameTimer.cancel();
        }else {
            boolean isNoWay = true;
            for (int i = 0; i < fieldValuesArray.length; i ++){
                if (fieldValuesArray[i] == FieldValue.Empty) {
                    isNoWay = false;
                    break;
                }
            }
            if (isNoWay){
                gameTimer.cancel();
                showEndGameDialog(-1);
            }
        }
    }

//    private void writWinRedLine(Bundle bundle) {
//        int startX = bundle.getInt(GameChecker.COORDINATE_START_X);
//        int endX = bundle.getInt(GameChecker.COORDINATE_END_X);
//        int startY = bundle.getInt(GameChecker.COORDINATE_START_Y);
//        int endY = bundle.getInt(GameChecker.COORDINATE_END_Y);
//
//        if (startY == endY){
//            winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_vertical));
//            DisplayMetrics displaymetrics = new DisplayMetrics();
//            parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//
//            int width = displaymetrics.widthPixels;
//            switch (startY){
//                case 0:{
//                    winLineImageView.setPadding(0, winLineImageView.getPaddingTop(), winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
//                    winLineImageView.requestLayout();
//                    break;
//                }
//                case 1:{
//
//                    winLineImageView.setPadding(width/3, winLineImageView.getPaddingTop(), winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
//                    winLineImageView.requestLayout();
//                    break;
//                }
//                case 2:{
//
//                    winLineImageView.setPadding(width - width/3, winLineImageView.getPaddingTop(), winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
//                    winLineImageView.requestLayout();
//                    break;
//                }
//            }
//        }else if (startX == endX){
//            winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_horizontal));
//            DisplayMetrics displaymetrics = new DisplayMetrics();
//            parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//
//            int width = displaymetrics.widthPixels;
//            switch (startX){
//                case 0:{
//
//                    winLineImageView.setPadding(winLineImageView.getPaddingLeft(), 0, winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
//                    winLineImageView.requestLayout();
//                    break;
//                }
//                case 1:{
//
//                    winLineImageView.setPadding(winLineImageView.getPaddingLeft(), width/3, winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
//                    winLineImageView.requestLayout();
//                    break;
//                }
//                case 2:{
//
//                    winLineImageView.setPadding(winLineImageView.getPaddingLeft(), width - width/3, winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
//                    winLineImageView.requestLayout();
//                    break;
//                }
//
//
//            }
//        }else {
//            if(startX == 0 && startY == 0){
//                winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_left_slash));
//            }else {
//                winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_right_slash));
//            }
//        }
//    }
//taras
private void writWinRedLine(Bundle bundle) {

    int startX = bundle.getInt(GameChecker.COORDINATE_START_X);
    int endX = bundle.getInt(GameChecker.COORDINATE_END_X);
    int startY = bundle.getInt(GameChecker.COORDINATE_START_Y);
    int endY = bundle.getInt(GameChecker.COORDINATE_END_Y);

    Log.d("WinLine", " StartX=" + startX + " EndX=" + endX + " StartY=" + startY + " EndY=" + endY);
    // winLineBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.zero_field);
    //   winLineBitmap = null;

    winLine=0;
    winLineBitmap = null;

    if (startY == endY) {
        winLineBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.line_horizontal);

        switch (startY) {
            case 0: {
                winLine=1;
                break;
            }
            case 1: {
                winLine=2;
                break;
            }
            case 2: {
                winLine=3;
                break;
            }
        }
    }

    if (startX == endX) {
        winLineBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.line_vertical);

        switch (startX) {
            case 0: {
                winLine=4;
                break;
            }
            case 1: {
                winLine=5;
                break;
            }
            case 2: {
                winLine=6;
                break;
            }

        }
    }
    if  (startX == 0 && startY == 0 && endX==2 && endY==2){
        winLineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.line_left_slash);
        winLine=7;
    }

    if (startX == 0 && endX == 2 && endY == 0 && startY ==2){
        winLineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.line_right_slash);
        winLine=8;
    }

     /*   if (isDrawGame) {
            winLine=0;
            winLineBitmap = null;
            isDrawGame=!isDrawGame;//false;//
        }*/

}

    private class FromActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Constant.FF_OPONENT_STROK)){
                String oponentStrok = intent.getStringExtra(Constant.INTENT_KEY_OPONENT_STROK);
                infoYourTheyTornTextView.setText(parentActivity.getString(R.string.your_torn_string));
                if (mySign == Constant.MY_SYMBOLE_X ){
                    SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                    viewXOField.invalidate();//taras
                }else {
                    SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);
                    viewXOField.invalidate();//taras
                }
                putOponentStrok(oponentStrok);
            }else if(intent.getAction().equals(Constant.FF_IS_GAME_CONTINUE_OPPONENT_OPINION)){
                String oponentStrok = intent.getStringExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE);
                isGameContinueByOpponent = oponentStrok;
                checkOpponentOpinion();
            }else if(intent.getAction().equals(Constant.FF_OPPONENT_LEFT_GAME)){
              //  Toast.makeText(rootView.getContext(), R.string.opponent_left_game_string,Toast.LENGTH_LONG).show();
                myWinsThisGame++;
                isGameFinish=true;
                fillScoreView();

                AppStateManagerUtil.updateOlineProgressAppState();
                SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
                infoYourTheyTornTextView.setText(parentActivity.getString(R.string.win_string));

                continueNotificationShow();
                continueTextView.setText(parentActivity.getString(R.string.opponent_left_game_string));
                noContinueButton.setText(parentActivity.getString(R.string.back_string));
                yesContinueButton.setVisibility(View.INVISIBLE);
                if (waitinTimer != null){
                    waitinTimer.cancel();
                }

                if(gameTimer != null){
                    gameTimer.cancel();
                }
            }
        }
    }


    private void initFieldValues(){
        fieldValuesMatrix = new FieldValue[3][3];
        fieldValuesArray = new FieldValue[9];
        for (int i =0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                fieldValuesMatrix[i][j] = FieldValue.Empty;
                fieldValuesArray[i*3+j] = FieldValue.Empty;
            }
        }
    }
    private void fieldArrayToMarix(){
        for (int i =0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                fieldValuesMatrix[i][j] = fieldValuesArray[i*3+j];

            }
        }
    }

    private void showEndGameDialog (int winerStatus ){
        isGameFinish = true;
        timerCountToContinueGame = timeToContinueGame;
        waitinTimer = new Timer();
        waitinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerWaitingMethod();
            }
        }, 0, 1000);

        switch (winerStatus){
            case -1:{
                showDrawMessage();
                break;
            }
            default:{
                if (mySign == winerStatus){
                    showWinMessage();
                }else {
                    showLoseMessage();
                }
                break;
            }
        }
        if (AppSettings.isFerstOnlineGame(parentActivity)){
            AchievementUnlokUtil.init(((GameActivity)parentActivity).getGameHelper().getApiClient(), parentActivity );
            AchievementUnlokUtil.unlockBeginer();
            AppSettings.setFerstOnlineGame(parentActivity);
        }
        fillScoreView();
    }



    private void showWinMessage(){
        myWinsThisGame++;
        AppStateManagerUtil.updateOlineProgressAppState();
        SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.win_string));

        continueNotificationShow();
    }

    private void showLoseMessage(){
        oponentWinsThisGame++;
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.lose_string));

        continueNotificationShow();
    }

    private void showDrawMessage(){
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);

        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.draw_game_string));
        continueNotificationShow();
    }

    private void continueNotificationShow(){
        noContinueButton.setOnClickListener(new NoOnClickButtonListener());
        noContinueButton.setVisibility(View.VISIBLE);

        yesContinueButton.setOnClickListener(new YesOnClickButtonListener());
        yesContinueButton.setVisibility(View.VISIBLE);

        continueTextView.setText(parentActivity.getString(R.string.continue_string));
        continueTextView.setVisibility(View.VISIBLE);
    }

        //taras
    private void continueNotificationShowForleftUser(){

//        continueTextView.setText(parentActivity.getString(R.string.opponent_left_game_string));
//        noContinueButton.setText(parentActivity.getString(R.string.back_string));
//        yesContinueButton.setVisibility(View.INVISIBLE);
        noContinueButton.setOnClickListener(new NoOnClickButtonListener());
        noContinueButton.setVisibility(View.VISIBLE);

        yesContinueButton.setOnClickListener(new YesOnClickButtonListener());
        yesContinueButton.setVisibility(View.VISIBLE);

        continueTextView.setText(parentActivity.getString(R.string.continue_string));
        continueTextView.setVisibility(View.VISIBLE);
    }

    private class YesOnClickButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            viewXOField.invalidate();//taras
            Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
            intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "yes" );
            parentActivity.sendBroadcast(intent);
            isGameContinueByMe = "yes";
            waitingOpponentRespond();
            checkOpponentOpinion();
            winLineBitmap=null; //taras


            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
        }
    }
    private void waitingOpponentRespond(){
        noContinueButton.setVisibility(View.INVISIBLE);
        yesContinueButton.setVisibility(View.INVISIBLE);
        continueTextView.setVisibility(View.INVISIBLE);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.waiting_opponent_string));
    }


    private void cancelGame(){

        Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
        intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "no" );
        parentActivity.sendBroadcast(intent);
//        if(!isMyTurn ) {
//            Toast.makeText(parentActivity.getApplicationContext(), R.string.opponent_left_game_string, Toast.LENGTH_LONG).show();
//        }
    }

    private class NoOnClickButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            cancelGame();
            if (waitinTimer != null){
                waitinTimer.cancel();
            }

            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
        }
    }

    private void initialGameField(){
        sendScreenView(Constant.SCREEN_GAME);
      //  gridview.setAdapter(new XOImageAdapter(parentActivity, mySign, fieldValuesArray ));//taras
    }

    private void checkOpponentOpinion() {
        if (isGameContinueByOpponent != null && isGameContinueByMe != null){

            if (waitinTimer != null) {
                waitinTimer.cancel();
            }
            if (isGameContinueByOpponent.equals("yes") && isGameContinueByMe.equals("yes")){

                initNewGame();
            } else if (isGameContinueByOpponent.equals("no")){
                Toast.makeText(parentActivity.getApplicationContext(), R.string.opponent_left_game_string,Toast.LENGTH_LONG).show();
                Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
                intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "no" );
                parentActivity.sendBroadcast(intent);
            }

        }
    }
    private void initNewGame(){
        isGameFinish = false;
        initMySign();
        fillDataInView();
        initFieldValues();
        initialGameField();

    }
    private void initMySign() {
        isGameContinueByOpponent = null;
        isGameContinueByMe = null;

        if (mySign == Constant.MY_SYMBOLE_X){
            mySign = Constant.MY_SYMBOLE_O;
            isMyTurn = false;
        }else {
            mySign = Constant.MY_SYMBOLE_X;
            isMyTurn = true;
        }
//        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));
//        winLineImageView.setPadding(0,0,0,0);
//        winLineImageView.requestLayout();
    }

    //region Timers
    private void TimerGameMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        parentActivity.runOnUiThread(Timer_Tick);
    }
    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            if (timerCountToStrock != 0){
                if (timerCountToStrock <10){
                    timerTextView.setTextColor(parentActivity.getResources().getColor(android.R.color.holo_red_dark));
                }else{
                    timerTextView.setTextColor(parentActivity.getResources().getColor(android.R.color.black));
                }
                timerTextView.setText(String.valueOf(timerCountToStrock));
            }else {
                timerTextView.setText(String.valueOf(timerCountToStrock));
                gameTimer.cancel();
                endGameByTimer();
                isGameFinish=true;
                if(!isMyTurn ) {
                    Toast.makeText(parentActivity.getApplicationContext(), R.string.opponent_left_game_string, Toast.LENGTH_LONG).show();
                }
                cancelGame();


            }
            timerCountToStrock = timerCountToStrock - 1;
        }
    };

    private void endGameByTimer() {
        if (isMyTurn){
            showLoseMessage();
        }else {
            showWinMessage();
        }

    }

    private void TimerWaitingMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        parentActivity.runOnUiThread(Timer_Tick_Waiting);
    }
    private Runnable Timer_Tick_Waiting = new Runnable() {
        public void run() {
            if (timerCountToContinueGame != 0){
                if (timerCountToContinueGame <10){
                    timerTextView.setTextColor(parentActivity.getResources().getColor(android.R.color.holo_red_dark));
                }else{
                    timerTextView.setTextColor(parentActivity.getResources().getColor(android.R.color.black));
                }
                timerTextView.setText(String.valueOf(timerCountToContinueGame));
            }else {
                timerTextView.setText(String.valueOf(timerCountToContinueGame));
                waitinTimer.cancel();
                cancelGame();




             //   winLineBitmap=null;//////taras
            }
            timerCountToContinueGame = timerCountToContinueGame - 1;
        }
    };
    // endregion



    //taras
    private class CustomViewXOField extends View {

        private boolean clicked = false;
        private float x_touch;
        private float y_touch;

        private int[][] mResourceMatrixForX;
        private int[][] mResourceMatrixForO;

        public CustomViewXOField(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public CustomViewXOField(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public CustomViewXOField(Context context) {
            super(context);
            init();
        }

        public void init() {

            super.setBackgroundResource(R.drawable.field_bg);

            setRandomResource(3,3);

            super.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(!isGameFinish&&isMyTurn){

                        if (mySign == Constant.MY_SYMBOLE_X){
                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);
                        }

                        if (mySign == Constant.MY_SYMBOLE_O){
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                        }

                        x_touch = event.getX();
                        y_touch = event.getY();

                        clicked = !clicked;

                        invalidate();
                    }


                    return false;
                }
            });

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            super.onMeasure(widthMeasureSpec, widthMeasureSpec);

        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.save();

            drawOnField(canvas, x_touch, y_touch);

            canvas.restore();

            super.onDraw(canvas);
        }

        private void drawOnField(Canvas c, float x, float y) {

            float my_padging_h =(float)(getHeight()*0.068);
            float my_padging_w =(float)(getWidth()*0.068);

            float h = getHeight()-my_padging_h;
            float w = getWidth()-my_padging_w;

            if (clicked && !isGameFinish) {

                if (fieldValuesMatrix[xToField(x)][yToField(y)] == FieldValue.Empty && mySign == Constant.MY_SYMBOLE_X) {

                    fieldValuesArray[xToField(x)*3+yToField(y)]=FieldValue.X;
                    fieldArrayToMarix();

                    drawXorO(c, my_padging_h, my_padging_w, h, w);

                      infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));

                    myTurnWinChecker(xToField(x)*3+yToField(y));
                    invalidate();

                }

                if (fieldValuesMatrix[xToField(x)][yToField(y)] == FieldValue.Empty && mySign == Constant.MY_SYMBOLE_O) {

                    fieldValuesArray[xToField(x)*3+yToField(y)]=FieldValue.O;
                    fieldArrayToMarix();

                    drawXorO(c, my_padging_h, my_padging_w, h, w);

                        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));

                    myTurnWinChecker(xToField(x)*3+yToField(y));
                    invalidate();

                }

                clicked = !clicked;

            }

            drawXorO(c, my_padging_h, my_padging_w, h, w);

            if(isGameFinish && winLineBitmap!=null) {
                Rect rectLine;
                Paint p = new Paint();

                switch (winLine) {
                    case 0: {
                        invalidate();
                        break;
                    }
                    case 1: {
                        rectLine  = new Rect((int)(my_padging_w/2),(int)(my_padging_h/2),(int) w ,(int)(h/3));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 2: {
                        rectLine  = new Rect((int)(my_padging_w/2),(int)(my_padging_h/2+h/3),(int) w ,(int)(h/3+h/3));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 3: {
                        rectLine  = new Rect((int)(my_padging_w/2),(int)(my_padging_h/2+h/3+h/3),(int) w ,(int)(h));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 4: {
                        rectLine  = new Rect((int)(my_padging_w/2),(int)(my_padging_h/2),(int) (w/3) ,(int)(h));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 5: {
                        rectLine  = new Rect((int)(my_padging_w/2+w/3),(int)(my_padging_h/2),(int) (w/3+w/3) ,(int)(h));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 6: {
                        rectLine  = new Rect((int)(my_padging_w/2+w/3+w/3),(int)(my_padging_h/2),(int) (w) ,(int)(h));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 7: {
                        rectLine  = new Rect((int)(my_padging_w/2),(int)(my_padging_h/2),(int) (w) ,(int)(h));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                    case 8: {
                        rectLine  = new Rect((int)(my_padging_w/2),(int)(my_padging_h/2),(int) (w) ,(int)(h));
                        c.drawBitmap(winLineBitmap,null ,rectLine ,p );
                        break;
                    }
                }
                invalidate();
            }

            if(isGameFinish){
                invalidate();
            }
        }

        private void drawXorO(Canvas c, float my_padging_h, float my_padging_w, float h, float w) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (fieldValuesMatrix[i][j] == FieldValue.X) {

                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), mResourceMatrixForX[i][j]);// R.drawable.cross1_img);
                        Paint p = new Paint();

                        Rect rect = new Rect((int)(i*w/3 +my_padging_w),(int)(j*h/3+my_padging_h),(int)((i*w/3)+w/3),(int)((j*h/3)+h/3));

                        c.drawBitmap(bmp,null ,rect ,p );
                    }

                    if (fieldValuesMatrix[i][j] == FieldValue.O) {

                        Bitmap bmp = BitmapFactory.decodeResource(getResources(),mResourceMatrixForO[i][j]);
                        Paint p = new Paint();

                        Rect rect = new Rect((int)(i*w/3+my_padging_w),(int)(j*h/3+my_padging_h),(int)((i*w/3)+w/3),(int)((j*h/3)+h/3));

                        c.drawBitmap(bmp,null ,rect ,p );
                    }
                }
            }
        }

        private int xToField(float x) {

            int f;

            float w = getWidth();

            if (x < w / 3) {
                f = 0;
                return f;
            } else {
                if (x < (w / 3) * 2) {
                    f = 1;
                    return f;
                } else {
                    f = 2;
                    return f;
                }
            }

        }

        private int yToField(float y) {

            int f;

            float w = getWidth();

            if (y < w / 3) {
                f = 0;
                return f;
            } else {
                if (y < (w / 3) * 2) {
                    f = 1;
                    return f;
                } else {
                    f = 2;
                    return f;
                }
            }

        }

        private void setRandomResource(int x, int y) {

            mResourceMatrixForX = new int[x][y];
            mResourceMatrixForO = new int[x][y];

            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {

                    String crossName = "cross" + getRandomExcludeZero(5) + "_img" ;
                    String zeroName = "zero" + getRandomExcludeZero(5) + "_img" ;

                    mResourceMatrixForX[i][j]=getResources().getIdentifier(crossName, "drawable",parentActivity.getPackageName());
                    mResourceMatrixForO[i][j]=getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName());

                }
            }
        }

        private int getRandomExcludeZero(int size) {

            int x;

            Random r = new Random();

            do { x=r.nextInt(size);
                // if(x!=0) return x;
            } while (x==0);
            //  Log.d("MYRAND", " x="+x);
            return x;
        }

    }

}
