package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmAppView
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingAppView : AbWmSetting<WmAppView>() {
    lateinit var observeEmitter: ObservableEmitter<WmAppView>
    lateinit var setEmitter: SingleEmitter<WmAppView>
    lateinit var getEmitter: SingleEmitter<WmAppView>

    var is_support: Boolean = false

    override fun isSupport(): Boolean {
       return is_support
    }

    override fun observeChange(): Observable<WmAppView> {
        return Observable.create(object : ObservableOnSubscribe<WmAppView> {
            override fun subscribe(emitter: ObservableEmitter<WmAppView>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmAppView): Single<WmAppView> {
        return Single.create(object : SingleOnSubscribe<WmAppView> {
            override fun subscribe(emitter: SingleEmitter<WmAppView>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmAppView> {
        return Single.create(object : SingleOnSubscribe<WmAppView> {
            override fun subscribe(emitter: SingleEmitter<WmAppView>) {
                getEmitter = emitter
            }
        })
    }

}