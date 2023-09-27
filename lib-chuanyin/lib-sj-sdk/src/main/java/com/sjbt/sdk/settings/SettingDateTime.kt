package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

/**
 * 设置时间功能
 */
class SettingDateTime : AbWmSetting<WmDateTime>() {

    lateinit var observeEmitter: ObservableEmitter<WmDateTime>
    lateinit var setEmitter: SingleEmitter<WmDateTime>
    lateinit var getEmitter: SingleEmitter<WmDateTime>

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
