package com.mobilez365.xo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;

import com.google.android.gms.plus.Plus;
import com.mobilez365.xo.GameServiceUtil.BaseGameActivity;
import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.util.AppSettings;
import com.mobilez365.xo.util.Constant;

/**
 * Created by andrewtivodar on 08.05.2014.
 */
public class PreferencesActivity extends BaseGameActivity implements View.OnClickListener {

    private CheckedTextView cbSound;
    private CheckedTextView cbMusic;
    private CheckedTextView cbPush;
    private CheckedTextView cbAnalytics;
    private Button buttonLoginLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        initViews();
    }

    private void initViews() {
        cbSound = (CheckedTextView) findViewById(R.id.activity_preferences_sound);
        cbMusic = (CheckedTextView) findViewById(R.id.activity_preferences_music);
        cbPush = (CheckedTextView) findViewById(R.id.activity_preferences_push);
        cbAnalytics = (CheckedTextView) findViewById(R.id.activity_preferences_analytics);

        buttonLoginLogout = (Button)findViewById(R.id.activity_preferences_signin_signout);

        cbSound.setChecked(AppSettings.isSoundEnabled(this));
        cbMusic.setChecked(AppSettings.isMusicEnabled(this));
        cbPush.setChecked(AppSettings.isPushEnabled(this));
        cbAnalytics.setChecked(AppSettings.isAnalyticsEnabled(this));

        cbSound.setOnClickListener(this);
        cbMusic.setOnClickListener(this);
        cbPush.setOnClickListener(this);
        cbAnalytics.setOnClickListener(this);
        buttonLoginLogout.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        SoundManager.playSound(this, Constant.CLICK_SOUND);
        if (v.getId() == R.id.activity_preferences_signin_signout){
            if (isSignedIn()){
                signOut();
                buttonLoginLogout.setText(R.string.common_signin_button_text_long);
            }  else {
                beginUserInitiatedSignIn();
            }
           return;
        }
        boolean checked = !((CheckedTextView) v).isChecked();
        ((CheckedTextView) v).setChecked(checked);
        switch (v.getId()) {
            case R.id.activity_preferences_sound:
                AppSettings.setSoundState(this, checked);
                break;
            case R.id.activity_preferences_music:
                AppSettings.setMusicState(this, checked);
                if (checked)
                    SoundManager.playBackgroundMusic(this);
                else
                    SoundManager.stopBackgroundMusic();
                break;
            case R.id.activity_preferences_push:
                AppSettings.setPushState(this, checked);
                break;
            case R.id.activity_preferences_analytics:
                AppSettings.setAnalyticsState(this, checked);
                break;

        }

    }

    @Override
    public void onSignInFailed() {
        buttonLoginLogout.setText(R.string.common_signin_button_text_long);

    }

    @Override
    public void onSignInSucceeded() {
        buttonLoginLogout.setText(getString(R.string.settings_you_login_as_string)+Plus.PeopleApi.getCurrentPerson(mHelper.getApiClient()).getDisplayName());

    }

}
