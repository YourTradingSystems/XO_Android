package com.mobilez365.xo;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mobilez365.xo.util.Constant;

import java.util.HashMap;

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

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = Constant.GA_ACCOUNT_ID;

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public XOApplication() {
        super();
    }
    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t =  analytics.newTracker(PROPERTY_ID);

            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}



