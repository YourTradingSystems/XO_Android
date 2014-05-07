package com.mobilez365.xo;

import android.app.Activity;
import android.media.AudioManager;
import android.support.v4.app.FragmentActivity;

import com.mobilez365.xo.util.Constant;

/**
 * Created by andrewtivodar on 06.05.2014.
 */
public class LifecycleBaseActivity extends FragmentActivity {

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
    public void onBackPressed() {
        super.onBackPressed();
        SoundManager.playSound(Constant.CLICK_SOUND);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XOApplication.activityPaused();
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
