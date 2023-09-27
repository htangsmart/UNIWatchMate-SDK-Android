package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSedentaryReminder
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingDrinkWaterReminder:AbWmSetting<WmSedentaryReminder>() {

    lateinit var observeEmitter: ObservableEmitter<WmSedentaryReminder>
    lateinit var setEmitter: SingleEmitter<WmSedentaryReminder>
    lateinit var getEmitter: SingleEmitter<WmSedentaryReminder>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSedentaryReminder> {
        return Observable.create(object : ObservableOnSubscribe<WmSedentaryReminder> {
            override fun subscribe(emitter: ObservableEmitter<WmSedentaryReminder>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmSedentaryReminder): Single<WmSedentaryReminder> {
        return Single.create(object : SingleOnSubscribe<WmSedentaryReminder> {
            override fun subscribe(emitter: SingleEmitter<WmSedentaryReminder>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmSedentaryReminder> {
        return Single.create(object : SingleOnSubscribe<WmSedentaryReminder> {
            override fun subscribe(emitter: SingleEmitter<WmSedentaryReminder>) {
                getEmitter = emitter
            }
        })
    }
}