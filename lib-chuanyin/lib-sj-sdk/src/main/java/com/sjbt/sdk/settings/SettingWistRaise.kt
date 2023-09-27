package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmWistRaise
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingWistRaise: AbWmSetting<WmWistRaise>()  {
    lateinit var observeEmitter: ObservableEmitter<WmWistRaise>
    lateinit var setEmitter: SingleEmitter<WmWistRaise>
    lateinit var getEmitter: SingleEmitter<WmWistRaise>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmWistRaise> {
        return Observable.create(object : ObservableOnSubscribe<WmWistRaise> {
            override fun subscribe(emitter: ObservableEmitter<WmWistRaise>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmWistRaise): Single<WmWistRaise> {
        return Single.create(object : SingleOnSubscribe<WmWistRaise> {
            override fun subscribe(emitter: SingleEmitter<WmWistRaise>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmWistRaise> {
        return Single.create(object : SingleOnSubscribe<WmWistRaise> {
            override fun subscribe(emitter: SingleEmitter<WmWistRaise>) {
                getEmitter = emitter
            }
        })
    }

}