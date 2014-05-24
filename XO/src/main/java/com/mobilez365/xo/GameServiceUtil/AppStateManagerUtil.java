package com.mobilez365.xo.GameServiceUtil;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.mobilez365.xo.R;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by BruSD on 14.05.2014.
 */

public final class AppStateManagerUtil  {
    private static GoogleApiClient googleApiClient;
    private static Activity activity;

    //Cloud Slots
    public static final int USER_ONLINE_WINS_PROGRES = 1;
    private static final int USER_ESY_II_WINS_PROGRES = 2;
    private static final int USER_HARD_II_WINS_PROGRES = 3;
    private static final int USER_TOW_PLAYERS_GAME_PROGRES = 4;

    public static void init(GoogleApiClient _googleApiClient, Activity _activity){

        googleApiClient = _googleApiClient;
        activity = _activity;
    }

    public static void updateOlineProgressAppState(){
//

        AppStateManager.load(googleApiClient, USER_ONLINE_WINS_PROGRES).setResultCallback(new ResultCallback<AppStateManager.StateResult>() {
            @Override
            public void onResult(AppStateManager.StateResult result) {


                AppStateManager.StateLoadedResult loadedResult
                        = result.getLoadedResult();
                if (loadedResult.getStatus().isSuccess()){
                    if (loadedResult != null) {
                        processStateLoaded(loadedResult);
                    }
                }else {
                    String str = "1";
                    byte[] localData = str.getBytes(Charset.forName("UTF-8"));
                    AppStateManager.update(googleApiClient, USER_ONLINE_WINS_PROGRES, localData);
                }

            }
        });

    }
    private static void processStateLoaded(AppStateManager.StateLoadedResult result){
        byte[] localData = result.getLocalData();

        try {
            String str = new String(localData , "UTF-8");
            Log.v("XO", str);
            int score = Integer.valueOf(str);

            score = score + 1;
            str = String.valueOf(score);

            byte[] lastresult = str.getBytes(Charset.forName("UTF-8"));
            Games.Leaderboards.submitScore(googleApiClient, activity.getString(R.string.leader_board_id), score);
            AppStateManager.update(googleApiClient, USER_ONLINE_WINS_PROGRES, lastresult);

        } catch (UnsupportedEncodingException e) {
            Log.v("XO", e.toString());
        }
    }

}
