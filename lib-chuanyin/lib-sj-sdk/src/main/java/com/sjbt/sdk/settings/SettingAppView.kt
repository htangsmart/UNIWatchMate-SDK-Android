package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmAppView
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingAppView(val sjUniWatch: SJUniWatch) : AbWmSetting<WmAppView>() {
    var observeEmitter: ObservableEmitter<WmAppView>? = null
    var setEmitter: SingleEmitter<WmAppView>? = null
    var getEmitter: SingleEmitter<WmAppView>? = null
    var isActionSupport: Boolean = false
    private var mAppView: WmAppView? = null

    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun observeChange(): Observable<WmAppView> {
        return Observable.create(object : ObservableOnSubscribe<WmAppView> {
            override fun subscribe(emitter: ObservableEmitter<WmAppView>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(appView: WmAppView): Single<WmAppView> {
        mAppView = appView
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

    fun setAppViewResult(isSuccess: Boolean) {
        if (isSuccess) {
            getEmitter?.onSuccess(mAppView)
        } else {
            getEmitter?.onError(Throwable("set fail"))
        }
    }
}