package com.mobilez365.xo.activity;

import android.os.Bundle;

import com.mobilez365.xo.LifecycleBaseActivity;
import com.mobilez365.xo.R;

/**
 * Created by andrewtivodar on 08.05.2014.
 */
public class AboutActivity extends LifecycleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
