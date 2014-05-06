package com.mobilez365.xo;

import android.app.Application;

/**
 * Created by andrewtivodar on 06.05.2014.
 */
public class XOApplication extends Application {

    public static boolean isHidden = false;
    private static boolean activityIsVisible;

    public static boolean isActivityVisible() {
        return activityIsVisible;
    }

    public static void activityResumed() {
        activityIsVisible = true;
    }

    public static void activityPaused() {
        activityIsVisible = false;
    }

}
