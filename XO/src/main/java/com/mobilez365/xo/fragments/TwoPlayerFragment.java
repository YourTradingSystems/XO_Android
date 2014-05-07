package com.mobilez365.xo.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobilez365.xo.R;

/**
 * Created by BruSD on 06.05.2014.
 */
public class TwoPlayerFragment extends Fragment{

    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game_layout, container, false);

        return rootView;
    }
}
