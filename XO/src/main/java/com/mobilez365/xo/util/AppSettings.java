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

    public static final String KEY_SOUND_EFFECTS    = "sound_effects_setting";
    public static final String KEY_BACKGROUND_MUSIC = "background_music_setting";
    public static final String KEY_PUSH             = "push_setting";
    public static final String KEY_ANALYTICS        = "analytics_setting";

    private static final String PREF_NAME           = "com.mobilez365.xo.appSettings";

    private static SharedPreferences mPreferences;
    private static SharedPreferences getSharedPreferences(Context context){
        if (mPreferences == null) {
            synchronized (AppSettings.class) {
                if (mPreferences == null)
                    mPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        if (!getSharedPreferences(context).contains(KEY_SOUND_EFFECTS)) {
            setBoolean(context, KEY_SOUND_EFFECTS, false);
        }
        return getSharedPreferences(context).getBoolean(KEY_SOUND_EFFECTS, false);
    }
    public static void setSoundState(Context context, boolean value) {
        setBoolean(context, KEY_SOUND_EFFECTS, value);
    }

    public static boolean isMusicEnabled(Context context) {
        if (!getSharedPreferences(context).contains(KEY_BACKGROUND_MUSIC)) {
            setBoolean(context, KEY_BACKGROUND_MUSIC, false);
        }
        return getSharedPreferences(context).getBoolean(KEY_BACKGROUND_MUSIC, false);
    }
    public static void setMusicState(Context context, boolean value) {
        setBoolean(context, KEY_BACKGROUND_MUSIC, value);
    }

    public static boolean isPushEnabled(Context context) {
        if (!getSharedPreferences(context).contains(KEY_PUSH)) {
            setBoolean(context, KEY_PUSH, false);
        }
        return getSharedPreferences(context).getBoolean(KEY_PUSH, false);
    }
    public static void setPushState(Context context, boolean value) {
        setBoolean(context, KEY_PUSH, value);
    }

    public static boolean isAnalyticsEnabled(Context context) {
        if (!getSharedPreferences(context).contains(KEY_ANALYTICS)) {
            setBoolean(context, KEY_ANALYTICS, false);
        }
        return getSharedPreferences(context).getBoolean(KEY_ANALYTICS, false);
    }
    public static void setAnalyticsState(Context context, boolean value) {
        setBoolean(context, KEY_ANALYTICS, value);
    }

}
