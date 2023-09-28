package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmAppView
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingAppView(sjUniWatch: SJUniWatch) : AbWmSetting<WmAppView>() {
    lateinit var observeEmitter: ObservableEmitter<WmAppView>
    lateinit var setEmitter: SingleEmitter<WmAppView>
    lateinit var getEmitter: SingleEmitter<WmAppView>
    val sjUniWatch = sjUniWatch

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

    override fun set(appView: WmAppView): Single<WmAppView> {
        return Single.create(object : SingleOnSubscribe<WmAppView> {
            override fun subscribe(emitter: SingleEmitter<WmAppView>) {
                setEmitter = emitter
                appView.appViewList.forEach {
                    if (it.status == 1) {
                        sjUniWatch.sendNormalMsg(CmdHelper.setAppViewCmd(it.id.toByte()))
                    }
                }
            }
        })
    }

    override fun get(): Single<WmAppView> {
        return Single.create(object : SingleOnSubscribe<WmAppView> {
            override fun subscribe(emitter: SingleEmitter<WmAppView>) {
                getEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.appViewList)
            }
        })
    }

}