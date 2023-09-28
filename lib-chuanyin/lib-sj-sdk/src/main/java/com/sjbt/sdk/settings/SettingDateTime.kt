package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

/**
 * 设置时间功能
 */
class SettingDateTime(sjUniWatch: SJUniWatch) : AbWmSetting<WmDateTime>() {

    lateinit var observeEmitter: ObservableEmitter<WmDateTime>
    lateinit var setEmitter: SingleEmitter<WmDateTime>
    lateinit var getEmitter: SingleEmitter<WmDateTime>

    var sjUniWatch = sjUniWatch

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmDateTime> {
        return Observable.create(object : ObservableOnSubscribe<WmDateTime> {
            override fun subscribe(emitter: ObservableEmitter<WmDateTime>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmDateTime): Single<WmDateTime> {
        return Single.create(object : SingleOnSubscribe<WmDateTime> {
            override fun subscribe(emitter: SingleEmitter<WmDateTime>) {
                setEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.syncTimeCmd)
            }
        })
    }

    override fun get(): Single<WmDateTime> {
        return Single.create(object : SingleOnSubscribe<WmDateTime> {
            override fun subscribe(emitter: SingleEmitter<WmDateTime>) {
                getEmitter = emitter
            }
        })
    }

}
