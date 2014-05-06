package com.mobilez365.xo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.mobilez365.xo.activity.SettingsActivity;

import java.io.IOException;

/**
 * Created by andrewtivodar on 05.05.2014.
 */
public class SoundManager{
    private static SoundPool soundPool;
    private static SharedPreferences prefs;
    private static MediaPlayer backgroundMusicPlayer;

    private static int clickSoundId = -1;
    private static int winSoundId = -1;
    private static int loseSoundId = -1;
    private static int goesXSoundId = -1;
    private static int goesOSoundId = -1;

    private static void initSoundPool() {
        if (soundPool == null)
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    }

    private static void initSharedPref(Context context){
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    private static void initClickSound(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor = assetManager.openFd("sound_click.mp3");
        clickSoundId = soundPool.load(descriptor, 1);
    }

    private static void initWinSound(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor = assetManager.openFd("sound_win.mp3");
        winSoundId = soundPool.load(descriptor, 1);
    }

    private static void initLoseSound(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor = assetManager.openFd("sound_lose.mp3");
        loseSoundId = soundPool.load(descriptor, 1);
    }

    private static void initGoesXSound(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor = assetManager.openFd("sound_goes_x.mp3");
        goesXSoundId = soundPool.load(descriptor, 1);
    }

    private static void initGoesOSound(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor = assetManager.openFd("sound_goes_o.mp3");
        goesOSoundId = soundPool.load(descriptor, 1);
    }

    public static void playBackgroundMusic(Context context) {
        initSharedPref(context);
        boolean backgroundMusicEnabled = prefs.getBoolean(SettingsActivity.BACKGROUND_MUSIC_ENABLED, false);
        if(backgroundMusicEnabled) {
            if(backgroundMusicPlayer == null) {
                backgroundMusicPlayer = MediaPlayer.create(context, R.raw.background_music);
                backgroundMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                backgroundMusicPlayer.setLooping(true);
                backgroundMusicPlayer.start();
            }
            else
                resumeBackgroundMusic();
        }
    }

    public static void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.release();
                backgroundMusicPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null)
            backgroundMusicPlayer.pause();
    }

    public static void resumeBackgroundMusic() {
        if (backgroundMusicPlayer != null)
            backgroundMusicPlayer.start();
    }

    public static void playClickSound(Context context) {
        initSoundPool();
        initSharedPref(context);
        try {
            initClickSound(context);
        } catch (IOException e) {
            Toast.makeText(context, "Sound files not found", Toast.LENGTH_LONG).show();
            return;
        }

        boolean soundEffectsEnabled = prefs.getBoolean(SettingsActivity.SOUND_EFFECTS_ENABLED, false);
        if (clickSoundId != -1 && soundEffectsEnabled) {
            soundPool.play(clickSoundId, 1, 1, 0, -1, 1);
        }
    }

    public static void playWinSound(Context context) {
        initSoundPool();
        initSharedPref(context);
        try {
            initClickSound(context);
        } catch (IOException e) {
            Toast.makeText(context, "Sound files not found", Toast.LENGTH_LONG).show();
            return;
        }

        boolean soundEffectsEnabled = prefs.getBoolean(SettingsActivity.SOUND_EFFECTS_ENABLED, false);
        if (winSoundId != -1 && soundEffectsEnabled) {
            soundPool.play(winSoundId, 1, 1, 0, -1, 1);
        }
    }

    public static void playLoseSound(Context context) {
        initSoundPool();
        initSharedPref(context);
        try {
            initClickSound(context);
        } catch (IOException e) {
            Toast.makeText(context, "Sound files not found", Toast.LENGTH_LONG).show();
            return;
        }

        boolean soundEffectsEnabled = prefs.getBoolean(SettingsActivity.SOUND_EFFECTS_ENABLED, false);
        if (loseSoundId != -1 && soundEffectsEnabled) {
            soundPool.play(loseSoundId, 1, 1, 0, -1, 1);
        }
    }

    public static void playGoesXSound(Context context) {
        initSoundPool();
        initSharedPref(context);
        try {
            initClickSound(context);
        } catch (IOException e) {
            Toast.makeText(context, "Sound files not found", Toast.LENGTH_LONG).show();
            return;
        }

        boolean soundEffectsEnabled = prefs.getBoolean(SettingsActivity.SOUND_EFFECTS_ENABLED, false);
        if (goesXSoundId != -1 && soundEffectsEnabled) {
            soundPool.play(goesXSoundId, 1, 1, 0, -1, 1);
        }
    }

    public static void playGoesOSound(Context context) {
        initSoundPool();
        initSharedPref(context);
        try {
            initClickSound(context);
        } catch (IOException e) {
            Toast.makeText(context, "Sound files not found", Toast.LENGTH_LONG).show();
            return;
        }

        boolean soundEffectsEnabled = prefs.getBoolean(SettingsActivity.SOUND_EFFECTS_ENABLED, false);
        if (goesOSoundId != -1 && soundEffectsEnabled) {
            soundPool.play(goesOSoundId, 1, 1, 0, -1, 1);
        }
    }

}
