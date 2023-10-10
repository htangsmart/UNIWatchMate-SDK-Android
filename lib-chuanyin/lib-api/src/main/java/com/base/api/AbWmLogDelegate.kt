package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.port.log.AbWmLog

internal class AbWmLogDelegate(
    private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmLog() {

    override fun logE(tag: String, msg: String) {
        watchObservable.value!!.wmLog.logE(tag, msg)
    }

    override fun logD(tag: String, msg: String) {
        watchObservable.value!!.wmLog.logD(tag, msg)
    }

    override fun logI(tag: String, msg: String) {
        watchObservable.value!!.wmLog.logE(tag, msg)
    }

    override fun logW(tag: String, msg: String) {
        watchObservable.value!!.wmLog.logW(tag, msg)
    }
}