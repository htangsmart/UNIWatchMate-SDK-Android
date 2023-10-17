package com.sjbt.sdk.app

import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.port.app.AbAppDateTime
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class AppDateTime(val sjUniWatch: SJUniWatch) : AbAppDateTime() {
    var setEmitter: SingleEmitter<Boolean>? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun setDateTime(dateTime: WmDateTime?): Single<Boolean> {
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendNormalMsg(CmdHelper.syncTimeCmd)
        }
    }
}