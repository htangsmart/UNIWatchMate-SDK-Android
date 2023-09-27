package com.sjbt.sdk.log

import com.base.sdk.`interface`.log.WmLog
import com.sjbt.sdk.TAG_SJ

object SJLog {

    open var logEnable: Boolean = false

    fun logBt(tag: String, state: String) {
        if (logEnable) {
            WmLog.d(tag, state)
        }
    }

    fun logE(tag: String, state: String) {
        if (logEnable) {
            WmLog.e(tag, state)
        }
    }

    fun logSendMsg(msg: String) {
        if (logEnable) {
            WmLog.d(TAG_SJ + "MSG-SEND：", msg)
        }
    }

    fun logReadMsg(msg: String) {
        if (logEnable) {
            WmLog.d(TAG_SJ + "MSG-READ：", msg)
        }
    }
}