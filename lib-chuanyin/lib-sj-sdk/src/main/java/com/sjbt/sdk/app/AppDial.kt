package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmDial
import com.base.sdk.port.app.AbAppDial
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class AppDial(sjUniWatch: SJUniWatch) : AbAppDial() {
    val sjUniWatch = sjUniWatch
    lateinit var syncDialListEmitter: ObservableEmitter<List<WmDial>>
    lateinit var deleteEmitter: SingleEmitter<WmDial>

    override fun isSupport(): Boolean {
        return true
    }

    override fun syncDialList(index: Byte): Observable<List<WmDial>> {
        return Observable.create(object : ObservableOnSubscribe<List<WmDial>> {
            override fun subscribe(emitter: ObservableEmitter<List<WmDial>>) {
                syncDialListEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.getDialListCmd(index))
            }
        })
    }

    override fun deleteDial(dialItem: WmDial): Single<WmDial> {
        return Single.create(object : SingleOnSubscribe<WmDial> {
            override fun subscribe(emitter: SingleEmitter<WmDial>) {
                deleteEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.getDialActionCmd(2, dialItem.id))
            }
        })
    }
}