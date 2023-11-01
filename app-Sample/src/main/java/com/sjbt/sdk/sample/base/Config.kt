package com.sjbt.sdk.sample.base

import android.os.Environment
import com.sjbt.sdk.sample.MyApplication.Companion.instance

object Config {
    val BASE_PATH = instance.externalCacheDir!!.absolutePath
    val APP_PATH = BASE_PATH + "/biu/"
    val APP_VIDEO_PATH = APP_PATH + "crop_video/"
    val APP_CROP_PIC_PATH = APP_VIDEO_PATH + "crop_pic/"
    val APP_DOWNLOAD =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/biu2us/"
    val APP_DOWNLOAD_PATH = APP_PATH + "/up/"
    val APP_DEBUG_EXPORT_FILE = APP_PATH + "/debug/"
    val APP_DIAL_PATH = APP_PATH + "/dial/"
    val APP_DIAL_THUMP_PATH = APP_PATH + "/dial/thump/"
    const val BT_REQUEST_CODE = 110
    const val FT_REQUEST_CODE = BT_REQUEST_CODE + 1
    const val BT_REQUEST_CODE_SETTING = FT_REQUEST_CODE + 1
    const val PERMISSION_REQUEST_CODE = 1101
    const val PERMISSION_INSTALL_APK = PERMISSION_REQUEST_CODE + 1
    const val CLICK_CANCEL = 0
    const val CLICK_OK = 1
    const val CLICK_FINISH = 2
    const val CLICK_RETRY = 3
    const val CLICK_INSTALL = 4
    const val CLICK_SET = 5
    const val CLICK_REMOVE = 6
    const val CLICK_UPDATE = 7
    const val CHECK_VERSION_APK = 2
    const val CHECK_VERSION_UP = 1
    const val UPDATE_STEP_1 = 1
    const val UPDATE_STEP_2 = 2
    const val UPDATE_STEP_3 = 3
    const val BATTERY_PERCENT_5 = 5
    const val BATTERY_PERCENT_15 = 15
    const val BATTERY_PERCENT_25 = 25
    const val BATTERY_PERCENT_35 = 35
    const val BATTERY_PERCENT_45 = 45
    const val BATTERY_PERCENT_55 = 55
    const val BATTERY_PERCENT_65 = 65
    const val BATTERY_PERCENT_75 = 75
    const val BATTERY_PERCENT_85 = 85
    const val BATTERY_PERCENT_95 = 95
    const val BATTERY_PERCENT_100 = 100
    const val CHOOSE_FILE_CODE = 100
    const val REQUEST_CODE_SCAN = 0X01
    const val REQUEST_CODE_DEVICE = 0X02
    const val REQUEST_INIT_INFO = 0X03
    const val REQUEST_UPDATE_HEALTH = 0X04
    const val SET_PWD = 1
    const val VERIFY_PWD = 2
    const val CLOSE_PWD = 3
    val NOTIFY_MSG_APP_PK = arrayOf(
        "com.hihonor.mms",
        "com.android.mms",
        "com.android.mms.service",
        "com.samsung.android.messaging"
    )
    const val TAG_QQ = "qq"
    const val TAG_WECHAT = "wechat"
    const val TAG_WHATS = "whats"
    const val STEP = "steps"
    const val RATE = "rate"
    const val BLOOD_OX = "blood_ox"
    const val BLOOD_SUGAR = "blood_sugar"
    const val BLOOD_PRESS = "blood_press"
    const val SLEEP_DEEP = "sleep_deep"
    const val SLEEP_LIGHT = "sleep_light"
    const val SLEEP_WAKE = "sleep_wake"
    const val AVG_MAX_MIN = "AVG,MAX,MIN"
    const val AVG = "AVG"
    const val TIME_FORMAT = "yyyy/MM/dd"
    const val BIND_TYPE_RELATION = "relation"
    const val MATE_WATCH_APP = "metawatchapp"
    const val PWD_LEN_MIN = 6
    const val PWD_LEN_MAX = 16
    const val HEIGHT = "height"
    const val WEIGHT = "weight"
    const val BIRTHDAY = "birthday"
    const val GENDER = "gender"
    const val HEAT_GOAL = "heat_goal"
    const val STEP_GOAL = "step_goal"
    const val DIS_GOAL = "dis_goal"
    const val INTERVAL_HOUR = 60 * 60 * 1000L
    const val INTERVAL_DAY = 24 * 60 * 60 * 1000L
    const val INTERVAL_MONTH: Long = 1
    const val TIME_DAY = 1
    const val TIME_WEEK = 2
    const val TIME_MONTH = 3
    const val TIME_SIX_MONTH = 4
    const val TIME_YEAR = 5
    const val SCAN_START = 4
    const val SCAN_STOP = SCAN_START + 1
    const val SCAN_FAIL = SCAN_STOP + 1

    //    public static final String WEB_BASE_URL = "https://metawatchapp.com";
    //    public static final String WEB_BASE_URL = "https://account-dev.aimetawatch.com";
    const val WEB_BASE_URL = "https://aimetawatch.com"
    const val SERVICE_URL = WEB_BASE_URL + "/user-terms?theme_color=dark"
    const val PRIVACY_URL = WEB_BASE_URL + "/privacy-policy?theme_color=dark"
    const val DELETE_ACCOUNT_URL = WEB_BASE_URL + "/account-delete-v2?theme_color=dark"
    const val MODIFY_PWD_URL = WEB_BASE_URL + "/edit-password-new?theme_color=dark"
    const val BACK_GUIDE_URL = WEB_BASE_URL + "/operational-guidance?theme_color=dark"
    const val CONNECT_TYPE_ADD_DEVICE = 1
    const val CONNECT_TYPE_RE_DEVICE = 2
    const val DIAL_BUILT_IN = 1
    const val DIAL_INSTALLED = 2
    const val PIC_GIF = ".gif"
    const val PIC_WEBP = ".webp"
    const val DIAL_PAGE_SIZE = 15
    const val ALARM_SET_CLOCK = 10001
    const val ACTION_LOGIN = "LOGIN"
    const val ACTION_REGISTER = "REGISTER"
    const val ACTION_CHANGE_PHONE = "CHANGE_PHONE"
    const val SCAN_MAC_ADDRESS = "SCAN_MAC_ADDRESS"
    const val SCAN_NAME = "SCAN_NAME"
    const val SCAN_RESULT = "SCAN_RESULT"
    const val FILE_TYPE_MP3 = ""
    const val FILE_TYPE_TXT = "txt"
    const val FILE_TYPE_VIDEO = "video"
    const val ACTION_TYPE_WEATHER = 0
    const val ACTION_TYPE_SPORT = 1
    const val ACTION_TYPE_RATE = 2
    const val ACTION_TYPE_CAMERA = 3
    const val ACTION_TYPE_NOTIFY = 4
    const val ACTION_TYPE_ALARM = 5
    const val ACTION_TYPE_MUSIC = 6
    const val ACTION_TYPE_CONTACT = 7
    const val ACTION_TYPE_SEARCH_DEVICE = 8
    const val ACTION_TYPE_SEARCH_PHONE = 9
    const val ACTION_TYPE_APP_VIEWS = 10
    const val ACTION_TYPE_SETTING_RING = 11
    const val ACTION_TYPE_SETTING_NOTIFY = 12
    const val ACTION_TYPE_SETTING_WATCH = 13
    const val ACTION_TYPE_SETTING_SYSTEM = 14
    const val ACTION_TYPE_SETTING_ARM = 15
    const val ACTION_TYPE_BLOOD_OX = 16
    const val ACTION_TYPE_BLOOD_SUGAR = 17
    const val ACTION_TYPE_BLOOD_PRESS = 18
    const val ACTION_TYPE_SLEEP = 19
    const val ACTION_TYPE_EBOOK = 20
    const val ACTION_TYPE_DIAL = 1001
    const val ACTION_TYPE_OTA = 1002
    const val ACTION_TOP = 1
    const val ACTION_SPORT = 2
    const val ACTION_WEATHER = 3
    const val MAX_NOT_SUPPORT_UP_VERSION = "2.1.91"
}