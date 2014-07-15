package com.mobilez365.xo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mobilez365.xo.GameServiceUtil.AchievementUnlokUtil;
import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.activity.GameActivity;
import com.mobilez365.xo.ai.AILevel;
import com.mobilez365.xo.ai.AIPlayer;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.ai.GameChecker;
import com.mobilez365.xo.util.AppSettings;
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

  //  private GridView gridview;
    private  CustomViewXOField viewXOField;
    private  Bitmap winLineBitmap;
    private boolean isDrawGame;
    private  int winLine=0; //(=1,2,3,4,5,6,7,8 or 0)

    private TextView myUserNameTextView, opponentUserNameTextView, mySignTextView, opponentSignTextView, timerTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView;//, winLineImageView;
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

        rootView = inflater.inflate(R.layout.fragment_game_test_layout, container, false);

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

      // gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);
      // viewXOField = (CustomViewXOField)rootView.findViewById(R.id.game_xo_custom_view_test);
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.game_field_relative_layout_test);//taras
        viewXOField = new CustomViewXOField(rootView.getContext()); //taras
        //viewXOField.setLayoutParams(new LayoutParams(width, height)); //taras

        relativeLayout.addView(viewXOField, 0);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment_test);
        opponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment_test);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment_test);
        opponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment_test);

        myAvatarImageView = (ImageView)rootView.findViewById(R.id.my_avatar_image_view_game_fragment_test);
        oponentAvatarImageView = (ImageView)rootView.findViewById(R.id.oponent_avatar_image_view_game_fragment_test);

      //  winLineImageView = (ImageView)rootView.findViewById(R.id.win_line_image_view_test); /////////////////////// not taras
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
      //  winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field)); //not taras

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
        unlockAchievement();

        SoundManager.playSound(parentActivity, Constant.WIN_SOUND);
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.win_string));

//        continueNotificationShow();
    }
    private void unlockAchievement(){
        if (mAILevel == AILevel.Easy){
            int lastWins = AppSettings.getEasyWins(parentActivity);
            lastWins++;
            if (lastWins >= 10 ){
                AchievementUnlokUtil.init(((GameActivity)parentActivity).getGameHelper().getApiClient(), parentActivity );
                AchievementUnlokUtil.unlockNewbie();
            }else {
                AppSettings.setEasyWins(parentActivity, lastWins);
            }
        }else if(mAILevel == AILevel.Hard){
            int lastWins = AppSettings.getHardWins(parentActivity);
            lastWins++;
            if (lastWins >= 10 ){
                AchievementUnlokUtil.init(((GameActivity)parentActivity).getGameHelper().getApiClient(), parentActivity );
                AchievementUnlokUtil.unlockGoodPlayer();
            }else {
                AppSettings.setHardWins(parentActivity, lastWins);
            }
        }
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
      //  gridview.setAdapter(new XOImageAdapter(parentActivity, mySign, fieldValuesArray ));


        if (!isMyTurn) {
            moveAI();
        }
    }

    /*
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
*/

    private void myTurnWinChecker(int position){
        winChecker();

//        int xPosition =  position / 3;
//        int yPosition = position - xPosition*3;
//        String message = String.valueOf(xPosition)+ String.valueOf(yPosition);

//        Intent intent =  new Intent(Constant.FILTER_SEND_MY_STROK);
//        intent.putExtra(Constant.INTENT_KEY_MY_STROK, String.valueOf(message) );
//        parentActivity.sendBroadcast(intent);

        isMyTurn = false;
        if (!isGameFinish){
            //moveAI();
        infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));

        timerCountToContinueGame = 2;
        waitinTimer = new Timer();
        waitinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerWaitMoveAI();
            }
        }, 0, 1000);
        timerWaitMoveAI();}

    }

    private void moveAI() {

        Bundle move = AIPlayer.getInstance().getPlayerMove(mAILevel, fieldValuesMatrix, 3,
                mySign == Constant.MY_SYMBOLE_X ? FieldValue.O : FieldValue.X);
        int i = move.getInt(AIPlayer.COORDINATE_X);
        int j = move.getInt(AIPlayer.COORDINATE_Y);

        int arrayIndex = i*3 + j;

        if(mySign == Constant.MY_SYMBOLE_O) {
            fieldValuesArray[arrayIndex] = FieldValue.X;
            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);//////////////taras
        }else {
            fieldValuesArray[arrayIndex] = FieldValue.O;
            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);//////////taras
        }

       // ((XOImageAdapter)gridview.getAdapter()).notifyDataSetChanged();
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

  /*  private void writWinRedLine(Bundle bundle) {
        int startX = bundle.getInt(GameChecker.COORDINATE_START_X);
        int endX = bundle.getInt(GameChecker.COORDINATE_END_X);
        int startY = bundle.getInt(GameChecker.COORDINATE_START_Y);
        int endY = bundle.getInt(GameChecker.COORDINATE_END_Y);

          Log.d("WinLine", " StartX="+startX+" EndX="+endX+" StartY="+startY+" EndY="+endY);

        if (startY == endY){
           // winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_vertical));  not taras
            winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_horizontal)); //taras
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
           // winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_horizontal));  not taras
            winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.line_vertical)); //taras
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
    }*/

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
        //        isDrawGame=true;///////////////////////taras
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

//taras
    private void timerWaitMoveAI() {

        parentActivity.runOnUiThread(MoveAI_Waiting);
    }
//taras
    private Runnable MoveAI_Waiting = new Runnable() {
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

//                if (mySign == Constant.MY_SYMBOLE_X){
//                    mySign = Constant.MY_SYMBOLE_O;
//                    isMyTurn = false;
//                }
//                else {
//                    mySign = Constant.MY_SYMBOLE_X;
//                    isMyTurn = true;
//                }

                moveAI();
                viewXOField.invalidate();
           }
           timerCountToContinueGame = timerCountToContinueGame - 1;

        }
    };

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
                winLineBitmap=null;//////taras
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

                    if(!isGameFinish){

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

                    drawXorO(c, my_padging_h, my_padging_w, h, w);

                 //   infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));

                    myTurnWinChecker(1);

                }

                if (fieldValuesMatrix[xToField(x)][yToField(y)] == FieldValue.Empty && mySign == Constant.MY_SYMBOLE_O) {

                    fieldValuesArray[xToField(x)*3+yToField(y)]=FieldValue.O;


                    drawXorO(c, my_padging_h, my_padging_w, h, w);

                //    infoYourTheyTornTextView.setText(parentActivity.getString(R.string.they_torn_string));

                    myTurnWinChecker(1);

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

