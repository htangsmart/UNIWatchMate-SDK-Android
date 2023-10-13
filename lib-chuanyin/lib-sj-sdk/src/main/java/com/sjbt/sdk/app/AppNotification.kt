package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.port.app.AbAppNotification
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.core.SingleOnSubscribe

class AppNotification(sjUniWatch: SJUniWatch) : AbAppNotification() {
    val sjUniWatch = sjUniWatch
    var sendNotificationEmitter: SingleEmitter<Boolean>? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun sendNotification(notification: WmNotification): Single<Boolean> {
        return Single.create(object : SingleOnSubscribe<Boolean> {
            override fun subscribe(emitter: SingleEmitter<Boolean>) {
                sendNotificationEmitter = emitter

                sjUniWatch.sendNormalMsg(CmdHelper.getNotificationCmd(notification))
            }
        })
    }
}