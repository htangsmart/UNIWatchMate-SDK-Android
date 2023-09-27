package com.sjbt.sdk.utils;

import android.util.Log;

public class LogUtils {
    public static final String TAG_COMMON = ">>>>>>>>common";
    public static final String TAG_BLUETOOTH = ">>>>>>>>bluetooth";
    public static final String TAG_BLE = ">>>>>>>>ble";

    public static void logCommon(String log) {
//        if (AppStatus.isDebugVersion(BaseApplication.getInstance())) {
        Log.e(TAG_COMMON, log);
//        }
    }

    public static void logBlueTooth(String log) {
//        if (AppStatus.isDebugVersion(BaseApplication.getInstance())) {
        Log.e(TAG_BLUETOOTH, log);
//        }
    }

    public static void logBle(String log) {
//        if (AppStatus.isDebugVersion(BaseApplication.getInstance())) {
        Log.e(TAG_BLE, log);
//        }
    }


}
