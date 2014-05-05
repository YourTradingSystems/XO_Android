package com.mobilez365.xo;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created by BruSD on 02.05.2014.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    private Button onePlayerButton, twoPlayerButton, onlinePlayButton, aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        initAllView();
    }
    private void initAllView(){
        onePlayerButton = (Button)findViewById(R.id.button_one_player_main_activity);
        twoPlayerButton = (Button)findViewById(R.id.button_two_player_main_activity);
        onlinePlayButton = (Button)findViewById(R.id.button_online_main_activity);
        aboutButton = (Button)findViewById(R.id.button_about_main_activity);

        onePlayerButton.setOnClickListener(this);
        twoPlayerButton.setOnClickListener(this);
        onlinePlayButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
       int id =  v.getId();
        switch (id){
            case R.id.button_one_player_main_activity:{
                //TODO: Open Player VS II Game
                break;
            }
            case R.id.button_two_player_main_activity:{
                //TODO: Open Player VS Player Game
                break;
            }
            case R.id.button_online_main_activity:{
                //TODO: Open Online  Game
                break;
            }

        }
    }
}
