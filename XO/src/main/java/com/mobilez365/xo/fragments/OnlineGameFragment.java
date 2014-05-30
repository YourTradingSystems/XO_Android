package com.mobilez365.xo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;


import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by BruSD on 06.05.2014.
 */
public class OnlineGameFragment extends Fragment {

    private FromActivityBroadcastReceiver mIntReceiver = new FromActivityBroadcastReceiver();
    private boolean isMyTurn;

    private View rootView;
    private Activity parentActivity;
    private int mySign;
    private FieldValue [][] fieldValuesMatrix;
    private FieldValue [] fieldValuesArray;
    private GridView gridview;

    private String isGameContinueByMe, isGameContinueByOpponent =  new String();

    private Timer gameTimer,  waitinTimer;
    private static final int timeToStrock = 30;
    private static final int timeToContinueGame = 10;
    private int timerCountToStrock;
    private int timerCountToContinueGame;
    private boolean isGameFinish;
    private TextView myUserNameTextView, oponentUserNameTextView, mySignTextView, oponentSignTextView, timerTextView;
    private TextView myScoreFirstCounterTextView, myScoreSecondCounterTextView, opponentScoreFirstCounterTextView, opponentScoreSecondCounterTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView, winLineImageView;
    private Button noContinueButton, yesContinueButton;
    private TextView infoYourTheyTornTextView, continueTextView;
    private int myWinsThisGame, oponentWinsThisGame;
    private Tracker traker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_layout, container, false);
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
        if (mIntReceiver != null)
            parentActivity.unregisterReceiver(mIntReceiver);
        super.onPause();
    }

    private void initialAllView() {
        isGameFinish = false;

        gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment);
        oponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment);
        oponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment);

        myAvatarImageView = (ImageView)rootView.findViewById(R.id.my_avatar_image_view_game_fragment);
        oponentAvatarImageView = (ImageView)rootView.findViewById(R.id.oponent_avatar_image_view_game_fragment);

        winLineImageView = (ImageView)rootView.findViewById(R.id.win_line_image_view);
        timerTextView = (TextView)rootView.findViewById(R.id.timer_text_view_game_fragment);

        // notification panel
        noContinueButton = (Button)rootView.findViewById(R.id.no_continue_button);
        yesContinueButton = (Button)rootView.findViewById(R.id.yes_continue_button);
        infoYourTheyTornTextView = (TextView) rootView.findViewById(R.id.info_your_they_torn_text_view);
        continueTextView = (TextView) rootView.findViewById(R.id.continue_text_view);

        //score counter
        myScoreFirstCounterTextView = (TextView)rootView.findViewById(R.id.first_count_my_score_text_view);
        myScoreSecondCounterTextView =  (TextView)rootView.findViewById(R.id.second_count_my_score_text_view);
        opponentScoreFirstCounterTextView = (TextView)rootView.findViewById(R.id.first_count_opponent_score_text_view);
        opponentScoreSecondCounterTextView = (TextView)rootView.findViewById(R.id.second_count_opponent_score_text_view);
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

        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));

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



    private void fillPlayersData()  {
        Room room = ((GameActivity) parentActivity).mXORoom;
        if (room != null) {
            ArrayList<Participant> mParticipants = room.getParticipants();

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
        }else {

        }
    }






    public class XOImageAdapter extends BaseAdapter {
        private Context mContext;
        private int mySymbole;
        private FieldValue[] fieldValuesG;

        public XOImageAdapter(Context c, int mySymbole, FieldValue[] fieldValues) {
            mContext = c;
            this.mySymbole = mySymbole;
            this.fieldValuesG = fieldValues;
        }

        public int getCount() {
            return fieldValuesG.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position,  View convertView, ViewGroup parent) {



            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.grid_view_cell_layout, parent, false);



            final ImageView imageView = (ImageView) convertView.findViewById(R.id.xo_cell_image_view);
            FieldValue id = fieldValuesG[position];
            switch (id){
                case Empty:{
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.zero_field));
                    break;
                }
                case X:{
                    String crossName = "cross" + 1 + "_img" ;
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
                    break;
                }
                case O:{

                    String zeroName = "zero" + 1 + "_img";
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                    break;
                }

            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isGameFinish)
                        return;
                    if(isMyTurn && fieldValuesArray[position] == FieldValue.Empty ) {
                        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));
                        if (mySymbole == Constant.MY_SYMBOLE_O) {

                            Random random = new Random();
                            int randomID = random.nextInt(3) + 1;

                            String zeroName = "zero" + 1 + "_img";

                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.O;
                            myTurnWinChecker(position);
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                        } else if (mySymbole == Constant.MY_SYMBOLE_X) {
                            Random random = new Random();
                            int randomID = random.nextInt(4) + 1;

                            String crossName = "cross" + 1 + "_img";
                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.X;
                            myTurnWinChecker(position);
                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);

                        }

                        v.setEnabled(false);
                    }
                }
            });


            return convertView;
        }

    }




    private void myTurnWinChecker(int position){
        winChecker();

        int xPosition =  position / 3;
        int yPosition = position - xPosition*3;
        String message = String.valueOf(xPosition)+ String.valueOf(yPosition);

        Intent intent =  new Intent(Constant.FILTER_SEND_MY_STROK);
        intent.putExtra(Constant.INTENT_KEY_MY_STROK, String.valueOf(message) );
        parentActivity.sendBroadcast(intent);

        isMyTurn = false;


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
        ((XOImageAdapter)gridview.getAdapter()).notifyDataSetChanged();
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

    private void writWinRedLine(Bundle bundle) {
        int startX = bundle.getInt(GameChecker.COORDINATE_START_X);
        int endX = bundle.getInt(GameChecker.COORDINATE_END_X);
        int startY = bundle.getInt(GameChecker.COORDINATE_START_Y);
        int endY = bundle.getInt(GameChecker.COORDINATE_END_Y);

        if (startY == endY){
            winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_vertical));
            DisplayMetrics displaymetrics = new DisplayMetrics();
            parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            int width = displaymetrics.widthPixels;
            switch (startY){
                case 0:{
                    winLineImageView.setPadding(0, winLineImageView.getPaddingTop(), winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
                    winLineImageView.requestLayout();
                    break;
                }
                case 1:{

                    winLineImageView.setPadding(width/3, winLineImageView.getPaddingTop(), winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
                    winLineImageView.requestLayout();
                    break;
                }
                case 2:{

                    winLineImageView.setPadding(width - width/3, winLineImageView.getPaddingTop(), winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
                    winLineImageView.requestLayout();
                    break;
                }
            }
        }else if (startX == endX){
            winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_horizontal));
            DisplayMetrics displaymetrics = new DisplayMetrics();
            parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            int width = displaymetrics.widthPixels;
            switch (startX){
                case 0:{

                    winLineImageView.setPadding(winLineImageView.getPaddingLeft(), 0, winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
                    winLineImageView.requestLayout();
                    break;
                }
                case 1:{

                    winLineImageView.setPadding(winLineImageView.getPaddingLeft(), width/3, winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
                    winLineImageView.requestLayout();
                    break;
                }
                case 2:{

                    winLineImageView.setPadding(winLineImageView.getPaddingLeft(), width - width/3, winLineImageView.getPaddingRight(), winLineImageView.getPaddingBottom());
                    winLineImageView.requestLayout();
                    break;
                }


            }
        }else {
            if(startX == 0 && startY == 0){
                winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_left_slash));
            }else {
                winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_right_slash));
            }
        }
    }


    private class FromActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Constant.FF_OPONENT_STROK)){
                String oponentStrok = intent.getStringExtra(Constant.INTENT_KEY_OPONENT_STROK);
                infoYourTheyTornTextView.setText(parentActivity.getString(R.string.your_torn_string));
                if (mySign == Constant.MY_SYMBOLE_X ){
                    SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                }else {
                    SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);
                }
                putOponentStrok(oponentStrok);
            }else if(intent.getAction().equals(Constant.FF_IS_GAME_CONTINUE_OPPONENT_OPINION)){
                String oponentStrok = intent.getStringExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE);
                isGameContinueByOpponent = oponentStrok;
                checkOpponentOpinion();
            }else if(intent.getAction().equals(Constant.FF_OPPONENT_LEFT_GAME)){
                myWinsThisGame++;

                AppStateManagerUtil.updateOlineProgressAppState();
                SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
                infoYourTheyTornTextView.setText(parentActivity.getString(R.string.win_string));

                continueNotificationShow();
                continueTextView.setText(parentActivity.getString(R.string.opponent_left_game_string));
                noContinueButton.setText(parentActivity.getString(R.string.back_string));
                yesContinueButton.setVisibility(View.INVISIBLE);
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

    private class YesOnClickButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
            intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "yes" );
            parentActivity.sendBroadcast(intent);
            isGameContinueByMe = "yes";
            waitingOpponentRespond();
            checkOpponentOpinion();

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
        gridview.setAdapter(new XOImageAdapter(parentActivity, mySign, fieldValuesArray ));
    }

    private void checkOpponentOpinion() {
        if (isGameContinueByOpponent != null && isGameContinueByMe != null){
            waitinTimer.cancel();
            if (isGameContinueByOpponent.equals("yes") && isGameContinueByMe.equals("yes")){

                initNewGame();
            } else if (isGameContinueByOpponent.equals("no")){
                Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
                intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "no" );
                parentActivity.sendBroadcast(intent);
            }

        }
    }
    private void initNewGame(){
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
        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));
        winLineImageView.setPadding(0,0,0,0);
        winLineImageView.requestLayout();
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
            }
            timerCountToContinueGame = timerCountToContinueGame - 1;
        }
    };
    // endregion
}
