package com.base.sdk.port.log

abstract class AbWmLog {

    abstract fun logE(tag: String, msg: String)
    abstract fun logD(tag: String, msg: String)
    abstract fun logI(tag: String, msg: String)
    abstract fun logW(tag: String, msg: String)
}