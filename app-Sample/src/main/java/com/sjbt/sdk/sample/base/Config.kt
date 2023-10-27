package com.sjbt.sdk.sample.base;

import android.os.Environment;

import com.sjbt.sdk.sample.MyApplication;


public class Config {

    public static final String BASE_PATH = MyApplication.Companion.getInstance().getExternalCacheDir().getAbsolutePath();
    public static final String APP_PATH = BASE_PATH + "/biu/";
    public static final String APP_VIDEO_PATH = APP_PATH + "crop_video/";
    public static final String APP_CROP_PIC_PATH = APP_VIDEO_PATH + "crop_pic/";
    public static final String APP_DOWNLOAD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/biu2us/";
    public static final String APP_DOWNLOAD_PATH = APP_PATH + "/up/";
    public static final String APP_DEBUG_EXPORT_FILE = APP_PATH + "/debug/";
    public static final String APP_DIAL_PATH = APP_PATH + "/dial/";
    public static final String APP_DIAL_THUMP_PATH = APP_PATH + "/dial/thump/";

    public final static int BT_REQUEST_CODE = 110;
    public final static int FT_REQUEST_CODE = BT_REQUEST_CODE + 1;
    public final static int BT_REQUEST_CODE_SETTING = FT_REQUEST_CODE + 1;
    public final static int PERMISSION_REQUEST_CODE = 1101;
    public final static int PERMISSION_INSTALL_APK = PERMISSION_REQUEST_CODE + 1;

    public final static int CLICK_CANCEL = 0;
    public final static int CLICK_OK = 1;
    public final static int CLICK_FINISH = 2;
    public final static int CLICK_RETRY = 3;
    public final static int CLICK_INSTALL = 4;
    public final static int CLICK_SET = 5;
    public final static int CLICK_REMOVE = 6;
    public final static int CLICK_UPDATE = 7;

    public final static int CHECK_VERSION_APK = 2;
    public final static int CHECK_VERSION_UP = 1;

    public final static int UPDATE_STEP_1 = 1;
    public final static int UPDATE_STEP_2 = 2;
    public final static int UPDATE_STEP_3 = 3;

    public final static int BATTERY_PERCENT_5 = 5;
    public final static int BATTERY_PERCENT_15 = 15;
    public final static int BATTERY_PERCENT_25 = 25;
    public final static int BATTERY_PERCENT_35 = 35;
    public final static int BATTERY_PERCENT_45 = 45;
    public final static int BATTERY_PERCENT_55 = 55;
    public final static int BATTERY_PERCENT_65 = 65;
    public final static int BATTERY_PERCENT_75 = 75;
    public final static int BATTERY_PERCENT_85 = 85;
    public final static int BATTERY_PERCENT_95 = 95;
    public final static int BATTERY_PERCENT_100 = 100;

    public static final int CHOOSE_FILE_CODE = 100;
    public static final int REQUEST_CODE_SCAN = 0X01;
    public static final int REQUEST_CODE_DEVICE = 0X02;
    public static final int REQUEST_INIT_INFO = 0X03;
    public static final int REQUEST_UPDATE_HEALTH = 0X04;

    public static final int SET_PWD = 1, VERIFY_PWD = 2, CLOSE_PWD = 3;

    public static final String[] NOTIFY_MSG_APP_PK = {"com.hihonor.mms", "com.android.mms", "com.android.mms.service", "com.samsung.android.messaging"};

    public static final String TAG_QQ = "qq";
    public static final String TAG_WECHAT = "wechat";
    public static final String TAG_WHATS = "whats";

    public static final String STEP = "steps", RATE = "rate", BLOOD_OX = "blood_ox", BLOOD_SUGAR = "blood_sugar", BLOOD_PRESS = "blood_press", SLEEP_DEEP = "sleep_deep", SLEEP_LIGHT = "sleep_light", SLEEP_WAKE = "sleep_wake";
    public static final String AVG_MAX_MIN = "AVG,MAX,MIN";
    public static final String AVG = "AVG";
    public static final String TIME_FORMAT = "yyyy/MM/dd";

    public static final String BIND_TYPE_RELATION = "relation";
    public static final String MATE_WATCH_APP = "metawatchapp";

    public static final int PWD_LEN_MIN = 6;
    public static final int PWD_LEN_MAX = 16;

    public static final String HEIGHT = "height";
    public static final String WEIGHT = "weight";
    public static final String BIRTHDAY = "birthday";
    public static final String GENDER = "gender";
    public static final String HEAT_GOAL = "heat_goal";
    public static final String STEP_GOAL = "step_goal";
    public static final String DIS_GOAL = "dis_goal";

    public static final long INTERVAL_HOUR = 60 * 60 * 1000l;
    public static final long INTERVAL_DAY = 24 * 60 * 60 * 1000l;
    public static final long INTERVAL_MONTH = 1;

    public static final int TIME_DAY = 1, TIME_WEEK = 2, TIME_MONTH = 3, TIME_SIX_MONTH = 4, TIME_YEAR = 5;

    public static final int SCAN_START = 4;
    public static final int SCAN_STOP = SCAN_START + 1;
    public static final int SCAN_FAIL = SCAN_STOP + 1;

    //    public static final String WEB_BASE_URL = "https://metawatchapp.com";
//    public static final String WEB_BASE_URL = "https://account-dev.aimetawatch.com";
    public static final String WEB_BASE_URL = "https://aimetawatch.com";

    public static final String
            SERVICE_URL = WEB_BASE_URL + "/user-terms?theme_color=dark",
            PRIVACY_URL = WEB_BASE_URL + "/privacy-policy?theme_color=dark",
            DELETE_ACCOUNT_URL = WEB_BASE_URL + "/account-delete-v2?theme_color=dark",
            MODIFY_PWD_URL = WEB_BASE_URL + "/edit-password-new?theme_color=dark",
            BACK_GUIDE_URL = WEB_BASE_URL + "/operational-guidance?theme_color=dark";

    public static final int CONNECT_TYPE_ADD_DEVICE = 1;
    public static final int CONNECT_TYPE_RE_DEVICE = 2;

    public static final int DIAL_BUILT_IN = 1;
    public static final int DIAL_INSTALLED = 2;

    public static final String PIC_GIF = ".gif", PIC_WEBP = ".webp";

    public static final int DIAL_PAGE_SIZE = 15;

    public static final int ALARM_SET_CLOCK = 10001;

    public static final String ACTION_LOGIN = "LOGIN", ACTION_REGISTER = "REGISTER", ACTION_CHANGE_PHONE = "CHANGE_PHONE";

    public static final String SCAN_MAC_ADDRESS = "SCAN_MAC_ADDRESS";
    public static final String SCAN_NAME = "SCAN_NAME";
    public static final String SCAN_RESULT = "SCAN_RESULT";

    public static final String FILE_TYPE_MP3 = "";
    public static final String FILE_TYPE_TXT = "txt";
    public static final String FILE_TYPE_VIDEO = "video";

    public static final int ACTION_TYPE_WEATHER = 0;
    public static final int ACTION_TYPE_SPORT = 1;
    public static final int ACTION_TYPE_RATE = 2;
    public static final int ACTION_TYPE_CAMERA = 3;
    public static final int ACTION_TYPE_NOTIFY = 4;
    public static final int ACTION_TYPE_ALARM = 5;
    public static final int ACTION_TYPE_MUSIC = 6;
    public static final int ACTION_TYPE_CONTACT = 7;
    public static final int ACTION_TYPE_SEARCH_DEVICE = 8;
    public static final int ACTION_TYPE_SEARCH_PHONE = 9;
    public static final int ACTION_TYPE_APP_VIEWS = 10;
    public static final int ACTION_TYPE_SETTING_RING = 11;
    public static final int ACTION_TYPE_SETTING_NOTIFY = 12;
    public static final int ACTION_TYPE_SETTING_WATCH = 13;
    public static final int ACTION_TYPE_SETTING_SYSTEM = 14;
    public static final int ACTION_TYPE_SETTING_ARM = 15;
    public static final int ACTION_TYPE_BLOOD_OX = 16;
    public static final int ACTION_TYPE_BLOOD_SUGAR = 17;
    public static final int ACTION_TYPE_BLOOD_PRESS = 18;
    public static final int ACTION_TYPE_SLEEP = 19;
    public static final int ACTION_TYPE_EBOOK = 20;

    public static final int ACTION_TYPE_DIAL = 1001;
    public static final int ACTION_TYPE_OTA = 1002;

    public static final int ACTION_TOP = 1;
    public static final int ACTION_SPORT = 2;
    public static final int ACTION_WEATHER = 3;
    public static final String MAX_NOT_SUPPORT_UP_VERSION = "2.1.91";


}
