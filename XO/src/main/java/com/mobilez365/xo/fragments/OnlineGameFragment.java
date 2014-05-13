package com.mobilez365.xo.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;


import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.BaseAdapter;

import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.plus.Plus;
import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.activity.GameActivity;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.ai.GameChecker;
import com.mobilez365.xo.util.Constant;
import com.mobilez365.xo.util.GlobalHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.util.ArrayList;


/**
 * Created by BruSD on 06.05.2014.
 */
public class OnlineGameFragment extends Fragment {

    private FromActivityBroadcastReceiver mIntReceiver = new FromActivityBroadcastReceiver();
    private boolean isMyTurn;
    private AlertDialog.Builder dialog;
    private View rootView;
    private Activity parentActivity;
    private int mySign;
    private FieldValue [][] fieldValuesMatrix;
    private FieldValue [] fieldValuesArray;
    private GridView gridview;

    private String isGameContinueByMe, isGameContinueByOpponent =  new String();



    private TextView myUserNameTextView, oponentUserNameTextView, mySignTextView, oponentSignTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView, winLineImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_layout, container, false);
        parentActivity = getActivity();

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


    private void initialAllView() {
        gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment);
        oponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment);
        oponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment);

        myAvatarImageView = (ImageView)rootView.findViewById(R.id.my_avatar_image_view_game_fragment);
        oponentAvatarImageView = (ImageView)rootView.findViewById(R.id.oponent_avatar_image_view_game_fragment);

        winLineImageView = (ImageView)rootView.findViewById(R.id.win_line_image_view);
    }

    private void fillDataInView() {
        if (mySign == Constant.MY_SYMBOLE_X){
            mySignTextView.setText("X");
            oponentSignTextView.setText("O");
        }else {
            mySignTextView.setText("O");
            oponentSignTextView.setText("X");
        }
        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));
    }
    private void fillPlayersData()  {
        Room room = ((GameActivity) parentActivity).mXORoom;
        ArrayList<Participant> mParticipants =  room.getParticipants();

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



            }else {
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


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.FF_OPONENT_STROK);
        filter.addAction(Constant.FF_IS_GAME_CONTINUE_OPPONENT_OPINION);
        parentActivity.registerReceiver(mIntReceiver, filter);

    }


    @Override
    public void onPause() {
        if (mIntReceiver != null)
            parentActivity.unregisterReceiver(mIntReceiver);
        super.onPause();
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
                    String crossName = "cross" + "1" + "_img" ;
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
                    break;
                }
                case O:{
                    String zeroName = "zero" + "1" + "_img";
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                    break;
                }

            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isMyTurn && fieldValuesArray[position] == FieldValue.Empty ) {
                        if (mySymbole == Constant.MY_SYMBOLE_O) {

                            String zeroName = "zero" + "1" + "_img";
                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.O;
                            myTurnWinChecker(position);
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                        } else if (mySymbole == Constant.MY_SYMBOLE_X) {

                            String crossName = "cross" + "1" + "_img";
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

        Log.v("XO", oponentStrok);


    }

    private void winChecker() {
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
        }else {
            boolean isNoWay = true;
            for (int i = 0; i < fieldValuesArray.length; i ++){
                if (fieldValuesArray[i] == FieldValue.Empty) {
                    isNoWay = false;
                    break;
                }
            }
            if (isNoWay){
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
                putOponentStrok(oponentStrok);
            }else if(intent.getAction().equals(Constant.FF_IS_GAME_CONTINUE_OPPONENT_OPINION)){
                String oponentStrok = intent.getStringExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE);
                isGameContinueByOpponent = oponentStrok;
                checkOpponentOpinion();
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
        switch (winerStatus){
            case -1:{
                showDrawDialog();
                break;
            }
            default:{
                if (mySign == winerStatus){
                    showWinDialog();
                }else {
                    showLoseDialog();
                }
                break;
            }

        }
    }
    private void showWinDialog(){
        SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
        dialog = new  AlertDialog.Builder(parentActivity);
        dialog.setTitle("You Win");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Yes", new YesOnClickButtonListener());
        dialog.setNegativeButton("No", new NoOnClickButtonListener());
        dialog.setMessage("Continue?");
        dialog.create().show();

    }
    private void showLoseDialog(){
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        dialog = new AlertDialog.Builder(parentActivity);
        dialog.setTitle("You Lose");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Yes", new YesOnClickButtonListener());
        dialog.setNegativeButton("No", new NoOnClickButtonListener());
        dialog.setMessage("Continue?");
        dialog.create().show();
    }
    private void showDrawDialog(){
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        dialog = new AlertDialog.Builder(parentActivity);
        dialog.setTitle("Draw Game");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Yes", new YesOnClickButtonListener());
        dialog.setNegativeButton("No", new NoOnClickButtonListener());
        dialog.setMessage("Continue?");
        dialog.create().show();
    }

    private class YesOnClickButtonListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
            intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "yes" );
            parentActivity.sendBroadcast(intent);
            isGameContinueByMe = "yes";
            checkOpponentOpinion();
            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
            dialog.dismiss();
        }
    }



    private class NoOnClickButtonListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent =  new Intent(Constant.FILTER_IS_GAME_CONTINUE);
            intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, "no" );
            parentActivity.sendBroadcast(intent);
            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
            dialog.dismiss();
        }
    }


    private void initialGameField(){
        gridview.setAdapter(new XOImageAdapter(parentActivity, mySign, fieldValuesArray ));
    }

    private void checkOpponentOpinion() {
        if (isGameContinueByOpponent != null && isGameContinueByMe != null){
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
}
