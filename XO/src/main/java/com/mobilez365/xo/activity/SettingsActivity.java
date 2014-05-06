package com.mobilez365.xo.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.XOApplication;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String SOUND_EFFECTS_ENABLED = "sound_enabled";
    public final static String BACKGROUND_MUSIC_ENABLED = "music_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_settings);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(BACKGROUND_MUSIC_ENABLED)) {
            boolean backgroundMusicEnabled = sharedPreferences.getBoolean(BACKGROUND_MUSIC_ENABLED, false);
            if(backgroundMusicEnabled)
                SoundManager.playBackgroundMusic(this);
            else
                SoundManager.stopBackgroundMusic();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        XOApplication.activityResumed();
        if (XOApplication.isHidden) {
            XOApplication.isHidden = false;
            SoundManager.resumeBackgroundMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        XOApplication.activityPaused();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SoundManager.playBackgroundMusic(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!XOApplication.isActivityVisible()) {
            XOApplication.isHidden = true;
            SoundManager.pauseBackgroundMusic();
        }
    }
}
