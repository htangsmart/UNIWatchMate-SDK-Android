package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingHeartRateAlerts: AbWmSetting<WmHeartRateAlerts>() {
    lateinit var observeEmitter: ObservableEmitter<WmHeartRateAlerts>
    lateinit var setEmitter: SingleEmitter<WmHeartRateAlerts>
    lateinit var getEmitter: SingleEmitter<WmHeartRateAlerts>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmHeartRateAlerts> {
        return Observable.create(object : ObservableOnSubscribe<WmHeartRateAlerts> {
            override fun subscribe(emitter: ObservableEmitter<WmHeartRateAlerts>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmHeartRateAlerts): Single<WmHeartRateAlerts> {
        return Single.create(object : SingleOnSubscribe<WmHeartRateAlerts> {
            override fun subscribe(emitter: SingleEmitter<WmHeartRateAlerts>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmHeartRateAlerts> {
        return Single.create(object : SingleOnSubscribe<WmHeartRateAlerts> {
            override fun subscribe(emitter: SingleEmitter<WmHeartRateAlerts>) {
                getEmitter = emitter
            }
        })
    }
}