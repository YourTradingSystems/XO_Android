package com.mobilez365.xo.util;

/**
 * Created by BruSD on 06.05.2014.
 */
public class Constant {
    //Therd party utils keys
    public final static String START_AD_MOVBI_PUBLISHER_ID = "5375ec5b5bfbc40f00000000";
    public final static String GA_ACCOUNT_ID ="UA-51080238-1";

    //Screens for Analytics
    public final static String SCREEN_MAIN ="Main Screen";
    public final static String SCREEN_SINGLE_PLAYER ="Single Player";
    public final static String SCREEN_TWO_PLAYER ="Two Players";
    public final static String SCREEN_ONLINE_GAME_CHOICE ="Choice Online Game";
    public final static String SCREEN_QUICK_GAME ="Quick Game";
    public final static String SCREEN_INVITE_FRIEND ="Invite Friend";
    public final static String SCREEN_VIEW_INVITE ="View Invite";
    public final static String SCREEN_LEADER_BOARD ="Leader Board";
    public final static String SCREEN_ACHIEVEMENTS="Achievements";
    public final static String SCREEN_SETTINGS="Settings Screen";
    public final static String SCREEN_ABOUT="About Screen";
    public final static String SCREEN_GAME="Game Online Screen";


    // AILevel
    public static final int AI_EASY = 0;
    public static final int AI_MEDIUM = 1;
    public static final int AI_HARD = 2;

    //Screen Type
    public static final int SCREEN_TYPE_ONE_PLAYER = 0;
    public static final int SCREEN_TYPE_TWO_PLAYER= 1;
    public static final int SCREEN_TYPE_ONLINE = 2;
    public static final int SCREEN_TYPE_ONLINE_GAME = 3;
    public static final int SCREEN_AI_GAME = 4;

    public static final int CLICK_SOUND = 1;
    public static final int WIN_SOUND = 2;
    public static final int LOSE_SOUND = 3;
    public static final int GOES_X__SOUND = 4;
    public static final int GOES_O_SOUND = 5;

    public static final String KEY_SOUND_EFFECTS    = "sound_effects_setting";
    public static final String KEY_BACKGROUND_MUSIC = "background_music_setting";
    public static final String KEY_PUSH             = "push_setting";
    public static final String KEY_ANALYTICS        = "analytics_setting";
    public static final String PREF_NAME            = "com.mobilez365.xo.appSettings";

    //Recivers Type for Activity

    public static final String FILTER_VIEW_EASY = "com.mobilez365.xo.easy";
    public static final String FILTER_VIEW_MEDIUM = "com.mobilez365.xo.medium";
    public static final String FILTER_VIEW_HARD = "com.mobilez365.xo.hard";

    public static final String FILTER_VIEW_INVETATION = "com.mobilez365.xo.viewInvite";
    public static final String FILTER_PLAY_WITH_FRIEND = "com.mobilez365.xo.playWithFriend";
    public static final String FILTER_START_QUICK_GAME = "com.mobilez365.xo.startQuickGame";

    public static final String FILTER_SEND_MY_STROK = "com.mobilez365.xo.SEND_MY_STROK";

    public static final String FILTER_IS_GAME_CONTINUE = "com.mobilez365.xo.FILTER_IS_GAME_CONTINUE";

    //Online Game Fragment Filter

    public static final String FF_OPONENT_STROK = "com.mobilez365.xo.OPONENT_STROK";
    public static final String FF_IS_GAME_CONTINUE_OPPONENT_OPINION= "com.mobilez365.xo.FF_IS_GAME_CONTINUE_OPPONENT_OPINION";
    public static final String FF_OPPONENT_LEFT_GAME= "com.mobilez365.xo.opponent_left_game";

    //Intent Keys
    public static final String INTENT_KEY_MY_STROK = "com.mobilez365.xo.MyStrok";
    public static final String INTENT_KEY_OPONENT_STROK = "com.mobilez365.xo.OPONENT_STROK";
    public static final String INTENT_KEY_IS_MY_TURN= "com.mobilez365.xo.IS_MY_TURN";
    public static final String INTENT_KEY_IS_GAME_CONTINUE= "com.mobilez365.xo.INTENT_KEY_IS_GAME_CONTINUE";
    public static final String INTENT_KEY_AI_LEVEL= "com.mobilez365.xo.INTENT_KEY_AI_LEVEL";

    //Simbol Identificator
    public static final int MY_SYMBOLE_O = 0;
    public static final int MY_SYMBOLE_X = 1;


}
