package com.base.sdk.`interface`.log

import android.util.Log

object WmLog {

    val TAG: String = "UNI_WATCH_SDK_LOG_"

    var logEnable = false

    fun e( tag: String, msg: String) {
        if (logEnable) {
            Log.e(TAG + tag, msg)
        }
    }

    fun i( tag: String, msg: String) {
        if (logEnable) {
            Log.i(TAG + tag, msg)
        }
    }

    fun d( tag: String, msg: String) {
        if (logEnable) {
            Log.d(TAG + tag, msg)
        }
    }
}