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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
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
import com.mobilez365.xo.fragments.AiFragment;
import com.mobilez365.xo.fragments.SelectOnePlayerFragment;
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
    private Fragment fragment;
    private String mMyPartisipientId;
    private String mInvitedPartisipientId;
    private boolean isFierstMessage = true;

    private boolean isGameOfTruth = false; //taras  if true = game of truth else quick game

    private int myRandom365;
    private boolean isMyTurn;
    //CALBACKS
    private XORoomUpdateListener xoRoomUpdateListener ;
    private XORealTimeMessageReceivedListener xoRealTimeMessageReceivedListener;
    private XORoomStatusUpdateListener xoRoomStatusUpdateListener;
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
    public void onBackPressed() {
        if (fragment instanceof OnlineGameFragment) {
            Games.RealTimeMultiplayer.leave(getApiClient(), xoRoomUpdateListener, mXORoomID);
            setFragment(Constant.SCREEN_TYPE_ONLINE, null);
        }else if (fragment instanceof AiFragment) {
            setFragment(Constant.SCREEN_TYPE_ONE_PLAYER, null);
        }else if(fragment instanceof TwoPlayerFragment) {
            super.onBackPressed();
        }else if(fragment instanceof SelectOnePlayerFragment) {
            super.onBackPressed();
        }else if(fragment instanceof SelectOnlineGameFragment) {
            super.onBackPressed();
        }

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
        filter.addAction(Constant.FILTER_VIEW_EASY);
        filter.addAction(Constant.FILTER_VIEW_MEDIUM);
        filter.addAction(Constant.FILTER_VIEW_HARD);

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

        switch (fragmentType) {
            case Constant.SCREEN_TYPE_ONE_PLAYER: {

                fragment = new SelectOnePlayerFragment();
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
            case Constant.SCREEN_AI_GAME: {
                fragment = new AiFragment();
                break;
            }
            default: {
                return null;
            }
        }
        return fragment;
    }

    private void startQuickGame() {

        mMyPartisipientId=null;//taras
        mInvitedPartisipientId=null;//taras
        isGameOfTruth=false;//taras

        xoRoomUpdateListener = new XORoomUpdateListener();
        xoRealTimeMessageReceivedListener = new XORealTimeMessageReceivedListener();
        xoRoomStatusUpdateListener = new XORoomStatusUpdateListener();
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

        xoRoomUpdateListener = new XORoomUpdateListener();
        xoRealTimeMessageReceivedListener = new XORealTimeMessageReceivedListener();
        xoRoomStatusUpdateListener = new XORoomStatusUpdateListener();
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

            mMyPartisipientId = invitation.getInviter().getParticipantId();//taras

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
        return RoomConfig.builder( xoRoomUpdateListener)
                .setMessageReceivedListener( xoRealTimeMessageReceivedListener)
                .setRoomStatusUpdateListener(xoRoomStatusUpdateListener);
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

                //taras s
                for (Participant p : mParticipants)
                {
                    if(!p.getParticipantId().equals(mMyPartisipientId))
                    {
                        mInvitedPartisipientId = p.getParticipantId();
                    }
                }
                //taras e

                mXORoom = room;

                sendMessageToAllInRoom(String.valueOf(myRandom365));


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
            sendBroadcast(new Intent(Constant.FF_OPPONENT_LEFT_GAME));
            Games.RealTimeMultiplayer.leave(getApiClient(), xoRoomUpdateListener, mXORoomID);
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
        bundle.putString(Constant.INTENT_KEY_WAS_INVITED,mInvitedPartisipientId);//taras
        bundle.putString(Constant.INTENT_KEY_INVITOR,mMyPartisipientId);//taras

        bundle.putBoolean(Constant.INTENT_KEY_IS_GAME_OF_TRUTH,isGameOfTruth);
        setFragment(Constant.SCREEN_TYPE_ONLINE_GAME, bundle);

    }


    private class FromFragmentBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constant.FILTER_START_QUICK_GAME)) {
                setMyRandom365();
                isGameOfTruth=false;
                startQuickGame();
            }else if(intent.getAction().equals(Constant.FILTER_PLAY_WITH_FRIEND)){
                setMyRandom365();
                isGameOfTruth=true;
                inviteFriend();
            }else if(intent.getAction().equals(Constant.FILTER_VIEW_INVETATION)){
                viewInvitation();
                isGameOfTruth=true;

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
            else if (intent.getAction().equals(Constant.FILTER_VIEW_EASY)) {

                Bundle bundle = new Bundle();
                bundle.putInt(Constant.INTENT_KEY_AI_LEVEL, Constant.AI_EASY);
                setFragment(Constant.SCREEN_AI_GAME, bundle);
            }
            else if (intent.getAction().equals(Constant.FILTER_VIEW_MEDIUM)) {

                Bundle bundle = new Bundle();
                bundle.putInt(Constant.INTENT_KEY_AI_LEVEL, Constant.AI_MEDIUM);
                setFragment(Constant.SCREEN_AI_GAME, bundle);
            }
            else if (intent.getAction().equals(Constant.FILTER_VIEW_HARD)) {

                Bundle bundle = new Bundle();
                bundle.putInt(Constant.INTENT_KEY_AI_LEVEL, Constant.AI_HARD);
                setFragment(Constant.SCREEN_AI_GAME, bundle);
            }
        }
    }

    private void setMyRandom365(){
        Random r = new Random();
        myRandom365 = r.nextInt(365);
    }

    private void viewInvitation(){
        // launch the intent to show the invitation inbox screen
        Intent intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    class XORealTimeMessageReceivedListener implements RealTimeMessageReceivedListener{

        @Override
        public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
            byte[] bytes = realTimeMessage.getMessageData();

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

    private void checkHowFirst(String opponentMess) {
        int opponentRandom365 =  Integer.valueOf(opponentMess);
        if (opponentRandom365 > myRandom365){
            isMyTurn = false;
            isFierstMessage = false;
        }else if(opponentRandom365 < myRandom365){
            isMyTurn = true;
            isFierstMessage = false;
        }else if(opponentRandom365 == myRandom365){
            setMyRandom365();
            sendMessageToAllInRoom(String.valueOf(myRandom365));
        }
    }

    private void sendMessageToAllInRoom(String messageToRoom){
        byte[] message = messageToRoom.getBytes() ;

        for (Participant p : mParticipants) {
            if (p.getPlayer() == null){
                Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, message,
                        mXORoomID, p.getParticipantId());
            }else if (! p.getPlayer().getPlayerId().equals(Games.Players.getCurrentPlayerId(getApiClient()) )) {
                Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, message,
                        mXORoomID, p.getParticipantId());
            }

        }
    }
}
