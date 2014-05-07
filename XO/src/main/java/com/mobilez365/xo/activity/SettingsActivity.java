package com.mobilez365.xo.activity;

import android.content.SharedPreferences;
import android.media.AudioManager;
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SoundManager.playSound(SoundManager.CLICK_SOUND);
                finish();
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SoundManager.playSound(SoundManager.CLICK_SOUND);
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

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

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
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
