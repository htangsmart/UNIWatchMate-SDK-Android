package com.sjbt.sdk.log

import android.util.Log
import com.base.sdk.port.log.AbWmLog
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.TAG_SJ

class SJLog(val sjUniWatch: SJUniWatch) : AbWmLog() {

    val sdkDebugLogEnable = true

    override fun logE(tag: String, msg: String) {
        if (sjUniWatch.sdkLogEnable) {
            Log.e(TAG_SJ + tag, msg)
        }
    }

    override fun logD(tag: String, msg: String) {
        if (sdkDebugLogEnable && sjUniWatch.sdkLogEnable) {
            Log.d(TAG_SJ + tag, msg)
        }
    }

    override fun logI(tag: String, msg: String) {
        if (sdkDebugLogEnable && sjUniWatch.sdkLogEnable) {
            Log.i(TAG_SJ + tag, msg)
        }
    }

    override fun logW(tag: String, msg: String) {
        if (sdkDebugLogEnable && sjUniWatch.sdkLogEnable) {
            Log.w(TAG_SJ + tag, msg)
        }
    }
}