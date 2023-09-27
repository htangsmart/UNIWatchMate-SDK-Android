package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSoundAndHaptic
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingSoundAndHaptic : AbWmSetting<WmSoundAndHaptic>() {
    lateinit var observeEmitter: ObservableEmitter<WmSoundAndHaptic>
    lateinit var setEmitter: SingleEmitter<WmSoundAndHaptic>
    lateinit var getEmitter: SingleEmitter<WmSoundAndHaptic>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSoundAndHaptic> {
        return Observable.create(object : ObservableOnSubscribe<WmSoundAndHaptic> {
            override fun subscribe(emitter: ObservableEmitter<WmSoundAndHaptic>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmSoundAndHaptic): Single<WmSoundAndHaptic> {
        return Single.create(object : SingleOnSubscribe<WmSoundAndHaptic> {
            override fun subscribe(emitter: SingleEmitter<WmSoundAndHaptic>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmSoundAndHaptic> {
        return Single.create(object : SingleOnSubscribe<WmSoundAndHaptic> {
            override fun subscribe(emitter: SingleEmitter<WmSoundAndHaptic>) {
                getEmitter = emitter
            }
        })
    }

}