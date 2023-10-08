package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSoundAndHaptic
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSoundAndHaptic(sjUniWatch: SJUniWatch) : AbWmSetting<WmSoundAndHaptic>() {
    lateinit var observeEmitter: ObservableEmitter<WmSoundAndHaptic>
    lateinit var setEmitter: SingleEmitter<WmSoundAndHaptic>
    lateinit var getEmitter: SingleEmitter<WmSoundAndHaptic>

    private var sjUniWatch = sjUniWatch
    private var wmSoundAndHaptic: WmSoundAndHaptic? = null

    fun getWmWistRaise(wmWistRaise: WmSoundAndHaptic) {
        getEmitter?.onSuccess(wmWistRaise)
    }

    fun observeWmWistRaiseChange(wmWistRaise: WmSoundAndHaptic) {
        observeEmitter?.onNext(wmWistRaise)
    }

    fun observeWmWistRaiseChange(type: Int, value: Int) {
        wmSoundAndHaptic?.let {
            when (type) {
                4 -> {
                    setEmitter.onSuccess(it)
                }
            }
        }
    }

    fun setSuccess() {
        wmSoundAndHaptic?.let {
            setEmitter.onSuccess(it)
        }
    }

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

                wmSoundAndHaptic?.let {
                    if (it.isRingtoneEnabled != obj.isRingtoneEnabled) {
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                0.toByte(),
                                if (obj.isRingtoneEnabled) 1 else 0
                            )
                        )
                    } else if (it.isNotificationHaptic != obj.isNotificationHaptic) {
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                1.toByte(),
                                if (obj.isRingtoneEnabled) 1 else 0
                            )
                        )
                    } else if (it.isCrownHapticFeedback != obj.isCrownHapticFeedback) {

                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                2.toByte(),
                                if (obj.isRingtoneEnabled) 1 else 0
                            )
                        )
                    } else if (it.isSystemHapticFeedback != obj.isSystemHapticFeedback) {

                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                3.toByte(),
                                if (obj.isRingtoneEnabled) 1 else 0
                            )
                        )
                    } else if (it.isMuted != obj.isMuted) {
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                5.toByte(),
                                if (obj.isRingtoneEnabled) 1 else 0
                            )
                        )
                    } else {
                        0
                    }
                }

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