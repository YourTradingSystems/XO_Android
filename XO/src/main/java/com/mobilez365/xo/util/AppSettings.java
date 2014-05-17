package com.mobilez365.xo.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with Android Studio.
 * User: MediumMG
 * Date: 07.05.2014
 * Time: 16:18
 */
public class AppSettings {

    private static SharedPreferences mPreferences;

    private static SharedPreferences getSharedPreferences(Context context){
        if (mPreferences == null) {
            synchronized (AppSettings.class) {
                if (mPreferences == null)
                    mPreferences = context.getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE);
            }
        }
        return mPreferences;
    }

    private static void setBoolean(Context context, String key, Boolean value){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean isSoundEnabled(Context context) {
        if (!getSharedPreferences(context).contains(Constant.KEY_SOUND_EFFECTS)) {
            setBoolean(context, Constant.KEY_SOUND_EFFECTS, false);
        }
        return getSharedPreferences(context).getBoolean(Constant.KEY_SOUND_EFFECTS, false);
    }

    public static void setSoundState(Context context, boolean value) {
        setBoolean(context, Constant.KEY_SOUND_EFFECTS, value);
    }

    public static boolean isMusicEnabled(Context context) {
        if (!getSharedPreferences(context).contains(Constant.KEY_BACKGROUND_MUSIC)) {
            setBoolean(context, Constant.KEY_BACKGROUND_MUSIC, false);
        }
        return getSharedPreferences(context).getBoolean(Constant.KEY_BACKGROUND_MUSIC, false);
    }
    public static void setMusicState(Context context, boolean value) {
        setBoolean(context, Constant.KEY_BACKGROUND_MUSIC, value);
    }

    public static boolean isPushEnabled(Context context) {
        if (!getSharedPreferences(context).contains(Constant.KEY_PUSH)) {
            setBoolean(context, Constant.KEY_PUSH, false);
        }
        return getSharedPreferences(context).getBoolean(Constant.KEY_PUSH, false);
    }
    public static void setPushState(Context context, boolean value) {
        setBoolean(context, Constant.KEY_PUSH, value);
    }

    public static boolean isAnalyticsEnabled(Context context) {
        if (!getSharedPreferences(context).contains(Constant.KEY_ANALYTICS)) {
            setBoolean(context, Constant.KEY_ANALYTICS, false);
        }
        return getSharedPreferences(context).getBoolean(Constant.KEY_ANALYTICS, false);
    }
    public static void setAnalyticsState(Context context, boolean value) {
        setBoolean(context, Constant.KEY_ANALYTICS, value);
    }

}
