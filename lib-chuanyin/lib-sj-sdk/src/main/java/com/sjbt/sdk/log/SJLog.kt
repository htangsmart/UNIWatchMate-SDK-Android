package com.sjbt.sdk.log

import com.base.sdk.port.log.AbWmLog
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.TAG_SJ
import timber.log.Timber

class SJLog(val sjUniWatch: SJUniWatch) : AbWmLog() {

    override fun logE(tag: String, msg: String) {
        if (sjUniWatch.sdkLogEnable) {
            Timber.tag(TAG_SJ + tag).e(msg)
        }
    }

    override fun logD(tag: String, msg: String) {
        if (sjUniWatch.sdkLogEnable) {
            Timber.tag(TAG_SJ + tag).d(msg)
        }
    }

    override fun logI(tag: String, msg: String) {
        if (sjUniWatch.sdkLogEnable) {
            Timber.tag(TAG_SJ + tag).i(msg)
        }
    }

    override fun logW(tag: String, msg: String) {
        if (sjUniWatch.sdkLogEnable) {
            Timber.tag(TAG_SJ + tag).w(msg)
        }
    }

    fun logSendMsg(msg: String) {
        if (sjUniWatch.sdkLogEnable) {
            Timber.tag(TAG_SJ+"MSG:").w(msg)
        }
    }

}