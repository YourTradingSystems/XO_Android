package com.mobilez365.xo.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobilez365.xo.R;

/**
 * Created by BruSD on 06.05.2014.
 */
public class SelectOnlineGameFragment extends Fragment implements View.OnClickListener{
    private Button quickGameButton, inviteFriendButton, checkInviteButton;
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_choice_online_game_layout, container, false);
        initAllView();
        return rootView;
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
        switch (id){
            case R.id.quick_game_button:{
                startQuickGame();
                break;
            }
            case R.id.invite_friend_button:{
                inviteFriend();
                break;
            }
            case R.id.pending_invite_button:{
                viewInvite();
                break;
            }
        }
    }

    private void viewInvite() {
        getActivity().sendBroadcast(new Intent("viewInvite"));
    }

    private void inviteFriend() {
        getActivity().sendBroadcast(new Intent("inviteFriend"));
    }

    private void startQuickGame() {
        getActivity().sendBroadcast(new Intent("startQuickGame"));
        
    }
}
