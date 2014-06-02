package com.mobilez365.xo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.Tracker;
import com.mobilez365.xo.GameServiceUtil.AchievementUnlokUtil;
import com.mobilez365.xo.GameServiceUtil.AppStateManagerUtil;
import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.XOApplication;
import com.mobilez365.xo.activity.GameActivity;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.ai.GameChecker;
import com.mobilez365.xo.util.Constant;

import java.util.Random;

/**
 * Created by BruSD on 06.05.2014.
 */
public class TwoPlayerFragment extends Fragment{


    private View rootView;
    private Activity parentActivity;

    private FieldValue[][] fieldValuesMatrix;
    private FieldValue [] fieldValuesArray;
    private GridView gridview;
    private TextView myUserNameTextView, oponentUserNameTextView, mySignTextView, oponentSignTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView, winLineImageView;
    private TextView myScoreFirstCounterTextView, myScoreSecondCounterTextView, opponentScoreFirstCounterTextView, opponentScoreSecondCounterTextView;
    private TextView infoYourTheyTornTextView, continueTextView;
    private Button noContinueButton, yesContinueButton;
    private int userOne, userTwo;
    private int winsUserOne, winsUserTwo;
    private Tracker traker;
    private boolean isUserOneTurn;
    private boolean isGameFinish;
    private String winnerInThisRound;
    private InterstitialAd interstitial;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_layout, container, false);
        parentActivity = getActivity();
        initAds();
        initGame();
        initialAllView();

        initFieldValues();

        fillDataInView();
        fillPlayersData();
        initialGameField();
        return rootView;
    }
    private void initAds(){
        interstitial = new InterstitialAd(parentActivity);
        interstitial.setAdUnitId(Constant.MY_AD_UNIT_ID);
        AdRequest adRequest = new AdRequest.Builder().build();

        // Запуск загрузки межстраничного объявления.
        interstitial.loadAd(adRequest);
    }
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        displayInterstitial();
    }

    private void initGame() {
        traker= ((XOApplication)parentActivity.getApplication()).getTracker(
                XOApplication.TrackerName.APP_TRACKER);

        AppStateManagerUtil.init(((GameActivity) parentActivity).getGameHelper().getApiClient(), parentActivity);


        winnerInThisRound = "";
        SoundManager.initSound(parentActivity, Constant.LOSE_SOUND);
        SoundManager.initSound(parentActivity, Constant.WIN_SOUND);
        SoundManager.initSound(parentActivity, Constant.GOES_X__SOUND);
        SoundManager.initSound(parentActivity, Constant.GOES_O_SOUND);

        userOne = Constant.MY_SYMBOLE_O;


        winsUserOne = 0;
        winsUserTwo = 0;

        isUserOneTurn = true;
        isGameFinish = false;

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


    private void fillDataInView() {

        if (userOne == Constant.MY_SYMBOLE_O){
            userOne = Constant.MY_SYMBOLE_X;
            userTwo = Constant.MY_SYMBOLE_O;
            isUserOneTurn = true;
            mySignTextView.setText("X");
            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_one_string));
            oponentSignTextView.setText("O");

        }else {
            userOne = Constant.MY_SYMBOLE_O;
            userTwo = Constant.MY_SYMBOLE_X;
            isUserOneTurn = false;
            mySignTextView.setText("O");
            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_two_string));
            oponentSignTextView.setText("X");
        }
        isGameFinish = false;

        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));
        noContinueButton.setVisibility(View.INVISIBLE);
        yesContinueButton.setVisibility(View.INVISIBLE);
        continueTextView.setVisibility(View.INVISIBLE);

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

    private void initialGameField(){
        gridview.setAdapter(new XOImageAdapter(parentActivity, fieldValuesArray ));

    }
    public class XOImageAdapter extends BaseAdapter {
        private Context mContext;
        private FieldValue[] fieldValuesG;

        public XOImageAdapter(Context c,  FieldValue[] fieldValues) {
            mContext = c;

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
                    String crossName = "cross" + 1 + "_img";
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

                    if (isUserOneTurn && fieldValuesArray[position] == FieldValue.Empty){
                        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_two_string));
                        if (userOne == Constant.MY_SYMBOLE_O) {

                            Random random = new Random();
                            int randomID = random.nextInt(3) + 1;

                            String zeroName = "zero" + 1 + "_img";

                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.O;
                            winChecker();
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                        } else if (userOne == Constant.MY_SYMBOLE_X) {
                            Random random = new Random();
                            int randomID = random.nextInt(4) + 1;

                            String crossName = "cross" + 1 + "_img";
                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.X;
                            winChecker();
                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);

                        }

                    }else if( fieldValuesArray[position] == FieldValue.Empty){
                        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_one_string));
                        if (userTwo == Constant.MY_SYMBOLE_O) {

                            Random random = new Random();
                            int randomID = random.nextInt(3) + 1;

                            String zeroName = "zero" + 1 + "_img";

                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.O;
                            winChecker();
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                        } else if (userTwo == Constant.MY_SYMBOLE_X) {
                            Random random = new Random();
                            int randomID = random.nextInt(4) + 1;

                            String crossName = "cross" + 1 + "_img";
                            imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
                            fieldValuesArray[position] = FieldValue.X;
                            winChecker();
                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);

                        }
                    }
                    isUserOneTurn = !isUserOneTurn;

                }
            });


            return convertView;
        }

    }

    private void winChecker() {

        fieldArrayToMarix();
        Bundle bundle = GameChecker.chechForWinCombination(fieldValuesMatrix, 3);
        if (bundle != null){
            String winerSymbole = bundle.getString(GameChecker.WIN_SYMBOL);

            writWinRedLine(bundle);
            increaseUserScore(winerSymbole);
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

    private void increaseUserScore(String winerSymbole) {
        if(winerSymbole.equals("X")){
          if (userOne == Constant.MY_SYMBOLE_X){
              winsUserOne++;
              winnerInThisRound = getString(R.string.user_one_string);
          } else {
              winnerInThisRound = getString(R.string.user_two_string);
              winsUserTwo++;
          }

        }else {
            if (userOne == Constant.MY_SYMBOLE_O){
                winnerInThisRound = getString(R.string.user_one_string);
                winsUserOne++;
            } else {
                winnerInThisRound = getString(R.string.user_two_string);
                winsUserTwo++;
            }
        }
    }
    private void showEndGameDialog (int winerStatus ){
        isGameFinish = true;

        if (winsUserOne + winsUserTwo == 10 ){
            AchievementUnlokUtil.init(((GameActivity) parentActivity).getGameHelper().getApiClient(), parentActivity);
            AchievementUnlokUtil.unlockFriendlyGamer();
        }
        switch (winerStatus){
            case -1:{
                showDrawMessage();
                break;
            }
            default:{
                    showWinMessage();
                break;
            }
        }
        fillScoreView();
    }
    private void showDrawMessage() {
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.draw_game_string));

        continueNotificationShow();
    }

    private void showWinMessage() {
        SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.win_user_string) + " " + winnerInThisRound);

        continueNotificationShow();
    }



    private void continueNotificationShow() {
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
            initNewGame();
            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
        }
    }
    private class NoOnClickButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            parentActivity.finish();
            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
        }
    }
    private void initNewGame(){

        fillDataInView();
        initFieldValues();
        initialGameField();
    }
    private void fillScoreView(){
        if (winsUserOne < 10){
            myScoreFirstCounterTextView.setText(String.valueOf(winsUserOne));
        }else {
            String second   = String.valueOf(winsUserOne).substring(0 ,1);
            String ferst = String.valueOf(winsUserOne).substring(1);
            myScoreFirstCounterTextView.setText(ferst);
            myScoreSecondCounterTextView.setText(second);
        }

        if (winsUserTwo < 10){
            opponentScoreFirstCounterTextView.setText(String.valueOf(winsUserTwo));
        }else {
            String second   = String.valueOf(winsUserTwo).substring(0 ,1);
            String ferst = String.valueOf(winsUserTwo).substring(1);
            opponentScoreFirstCounterTextView.setText(ferst);
            opponentScoreSecondCounterTextView.setText(second);
        }
    }
    private void fillPlayersData() {
        myUserNameTextView.setText(R.string.user_one_string);
        oponentUserNameTextView.setText(R.string.user_two_string);
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

}
