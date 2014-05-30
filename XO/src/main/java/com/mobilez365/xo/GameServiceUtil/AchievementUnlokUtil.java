package com.mobilez365.xo.GameServiceUtil;

import android.app.Activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.mobilez365.xo.R;

/**
 * Created by BruSD on 5/30/2014.
 */
public  class AchievementUnlokUtil {
    private static GoogleApiClient googleApiClient;
    private static Activity activity;

    public static void init(GoogleApiClient _googleApiClient, Activity _activity){
        googleApiClient = _googleApiClient;
        activity = _activity;
    }

    public static void unlockNewbie(){

        Games.Achievements.unlock(googleApiClient, activity.getString(R.string.achievement_newbie));
    }

    public static void unlockGoodPlayer(){
        Games.Achievements.unlock(googleApiClient, activity.getString(R.string.achievement_good_player));
    }
    public static void unlockBeginer(){
        Games.Achievements.unlock(googleApiClient, activity.getString(R.string.achievement_beginer));
    }
    public static void unlockGamer(){
        Games.Achievements.unlock(googleApiClient, activity.getString(R.string.achievement_gamer));
    }
    public static void unlockFriendlyGamer(){
        Games.Achievements.unlock(googleApiClient, activity.getString(R.string.achievement_friendly_gamer));
    }
}
