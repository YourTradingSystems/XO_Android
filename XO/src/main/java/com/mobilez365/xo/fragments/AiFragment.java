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

import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.ai.AILevel;
import com.mobilez365.xo.ai.AIPlayer;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.ai.GameChecker;
import com.mobilez365.xo.util.Constant;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with Android Studio.
 * User: MediumMG
 * Date: 14.05.2014
 * Time: 18:27
 */
public class AiFragment extends Fragment {

    private static final int timeToStrock = 30;
    private static final int timeToContinueGame = 5;

    private Activity parentActivity;
    private View rootView;
    private GridView gridview;
    private TextView myUserNameTextView, opponentUserNameTextView, mySignTextView, opponentSignTextView, timerTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView, winLineImageView;
    private TextView myScoreFirstCounterTextView, myScoreSecondCounterTextView, opponentScoreFirstCounterTextView, opponentScoreSecondCounterTextView;
    private Button noContinueButton, yesContinueButton;
    private TextView infoYourTheyTornTextView, continueTextView;

    private int  timerCountToContinueGame;
    private AILevel mAILevel;
    private boolean isMyTurn;
    private boolean isGameFinish;
    private int mySign;
    private Timer waitinTimer;

    private FieldValue [][] fieldValuesMatrix;
    private FieldValue [] fieldValuesArray;
    private int userWins, aiWins;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_layout, container, false);

        parentActivity = getActivity();

        initGame();


        initAllView();

        initFieldValues();

        switch (getArguments().getInt(Constant.INTENT_KEY_AI_LEVEL, 0)){
            case Constant.AI_HARD: { mAILevel = AILevel.Hard; break; }
            case Constant.AI_MEDIUM: { mAILevel = AILevel.Medium; break; }
            case Constant.AI_EASY:
            default: { mAILevel = AILevel.Easy; break;}
        }
        isMyTurn = false;//new Random().nextBoolean();
        if (isMyTurn){
            mySign = Constant.MY_SYMBOLE_X;
        }
        else {
            mySign = Constant.MY_SYMBOLE_O;
        }

        fillDataInView();
        fillPlayersData();
        initialGameField();

        return rootView;
    }
    private void initGame() {
        userWins = 0;
        aiWins = 0;
        SoundManager.initSound(parentActivity, Constant.LOSE_SOUND);
        SoundManager.initSound(parentActivity, Constant.WIN_SOUND);
        SoundManager.initSound(parentActivity, Constant.GOES_X__SOUND);
        SoundManager.initSound(parentActivity, Constant.GOES_O_SOUND);
    }

    @Override
    public void onStop() {
        if (waitinTimer != null) {
            waitinTimer.cancel();
        }
        super.onStop();
    }

    private void initAllView() {

        gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment);
        opponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment);
        opponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment);

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

    private void initFieldValues() {
        isGameFinish = false;
        fieldValuesMatrix = new FieldValue[3][3];
        fieldValuesArray = new FieldValue[9];
        for (int i =0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                fieldValuesMatrix[i][j] = FieldValue.Empty;
                fieldValuesArray[i*3+j] = FieldValue.Empty;
            }
        }
    }

    private void fillDataInView() {


        if (mySign == Constant.MY_SYMBOLE_X) {
            mySignTextView.setText("X");
            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.your_torn_string));
            opponentSignTextView.setText("O");
        } else {
            mySignTextView.setText("O");
            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));
            opponentSignTextView.setText("X");
        }
        winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));

        noContinueButton.setVisibility(View.INVISIBLE);
        yesContinueButton.setVisibility(View.INVISIBLE);
        continueTextView.setVisibility(View.INVISIBLE);
    }



    private void showLoseMessage() {
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.lose_string));

//        continueNotificationShow();
    }

    private void showWinMessage() {
        SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.win_string));

//        continueNotificationShow();
    }

    private void showDrawMessage() {
        SoundManager.playSound(parentActivity, Constant.LOSE_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.draw_game_string));
//        continueNotificationShow();
    }

    private void fillPlayersData() {
        String oppName;
        switch (mAILevel) {
            case Easy: oppName = "Easy AI"; break;
            case Medium: oppName = "Medium AI"; break;
            case Hard: oppName = "Hard AI"; break;
            default: oppName = "AI"; break;
        }
        opponentUserNameTextView.setText(oppName);
        myUserNameTextView.setText(R.string.player_string);

    }


    private void initialGameField(){
        gridview.setAdapter(new XOImageAdapter(parentActivity, mySign, fieldValuesArray ));

        if (!isMyTurn) {
            moveAI();
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
                        }
                        else if (mySymbole == Constant.MY_SYMBOLE_X) {
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

//        int xPosition =  position / 3;
//        int yPosition = position - xPosition*3;
//        String message = String.valueOf(xPosition)+ String.valueOf(yPosition);

//        Intent intent =  new Intent(Constant.FILTER_SEND_MY_STROK);
//        intent.putExtra(Constant.INTENT_KEY_MY_STROK, String.valueOf(message) );
//        parentActivity.sendBroadcast(intent);

        isMyTurn = false;
        if (!isGameFinish)
            moveAI();
    }

    private void moveAI() {
        Bundle move = AIPlayer.getInstance().getPlayerMove(mAILevel, fieldValuesMatrix, 3,
                mySign == Constant.MY_SYMBOLE_X ? FieldValue.O : FieldValue.X);
        int i = move.getInt(AIPlayer.COORDINATE_X);
        int j = move.getInt(AIPlayer.COORDINATE_Y);

        int arrayIndex = i*3 + j;

        if(mySign == Constant.MY_SYMBOLE_O) {
            fieldValuesArray[arrayIndex] = FieldValue.X;
        }else {
            fieldValuesArray[arrayIndex] = FieldValue.O;
        }

        ((XOImageAdapter)gridview.getAdapter()).notifyDataSetChanged();
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.your_torn_string));
        winChecker();
        isMyTurn = true;
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

        }
        else {
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

    private void fieldArrayToMarix(){
        for (int i =0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                fieldValuesMatrix[i][j] = fieldValuesArray[i*3+j];
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
        }
        else if (startX == endX){
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

    private void showEndGameDialog (int winerStatus){
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
                    userWins ++;
                    showWinMessage();
                }else {
                    aiWins++;
                    showLoseMessage();
                }
                break;
            }
        }
        fillScoreView();
    }

    private void TimerWaitingMethod() {
        parentActivity.runOnUiThread(Timer_Tick_Waiting);
    }

    private Runnable Timer_Tick_Waiting = new Runnable() {
        public void run() {

            if (timerCountToContinueGame > 0){
                if (timerCountToContinueGame < 10){
                    timerTextView.setTextColor(parentActivity.getResources().getColor(android.R.color.holo_red_dark));
                }else{
                    timerTextView.setTextColor(parentActivity.getResources().getColor(android.R.color.black));
                }
                timerTextView.setText(String.valueOf(timerCountToContinueGame));
            }
            else {
                timerTextView.setText(String.valueOf(timerCountToContinueGame));
                waitinTimer.cancel();

                if (mySign == Constant.MY_SYMBOLE_X){
                    mySign = Constant.MY_SYMBOLE_O;
                    isMyTurn = false;
                }
                else {
                    mySign = Constant.MY_SYMBOLE_X;
                    isMyTurn = true;
                }
                fillDataInView();
                initFieldValues();
                initialGameField();
            }
            timerCountToContinueGame = timerCountToContinueGame - 1;
        }
    };
    private void fillScoreView(){
        if (userWins < 10){
            myScoreFirstCounterTextView.setText(String.valueOf(userWins));
        }else {
            String second   = String.valueOf(userWins).substring(0 ,1);
            String ferst = String.valueOf(userWins).substring(1);
            myScoreFirstCounterTextView.setText(ferst);
            myScoreSecondCounterTextView.setText(second);
        }

        if (aiWins < 10){
            opponentScoreFirstCounterTextView.setText(String.valueOf(aiWins));
        }else {
            String second   = String.valueOf(aiWins).substring(0 ,1);
            String ferst = String.valueOf(aiWins).substring(1);
            opponentScoreFirstCounterTextView.setText(ferst);
            opponentScoreSecondCounterTextView.setText(second);
        }
    }
}
