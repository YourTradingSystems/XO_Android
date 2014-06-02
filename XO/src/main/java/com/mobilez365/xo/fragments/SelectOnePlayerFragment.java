package com.mobilez365.xo.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.mobilez365.xo.R;
import com.mobilez365.xo.util.Constant;

/**
 * Created by BruSD on 06.05.2014.
 */
public class SelectOnePlayerFragment extends Fragment implements View.OnClickListener {

    private Button btnEasy, btnMedium, btnHard;
    private View rootView;
    private InterstitialAd interstitial;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_choice_one_player, container, false);
        initAllView();
        initAds();
        return rootView;
    }

    private void initAllView() {
        btnEasy = (Button) rootView.findViewById(R.id.easy_player_button);
        btnMedium = (Button) rootView.findViewById(R.id.medium_player_button);
        btnHard = (Button) rootView.findViewById(R.id.hard_player_button);

        btnEasy.setOnClickListener(this);
        btnMedium.setOnClickListener(this);
        btnHard.setOnClickListener(this);
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
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        displayInterstitial();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.easy_player_button: {
                getActivity().sendBroadcast(new Intent(Constant.FILTER_VIEW_EASY));
                break;
            }
            case R.id.medium_player_button: {
                getActivity().sendBroadcast(new Intent(Constant.FILTER_VIEW_MEDIUM));
                break;
            }
            case R.id.hard_player_button: {
                getActivity().sendBroadcast(new Intent(Constant.FILTER_VIEW_HARD));
                break;
            }
        }
    }
}
