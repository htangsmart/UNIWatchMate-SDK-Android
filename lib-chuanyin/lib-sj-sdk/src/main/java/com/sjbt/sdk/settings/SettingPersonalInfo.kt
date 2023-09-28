package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmPersonalInfo
import com.base.sdk.port.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingPersonalInfo: AbWmSetting<WmPersonalInfo>(){
    lateinit var observeEmitter: ObservableEmitter<WmPersonalInfo>
    lateinit var setEmitter: SingleEmitter<WmPersonalInfo>
    lateinit var getEmitter: SingleEmitter<WmPersonalInfo>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmPersonalInfo> {
        return Observable.create(object : ObservableOnSubscribe<WmPersonalInfo> {
            override fun subscribe(emitter: ObservableEmitter<WmPersonalInfo>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmPersonalInfo): Single<WmPersonalInfo> {
        return Single.create(object : SingleOnSubscribe<WmPersonalInfo> {
            override fun subscribe(emitter: SingleEmitter<WmPersonalInfo>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmPersonalInfo> {
        return Single.create(object : SingleOnSubscribe<WmPersonalInfo> {
            override fun subscribe(emitter: SingleEmitter<WmPersonalInfo>) {
                getEmitter = emitter
            }
        })
    }

}