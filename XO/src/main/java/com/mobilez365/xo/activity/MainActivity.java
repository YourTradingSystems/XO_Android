package com.mobilez365.xo.activity;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.mobilez365.xo.GameServiceUtil.BaseGameActivity;
import com.mobilez365.xo.R;

import org.w3c.dom.Text;


/**
 * Created by BruSD on 02.05.2014.
 */
public class MainActivity extends BaseGameActivity implements View.OnClickListener{

    private Button onePlayerButton, twoPlayerButton, onlinePlayButton, aboutButton;
    private TextView userNameTextView;
    private Person userAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        initAllView();
    }
    private void initAllView(){
        //Buttons
        onePlayerButton = (Button)findViewById(R.id.button_one_player_main_activity);
        twoPlayerButton = (Button)findViewById(R.id.button_two_player_main_activity);
        onlinePlayButton = (Button)findViewById(R.id.button_online_main_activity);
        aboutButton = (Button)findViewById(R.id.button_about_main_activity);

        onePlayerButton.setOnClickListener(this);
        twoPlayerButton.setOnClickListener(this);
        onlinePlayButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);

        //Views

        userNameTextView = (TextView)findViewById(R.id.user_name_text_view);

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

    @Override
    public void onSignInFailed() {
        userNameTextView.setText(R.string.please_login_string);
    }

    @Override
    public void onSignInSucceeded() {
        if (mHelper.getApiClient().isConnected()) {

            userNameTextView.setText(Plus.AccountApi.getAccountName(mHelper.getApiClient()));

        } else {
            // not signed in. Show the "sign in" button and explanation.
            // ...
        }

    }
}
