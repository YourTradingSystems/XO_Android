package com.mobilez365.xo.activity;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.plus.Plus;
import com.mobilez365.xo.gameserviceutil.BaseGameActivity;
import com.mobilez365.xo.R;
import com.mobilez365.xo.util.Constant;


/**
 * Created by BruSD on 02.05.2014.
 */
public class MainActivity extends BaseGameActivity implements View.OnClickListener{

    private TextView onePlayerButton, twoPlayerButton, onlinePlayButton, aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        initAllView();
    }

    private void initAllView() {

        onePlayerButton = (TextView) findViewById(R.id.main_activity_one_player);
        twoPlayerButton = (TextView) findViewById(R.id.main_activity_two_player);
        onlinePlayButton = (TextView) findViewById(R.id.main_activity_play_online);
        aboutButton = (TextView) findViewById(R.id.main_activity_about);

        onePlayerButton.setOnClickListener(this);
        twoPlayerButton.setOnClickListener(this);
        onlinePlayButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);

        //Views

    }
    @Override
    public void onClick(View v) {
       int id =  v.getId();
        switch (id){
            case R.id.main_activity_one_player:{
                Intent myIntent = new Intent(this, GameActivity.class);
                myIntent.putExtra("screenType", Constant.SCREEN_TYPE_ONE_PLAYER);
                startActivity(myIntent);
                break;
            }
            case R.id.main_activity_two_player:{
                Intent myIntent = new Intent(this, GameActivity.class);
                myIntent.putExtra("screenType", Constant.SCREEN_TYPE_TWO_PLAYER);
                startActivity(myIntent);
                //TODO: Open Player VS Player Game
                break;
            }
            case R.id.main_activity_play_online:{
                Intent myIntent = new Intent(this, GameActivity.class);
                myIntent.putExtra("screenType", Constant.SCREEN_TYPE_ONLINE);
                startActivity(myIntent);
                break;
            }

        }
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {
        if (mHelper.getApiClient().isConnected()) {
            //userNameTextView.setText(Plus.PeopleApi.getCurrentPerson(mHelper.getApiClient()).getDisplayName());

        } else {
            // not signed in. Show the "sign in" button and explanation.
            // ...
        }

    }
}
