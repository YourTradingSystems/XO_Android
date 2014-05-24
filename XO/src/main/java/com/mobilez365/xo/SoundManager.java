package com.mobilez365.xo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.mobilez365.xo.util.AppSettings;
import com.mobilez365.xo.util.Constant;

import java.io.IOException;

/**
 * Created by andrewtivodar on 05.05.2014.
 */
public class SoundManager {
    private static SoundPool soundPool;
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

    public static void initSound(Context context, int soundId) {
        initSoundPool();
        AssetManager assetManager = context.getAssets();
        AssetFileDescriptor descriptor;

        try {
            switch (soundId) {
                case Constant.CLICK_SOUND:
                    descriptor = assetManager.openFd("sound_click.mp3");
                    clickSoundId = soundPool.load(descriptor, 1);
                    break;
                case Constant.WIN_SOUND:
                    descriptor = assetManager.openFd("sound_win.mp3");
                    winSoundId = soundPool.load(descriptor, 1);
                    break;
                case Constant.LOSE_SOUND:
                    descriptor = assetManager.openFd("sound_lose.mp3");
                    loseSoundId = soundPool.load(descriptor, 1);
                    break;
                case Constant.GOES_X__SOUND:
                    descriptor = assetManager.openFd("sound_goes_x.mp3");
                    goesXSoundId = soundPool.load(descriptor, 1);
                    break;
                case Constant.GOES_O_SOUND:
                    descriptor = assetManager.openFd("sound_goes_o.mp3");
                    goesOSoundId = soundPool.load(descriptor, 1);
                    break;
            }
        } catch (IOException e) {
            Log.e("sound_tag", "sound not found");
        }
    }

    public static void playSound(Context context, int soundId) {
        boolean soundEffectsEnabled = AppSettings.isSoundEnabled(context);
        if (soundEffectsEnabled) {
            int sampleId = -1;
            switch (soundId) {
                case Constant.CLICK_SOUND:
                    sampleId = clickSoundId;
                    break;
                case Constant.WIN_SOUND:
                    sampleId = winSoundId;
                    break;
                case Constant.LOSE_SOUND:
                    sampleId = loseSoundId;
                    break;
                case Constant.GOES_X__SOUND:
                    sampleId = goesXSoundId;
                    break;
                case Constant.GOES_O_SOUND:
                    sampleId = goesOSoundId;
                    break;
            }
            if (sampleId != -1) {
                soundPool.play(sampleId, 1, 1, 0, 0, 1);
            }
        }
    }

    public static void playBackgroundMusic(Context context) {
        boolean backgroundMusicEnabled = AppSettings.isMusicEnabled(context);
        if (backgroundMusicEnabled) {
            if (backgroundMusicPlayer == null) {
                backgroundMusicPlayer = MediaPlayer.create(context, R.raw.background_music);
                backgroundMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                backgroundMusicPlayer.setLooping(true);
                backgroundMusicPlayer.start();
            } else
                resumeBackgroundMusic();
        }
    }

    public static void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
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

}
