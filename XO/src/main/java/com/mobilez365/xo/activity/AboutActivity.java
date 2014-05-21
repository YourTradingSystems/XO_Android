package com.mobilez365.xo.activity;

import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.mobilez365.xo.LifecycleBaseActivity;
import com.mobilez365.xo.R;
import com.mobilez365.xo.util.Constant;

/**
 * Created by andrewtivodar on 08.05.2014.
 */
public class AboutActivity extends LifecycleBaseActivity {
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        interstitial = new InterstitialAd(this);
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
    protected void onResume() {
        super.onResume();
        displayInterstitial();
    }
}
