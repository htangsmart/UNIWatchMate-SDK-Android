package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmWistRaise
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingWistRaise(sjUniWatch: SJUniWatch) : AbWmSetting<WmWistRaise>() {
    lateinit var observeEmitter: ObservableEmitter<WmWistRaise>
    lateinit var setEmitter: SingleEmitter<WmWistRaise>
    lateinit var getEmitter: SingleEmitter<WmWistRaise>

    private var sjUniWatch = sjUniWatch

    private var mWmWistRaise: WmWistRaise? = null

    fun getWmWistRaise(wmWistRaise: WmWistRaise) {
        getEmitter?.onSuccess(wmWistRaise)
    }

    fun observeWmWistRaiseChange(wmWistRaise: WmWistRaise) {
        observeEmitter?.onNext(wmWistRaise)
    }

    fun observeWmWistRaiseChange(type: Int, value: Int) {
        mWmWistRaise?.let {
            when (type) {
                4 -> {
                    it.isScreenWakeEnabled = value == 1
                    setEmitter.onSuccess(it)
                }
            }
        }
    }

    fun setSuccess() {
        mWmWistRaise?.let {
            setEmitter.onSuccess(it)
        }
    }

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
                sjUniWatch.sendNormalMsg(
                    CmdHelper.getSetDeviceRingStateCmd(
                        4,
                        if (obj.isScreenWakeEnabled) 1 else 0
                    )
                )
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