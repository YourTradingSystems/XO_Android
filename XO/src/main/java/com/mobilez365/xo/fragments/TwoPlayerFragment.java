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

   /* private GridView gridview;*/ //taras

    private  CustomViewXOField viewXOField;
    private Bitmap winLineBitmap;
    private  int winLine=0; //(=1,2,3,4,5,6,7,8 or 0)

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
        rootView = inflater.inflate(R.layout.fragment_game_test_layout, container, false);
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

        userOne = Constant.MY_SYMBOLE_X;


        winsUserOne = 0;
        winsUserTwo = 0;

        isUserOneTurn = true;
        isGameFinish = false;

    }



    private void initialAllView() {

        /*gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);*/ //taras
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.game_field_relative_layout_test);//taras
        viewXOField = new CustomViewXOField(rootView.getContext()); //taras
        TextView timerTextView = (TextView)rootView.findViewById(R.id.timer_text_view_game_fragment_test);//taras
        timerTextView.setVisibility(View.GONE);//taras

        relativeLayout.addView(viewXOField, 0);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment_test);
        oponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment_test);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment_test);
        oponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment_test);

        myAvatarImageView = (ImageView)rootView.findViewById(R.id.my_avatar_image_view_game_fragment_test);
        oponentAvatarImageView = (ImageView)rootView.findViewById(R.id.oponent_avatar_image_view_game_fragment_test);

        //winLineImageView = (ImageView)rootView.findViewById(R.id.win_line_image_view); //taras

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

   //     winLineImageView.setImageDrawable(getResources().getDrawable(R.drawable.zero_field));
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
       /*gridview.setAdapter(new XOImageAdapter(parentActivity, fieldValuesArray ));*/ //taras
winChecker();

    }
/* //taras
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

  */
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

        if (winsUserOne + winsUserTwo == 10 && ((GameActivity) parentActivity).isSignedIn() ){
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
            winLineBitmap=null;///taras
        }
    }
    private class NoOnClickButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            parentActivity.finish();
            SoundManager.playSound(parentActivity, Constant.CLICK_SOUND);
            winLineBitmap=null;////taras
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

    //taras
   /* private void writWinRedLine(Bundle bundle) {
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
    }*/
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
            winLineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.line_horizontal);

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

                    if(!isGameFinish &&  fieldValuesMatrix[xToField(event.getX())][yToField(event.getY())] == FieldValue.Empty){

                        if (isUserOneTurn && userOne == Constant.MY_SYMBOLE_X){
                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);
                            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_two_string));
                        }

                        if (isUserOneTurn && userOne == Constant.MY_SYMBOLE_O){
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_two_string));
                        }

                        if (!isUserOneTurn && userOne == Constant.MY_SYMBOLE_X){
                            SoundManager.playSound(parentActivity, Constant.GOES_O_SOUND);
                            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_one_string));
                        }
                        if (!isUserOneTurn && userOne == Constant.MY_SYMBOLE_O){
                            SoundManager.playSound(parentActivity, Constant.GOES_X__SOUND);
                            infoYourTheyTornTextView.setText(parentActivity.getString(R.string.user_one_string));
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

                if (fieldValuesMatrix[xToField(x)][yToField(y)] == FieldValue.Empty && userOne == Constant.MY_SYMBOLE_X) {

                    if(isUserOneTurn) {
                        fieldValuesArray[xToField(x) * 3 + yToField(y)] = FieldValue.X;
                        fieldArrayToMarix();
                        isUserOneTurn=!isUserOneTurn;
                        winChecker();
                    } else {
                        fieldValuesArray[xToField(x) * 3 + yToField(y)] = FieldValue.O;
                        fieldArrayToMarix();
                        isUserOneTurn=!isUserOneTurn;
                        winChecker();
                    }
                 //   myTurnWinChecker(1);
                }

                if (fieldValuesMatrix[xToField(x)][yToField(y)] == FieldValue.Empty && userOne == Constant.MY_SYMBOLE_O) {

                    if(isUserOneTurn) {
                        fieldValuesArray[xToField(x) * 3 + yToField(y)] = FieldValue.O;
                        fieldArrayToMarix();
                        isUserOneTurn=!isUserOneTurn;
                        winChecker();
                    } else {
                        fieldValuesArray[xToField(x) * 3 + yToField(y)] = FieldValue.X;
                        fieldArrayToMarix();
                        isUserOneTurn=!isUserOneTurn;
                        winChecker();
                    }
                 //   myTurnWinChecker(1);
                }

                clicked = !clicked;
            }

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (fieldValuesMatrix[i][j] == FieldValue.X) {

                        Bitmap bmp = BitmapFactory.decodeResource(getResources(),mResourceMatrixForX[i][j]);// R.drawable.cross1_img);
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
