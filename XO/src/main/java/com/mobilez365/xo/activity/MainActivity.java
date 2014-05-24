package com.mobilez365.xo.activity;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.Games;


import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.GameServiceUtil.BaseGameActivity;
import com.mobilez365.xo.R;
import com.mobilez365.xo.XOApplication;
import com.mobilez365.xo.XOApplication.*;
import com.mobilez365.xo.util.Constant;
import com.startad.lib.SADView;






/**
 * Created by BruSD on 02.05.2014.
 */
public class MainActivity extends BaseGameActivity implements View.OnClickListener{

    private TextView onePlayerButton, twoPlayerButton, onlinePlayButton, aboutButton, settingsButton, leaderBoardButton;
    private Button achievementsButton;

    protected SADView sadView;
    private  Tracker traker;

    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableDebugLog(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        // Create the adView
        this.sadView = new SADView(this, Constant.START_AD_MOVBI_PUBLISHER_ID);

        // Get tracker.
        traker= ((XOApplication)this.getApplication()).getTracker(
                TrackerName.APP_TRACKER);

        LinearLayout layout = (LinearLayout)findViewById(R.id.main_activity_adsPanel);

        // Add the adView to it
        layout.addView(this.sadView);

        //Load ad for currently active language in app
        this.sadView.loadAd(SADView.LANGUAGE_EN);
         //or this.sadView.loadAd(SADView.LANGUAGE_RU);
        SoundManager.initSound(this, Constant.CLICK_SOUND);
        initAllView();

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(Constant.MY_AD_UNIT_ID);
        AdRequest adRequest = new AdRequest.Builder().build();

        // Запуск загрузки межстраничного объявления.
        interstitial.loadAd(adRequest);



    }
    @Override
    protected void onResume() {
        super.onResume();
        sendScreenView(Constant.SCREEN_MAIN);
    }
    private void sendScreenView(String screen){
        // Set screen name.
        // Where path is a String representing the screen name.
        traker.setScreenName(screen);

        // Send a screen view.
        traker.send(new HitBuilders.AppViewBuilder().build());
    }
    @Override
    public void onDestroy() {
        if (sadView != null) {
            sadView.destroy();
        }
        super.onDestroy();
    }
    private void initAllView() {

        onePlayerButton = (TextView) findViewById(R.id.main_activity_one_player);
        twoPlayerButton = (TextView) findViewById(R.id.main_activity_two_player);
        onlinePlayButton = (TextView) findViewById(R.id.main_activity_play_online);
        aboutButton = (TextView) findViewById(R.id.main_activity_about);
        settingsButton = (Button) findViewById(R.id.main_activity_settings);
        leaderBoardButton = (Button) findViewById(R.id.main_activity_leaderboard);
        achievementsButton  = (Button) findViewById(R.id.main_activity_achievements);

        onePlayerButton.setOnClickListener(this);
        twoPlayerButton.setOnClickListener(this);
        onlinePlayButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        leaderBoardButton.setOnClickListener(this);
        achievementsButton.setOnClickListener(this);

        //Views

    }
    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){
            case R.id.main_activity_one_player:{
                sendScreenView(Constant.SCREEN_SINGLE_PLAYER);
                Intent myIntent = new Intent(this, GameActivity.class);
                myIntent.putExtra("screenType", Constant.SCREEN_TYPE_ONE_PLAYER);
                startActivity(myIntent);

                break;
            }
            case R.id.main_activity_two_player:{
                sendScreenView(Constant.SCREEN_TWO_PLAYER);
                Intent myIntent = new Intent(this, GameActivity.class);
                myIntent.putExtra("screenType", Constant.SCREEN_TYPE_TWO_PLAYER);
                startActivity(myIntent);
                //TODO: Open Player VS Player Game
                break;
            }
            case R.id.main_activity_play_online:{
                sendScreenView(Constant.SCREEN_ONLINE_GAME_CHOICE);
                Intent myIntent = new Intent(this, GameActivity.class);
                myIntent.putExtra("screenType", Constant.SCREEN_TYPE_ONLINE);
                startActivity(myIntent);
                break;
            }
            case R.id.main_activity_settings:{
                sendScreenView(Constant.SCREEN_SETTINGS);
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            }
            case R.id.main_activity_about:{
                sendScreenView(Constant.SCREEN_ABOUT);
                startActivity(new Intent(this, AboutActivity.class));
                break;
            }
            case R.id.main_activity_leaderboard:{
                if(isSignedIn()){
                    sendScreenView(Constant.SCREEN_LEADER_BOARD);
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), getString(R.string.leader_board_id)), 2);
                }
                break;
            }
            case R.id.main_activity_achievements:{
                if(isSignedIn()){
                    sendScreenView(Constant.SCREEN_ACHIEVEMENTS);
                    startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 1);
                }
                break;
            }
        }
        SoundManager.playSound(this, Constant.CLICK_SOUND);
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {
        if (mHelper.getApiClient().isConnected()) {
            //userNameTextView.setText(Plus.PeopleApi.getCurrentPerson(mHelper.getApiClient()).getDisplayName());

        } else {
            // not signed in. Show the "sign in" button and explanation.
            // ...
        }

    }
}