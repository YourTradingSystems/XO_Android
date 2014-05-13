package com.mobilez365.xo.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.mobilez365.xo.GameServiceUtil.BaseGameActivity;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.fragments.OnePlayerFragment;
import com.mobilez365.xo.fragments.OnlineGameFragment;
import com.mobilez365.xo.fragments.SelectOnlineGameFragment;
import com.mobilez365.xo.fragments.TwoPlayerFragment;

import com.mobilez365.xo.R;
import com.mobilez365.xo.util.Constant;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by BruSD on 05.05.2014.
 */
public class GameActivity extends BaseGameActivity {

    private FromFragmentBroadcastReceiver mIntReceiver = new FromFragmentBroadcastReceiver();

    private List<Participant> mParticipants;
    public Room mXORoom;
    private String mXORoomID;
    private String mMyId;
    private boolean isFierstMessage = true;

    private int myRundom365;
    private boolean isMyTurn;
    // request code for the "select players" UI
    // can be any number as long as it's unique
    final static int RC_SELECT_PLAYERS = 10000;
    // request code (can be any number, as long as it's unique)
    final  static int RC_INVITATION_INBOX = 10001;
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_game);

        int fragmentType  = getIntent().getIntExtra("screenType", -1);

        setFragment(fragmentType, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.FILTER_PLAY_WITH_FRIEND);
        filter.addAction(Constant.FILTER_START_QUICK_GAME);
        filter.addAction(Constant.FILTER_VIEW_INVETATION);
        filter.addAction(Constant.FILTER_SEND_MY_STROK);
        filter.addAction(Constant.FILTER_IS_GAME_CONTINUE);
        registerReceiver(mIntReceiver, filter);
    }


    @Override
    protected void onPause() {
        if (mIntReceiver != null)
            unregisterReceiver(mIntReceiver);
        super.onPause();
    }


    private void setFragment( int fragmentType, Bundle bundle) {


        final Fragment fragment = fragmentFromContentType(fragmentType);


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        ft.replace(R.id.game_frame_layout, fragment);
        ft.commitAllowingStateLoss();


    }
    private Fragment fragmentFromContentType(int fragmentType) {
        Fragment fragment;
        switch (fragmentType) {
            case Constant.SCREEN_TYPE_ONE_PLAYER: {
                fragment = new OnePlayerFragment();
                break;
            }
            case Constant.SCREEN_TYPE_TWO_PLAYER: {
                fragment = new TwoPlayerFragment();
                break;
            }
            case Constant.SCREEN_TYPE_ONLINE: {
                fragment = new SelectOnlineGameFragment();
                break;
            }
            case Constant.SCREEN_TYPE_ONLINE_GAME: {

                fragment = new OnlineGameFragment();
                break;
            }
            default: {
                return null;
            }
        }
        return fragment;
    }

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(getApiClient(), roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
    }
    private void inviteFriend() {
        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mHelper.getApiClient(), 1, 1);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        if (request == RC_SELECT_PLAYERS) {
            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            Bundle extras = data.getExtras();
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get auto-match criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria =
                        RoomConfig.createAutoMatchCriteria(
                                minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(getApiClient(), roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (request == RC_INVITATION_INBOX) {
            if (response != Activity.RESULT_OK) {
                // canceled
                return;
            }

            // get the selected invitation
            Bundle extras = data.getExtras();
            Invitation invitation =
                    extras.getParcelable(Multiplayer.EXTRA_INVITATION);

            // accept it!
            RoomConfig roomConfig = makeBasicRoomConfigBuilder()
                    .setInvitationIdToAccept(invitation.getInvitationId())
                    .build();
            Games.RealTimeMultiplayer.join(getApiClient(), roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // go to game screen
        }
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(new XORoomUpdateListener())
                .setMessageReceivedListener(new XORealTimeMessageReceivedListener())
                .setRoomStatusUpdateListener(new XORoomStatusUpdateListener());
    }

    class XORoomUpdateListener implements RoomUpdateListener{
        final static int RC_WAITING_ROOM = 10002;

        @Override
        public void onRoomCreated(int statusCode, Room room) {
            if (statusCode != GamesStatusCodes.STATUS_OK) {
                // let screen go to sleep
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                // show error message, return to main screen.
            }

            // get waiting room intent
            Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, Integer.MAX_VALUE);
            startActivityForResult(i, RC_WAITING_ROOM);


        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            if (statusCode != GamesStatusCodes.STATUS_OK) {
                // display error
                return;
            }

            // get waiting room intent
            Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, Integer.MAX_VALUE);
            startActivityForResult(i, RC_WAITING_ROOM);
        }

        @Override
        public void onLeftRoom(int i, String s) {

        }

        @Override
        public void onRoomConnected(int statusCode, Room room) {
            if (statusCode != GamesStatusCodes.STATUS_OK) {
                // display error
                return;
            }


        }
    }





    class XORoomStatusUpdateListener implements RoomStatusUpdateListener{
        // are we already playing?
        boolean mPlaying = false;

        // at least 2 players required for our game
        final static int MIN_PLAYERS = 2;

        // returns whether there are enough players to start the game
        boolean shouldStartGame(Room room) {
            int connectedPlayers = 0;
            for (Participant p : room.getParticipants()) {
                if (p.isConnectedToRoom()) ++connectedPlayers;
            }
            return connectedPlayers >= MIN_PLAYERS;
        }

        // Returns whether the room is in a state where the game should be canceled.
        boolean shouldCancelGame(Room room) {
            // TODO: Your game-specific cancellation logic here. For example, you might decide to
            // cancel the game if enough people have declined the invitation or left the room.
            // You can check a participant's status with Participant.getStatus().
            // (Also, your UI should have a Cancel button that cancels the game too)
            return false;
        }

        @Override
        public void onPeersConnected(Room room, List<String> peers) {
            if (mPlaying) {
                // add new player to an ongoing game
            }
            else if (shouldStartGame(room)) {
                // start game!
                isFierstMessage = true;

                mParticipants = room.getParticipants();
                mXORoomID = room.getRoomId();

                mXORoom = room;
                mMyId = Games.Players.getCurrentPlayerId(mHelper.getApiClient());

                sendMessageToAllInRoom(String.valueOf(myRundom365));


            }
        }

        @Override
        public void onPeersDisconnected(Room room, List<String> peers) {
            if (mPlaying) {
                // do game-specific handling of this -- remove player's avatar
                // from the screen, etc. If not enough players are left for
                // the game to go on, end the game and leave the room.
            }
            else if (shouldCancelGame(room)) {
                // cancel the game
                Games.RealTimeMultiplayer.leave(getApiClient(), null, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onPeerLeft(Room room, List<String> peers) {
            // peer left -- see if game should be canceled
            if (!mPlaying && shouldCancelGame(room)) {
                Games.RealTimeMultiplayer.leave(getApiClient(), null, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onPeerDeclined(Room room, List<String> peers) {
            ;
            // peer declined invitation -- see if game should be canceled
            if (!mPlaying && shouldCancelGame(room)) {
                Games.RealTimeMultiplayer.leave(getApiClient(), null, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onPeerJoined(Room room, List<String> strings) {

        }

        @Override
        public void onRoomConnecting(Room room) {

        }

        @Override
        public void onRoomAutoMatching(Room room) {

        }

        @Override
        public void onPeerInvitedToRoom(Room room, List<String> strings) {

        }



        @Override
        public void onConnectedToRoom(Room room) {

        }

        @Override
        public void onDisconnectedFromRoom(Room room) {

        }

        @Override
        public void onP2PConnected(String s) {

        }

        @Override
        public void onP2PDisconnected(String s) {

        }
    }

    private void startGameOfTruth() {

        Bundle bundle = new Bundle();

        bundle.putBoolean(Constant.INTENT_KEY_IS_MY_TURN, isMyTurn);

        setFragment(Constant.SCREEN_TYPE_ONLINE_GAME , bundle);




    }


    private class FromFragmentBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constant.FILTER_START_QUICK_GAME)) {
                setMyRundom365();
                startQuickGame();
            }else if(intent.getAction().equals(Constant.FILTER_PLAY_WITH_FRIEND)){
                setMyRundom365();
                inviteFriend();
            }else if(intent.getAction().equals(Constant.FILTER_VIEW_INVETATION)){
                viewInitation();

            } else if(intent.getAction().equals(Constant.FILTER_SEND_MY_STROK)){
                String message = intent.getStringExtra(Constant.INTENT_KEY_MY_STROK);
                sendMessageToAllInRoom(message);
            }else if(intent.getAction().equals(Constant.FILTER_IS_GAME_CONTINUE)){
                String message = intent.getStringExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE);
                if(message.equals("yes")) {
                    sendMessageToAllInRoom(message);
                }else {
                    sendMessageToAllInRoom(message);
                    setFragment(Constant.SCREEN_TYPE_ONLINE, null);
                }

            }
        }
    }

    private void setMyRundom365(){
        Random r = new Random();
        myRundom365 = r.nextInt(365);

    }

    private void viewInitation(){


// launch the intent to show the invitation inbox screen
        Intent intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        startActivityForResult(intent, RC_INVITATION_INBOX);
    }
    class XORealTimeMessageReceivedListener implements RealTimeMessageReceivedListener{

        @Override
        public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
            byte[] bytes = realTimeMessage.getMessageData();
            Log.v("XO","get Message");
            String str = null;
            try {
                str = new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.v("XO", e.toString());
            }
            if (isFierstMessage){
                checkHowFirst(str);
                if(!isFierstMessage){
                    startGameOfTruth();
                }
            }else if (str.equals("yes") || str.equals("no")){
                Intent intent =  new Intent(Constant.FF_IS_GAME_CONTINUE_OPPONENT_OPINION);
                intent.putExtra(Constant.INTENT_KEY_IS_GAME_CONTINUE, str );
                sendBroadcast(intent);
            }else {
                Intent intent =  new Intent(Constant.FF_OPONENT_STROK);
                intent.putExtra(Constant.INTENT_KEY_OPONENT_STROK, str );
                sendBroadcast(intent);
            }
        }
    }

    private void checkHowFirst(String oponentMess) {
        int oponentRundom365 =  Integer.valueOf(oponentMess);
        if (oponentRundom365 > myRundom365){
            isMyTurn = false;
            isFierstMessage = false;
        }else if(oponentRundom365 < myRundom365){
            isMyTurn = true;
            isFierstMessage = false;
        }else if(oponentRundom365 == myRundom365){
            setMyRundom365();
            sendMessageToAllInRoom(String.valueOf(myRundom365));
        }
    }

    private void sendMessageToAllInRoom(String messageToRoom){
        byte[] message = messageToRoom.getBytes() ;

        for (Participant p : mParticipants) {

//            if (!p.getParticipantId().equals(mXORoom.getCreatorId())) {
//                Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, message,
//                        mXORoomID, p.getParticipantId());
//            }
        }
        Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(getApiClient(), message,
                        mXORoomID);
    }
}
