package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

/**
 * 设置时间功能
 */
class SettingDateTime(val sjUniWatch: SJUniWatch) : AbWmSetting<WmDateTime>() {
    var observeEmitter: ObservableEmitter<WmDateTime>? = null
    var setEmitter: SingleEmitter<WmDateTime>? = null
    var getEmitter: SingleEmitter<WmDateTime>? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmDateTime> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmDateTime): Single<WmDateTime> {
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendNormalMsg(CmdHelper.syncTimeCmd)
        }
    }

    override fun get(): Single<WmDateTime> {
        return Single.create { emitter ->
            getEmitter = emitter
            getEmitter?.onError(RuntimeException("time not support get!"))
        }
    }

}
