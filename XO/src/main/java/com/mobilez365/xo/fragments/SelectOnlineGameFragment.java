package com.mobilez365.xo.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mobilez365.xo.R;
import com.mobilez365.xo.SoundManager;
import com.mobilez365.xo.XOApplication;
import com.mobilez365.xo.activity.GameActivity;
import com.mobilez365.xo.util.Constant;

/**
 * Created by BruSD on 06.05.2014.
 */
public class SelectOnlineGameFragment extends Fragment implements View.OnClickListener {
    private Button quickGameButton, inviteFriendButton, checkInviteButton;
    private View rootView;
    private Tracker traker;
    private Activity parentActivity;
    private InterstitialAd interstitial;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_choice_online_game_layout, container, false);
        initAds();
        // Get tracker.
        traker= ((XOApplication)getActivity().getApplication()).getTracker(
                XOApplication.TrackerName.APP_TRACKER);

        initAllView();
        parentActivity = getActivity();
        return rootView;
    }

    private void initAds(){
        interstitial = new InterstitialAd(getActivity());
        interstitial.setAdUnitId(Constant.MY_AD_UNIT_ID);
        AdRequest adRequest = new AdRequest.Builder().build();

        // Запуск загрузки межстраничного объявления.
        interstitial.loadAd(adRequest);
    }
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        displayInterstitial();
    }

    private void initAllView(){
        quickGameButton = (Button)rootView.findViewById(R.id.quick_game_button);
        inviteFriendButton = (Button) rootView.findViewById(R.id.invite_friend_button);
        checkInviteButton =  (Button)rootView.findViewById(R.id.pending_invite_button);

        quickGameButton.setOnClickListener(this);
        inviteFriendButton.setOnClickListener(this);
        checkInviteButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id =  v.getId();
        if (((GameActivity)parentActivity).isSignedIn()) {
            switch (id) {
                case R.id.quick_game_button: {
                    sendScreenView(Constant.SCREEN_QUICK_GAME);
                    startQuickGame();
                    break;
                }
                case R.id.invite_friend_button: {
                    sendScreenView(Constant.SCREEN_INVITE_FRIEND);
                    playWithFriend();
                    break;
                }
                case R.id.pending_invite_button: {
                    sendScreenView(Constant.SCREEN_VIEW_INVITE);
                    viewInvite();
                    break;
                }
            }
        }else {

        }

        SoundManager.playSound(getActivity(), Constant.CLICK_SOUND);
    }

    private void sendScreenView(String screen){
        // Set screen name.
        // Where path is a String representing the screen name.
        traker.setScreenName(screen);

        // Send a screen view.
        traker.send(new HitBuilders.AppViewBuilder().build());
    }

    private void viewInvite() {
        getActivity().sendBroadcast(new Intent(Constant.FILTER_VIEW_INVETATION));
    }

    private void playWithFriend() {
        getActivity().sendBroadcast(new Intent(Constant.FILTER_PLAY_WITH_FRIEND));
    }

    private void startQuickGame() {
        getActivity().sendBroadcast(new Intent(Constant.FILTER_START_QUICK_GAME));
    }
}
