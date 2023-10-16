package com.sjbt.sdk.sample.base;

import java.util.UUID;

public class BTConfig {

    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final static int BT_REQUEST_CODE = 110;
    public final static int FT_REQUEST_CODE = BT_REQUEST_CODE + 1;
    public final static int BT_REQUEST_CODE_SETTING = FT_REQUEST_CODE + 1;

    public static final int CONNECT_STATE_DISCONNECTED = 0;
    public static final int CONNECT_STATE_CONNECTED = 2;
    public static final int CONNECT_STATE_CONNECT_FAIL = 3;

    public static final int CONNECT_FAIL_NO_RESPOND = 1;
    public static final int CONNECT_FAIL_BAD_ADDRESS =CONNECT_FAIL_NO_RESPOND + 1;
    public static final int CONNECT_FAIL_BT_DISABLE =CONNECT_FAIL_BAD_ADDRESS + 1;
    public static final int CONNECT_FAIL_VERIFY_TIMEOUT =CONNECT_FAIL_BT_DISABLE + 1;
    public static final int CONNECT_FAIL_OTHER =CONNECT_FAIL_VERIFY_TIMEOUT + 1;

    public static final int CONNECT_RETRY_COUNT = 2;

    public static final byte FILE_TRANSFER_JPG = 4;
    public static final byte FILE_TRANSFER_BIN = 3;
    public static final byte FILE_TRANSFER_UP = 2;
    public static final byte FILE_TRANSFER_UPEX = 5;


    public static final int FAIL_TYPE_BT_DISCONNECT = 400;
    public static final int FAIL_TYPE_FILE_NOT_EXIST = FAIL_TYPE_BT_DISCONNECT + 1;
    public static final int FAIL_TYPE_FILE_EMPTY = FAIL_TYPE_FILE_NOT_EXIST + 1;
    public static final int FAIL_TYPE_FILE_EXCEPTION = FAIL_TYPE_FILE_EMPTY + 1;
    public static final int FAIL_TYPE_USER_CANCEL = FAIL_TYPE_FILE_EXCEPTION + 1;
    public static final int FAIL_TYPE_DEVICE_CANCEL = FAIL_TYPE_USER_CANCEL + 1;
    public static final int FAIL_TYPE_TIMEOUT = FAIL_TYPE_DEVICE_CANCEL + 1;
    public static final int FAIL_TYPE_ERROR = FAIL_TYPE_TIMEOUT + 1;

    public static final int MAX_RETRY_COUNT = 5;
    public static final int MSG_INTERVAL = 15;
    public static final int MSG_INTERVAL_FRAME = 15;
    public static final int MSG_INTERVAL_SLOW = 40;

    public static final int OTA_STEP_MAIN_PROCESS = 200;
    public static final int OTA_STEP_VICE_PROCESS = 201;

    public static final String UP = "up", UP_EX = "upex",JPG="jpg";

}
