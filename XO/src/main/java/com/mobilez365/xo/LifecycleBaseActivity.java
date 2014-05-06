package com.mobilez365.xo;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

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
