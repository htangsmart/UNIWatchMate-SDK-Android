package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSoundAndHaptic
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSoundAndHaptic(sjUniWatch: SJUniWatch) : AbWmSetting<WmSoundAndHaptic>() {
    var observeEmitter: ObservableEmitter<WmSoundAndHaptic>? = null
    var setEmitter: SingleEmitter<WmSoundAndHaptic>? = null
    var getEmitter: SingleEmitter<WmSoundAndHaptic>? = null

    private var sjUniWatch = sjUniWatch
    private var wmSoundAndHaptic: WmSoundAndHaptic? = null
    private var backWmSoundAndHaptic = WmSoundAndHaptic();

    fun getWmWistRaise(wmWistRaise: WmSoundAndHaptic) {
        backWmSoundAndHaptic = wmWistRaise
        wmSoundAndHaptic = WmSoundAndHaptic(
            wmWistRaise.isRingtoneEnabled,
            wmWistRaise.isNotificationHaptic,
            wmWistRaise.isCrownHapticFeedback,
            wmWistRaise.isSystemHapticFeedback,
            wmWistRaise.isMuted
        )
        getEmitter?.onSuccess(wmWistRaise)
    }

    fun observeWmWistRaiseChange(wmWistRaise: WmSoundAndHaptic) {
        backWmSoundAndHaptic = wmWistRaise
        wmSoundAndHaptic = WmSoundAndHaptic(
            wmWistRaise.isRingtoneEnabled,
            wmWistRaise.isNotificationHaptic,
            wmWistRaise.isCrownHapticFeedback,
            wmWistRaise.isSystemHapticFeedback,
            wmWistRaise.isMuted
        )
        observeEmitter?.onNext(wmWistRaise)
    }

    fun observeWmWistRaiseChange(type: Int, value: Int) {
        wmSoundAndHaptic?.let {
            when (type) {
                0 -> {
                    it.isRingtoneEnabled = value == 1
                    backWmSoundAndHaptic.isRingtoneEnabled = value == 1
                }

                1 -> {
                    it.isNotificationHaptic = value == 1
                    backWmSoundAndHaptic.isNotificationHaptic = value == 1
                }

                2 -> {
                    it.isCrownHapticFeedback = value == 1
                    backWmSoundAndHaptic.isCrownHapticFeedback = value == 1
                }

                3 -> {
                    it.isSystemHapticFeedback = value == 1
                    backWmSoundAndHaptic.isSystemHapticFeedback = value == 1
                }

                4 -> {
                    it.isMuted = value == 1
                    backWmSoundAndHaptic.isMuted = value == 1
                }
            }

            setEmitter?.onSuccess(backWmSoundAndHaptic)

        }
    }

    fun setSuccess() {
        wmSoundAndHaptic?.isRingtoneEnabled = backWmSoundAndHaptic.isRingtoneEnabled
        wmSoundAndHaptic?.isNotificationHaptic = backWmSoundAndHaptic.isNotificationHaptic
        wmSoundAndHaptic?.isMuted = backWmSoundAndHaptic.isMuted
        wmSoundAndHaptic?.isCrownHapticFeedback = backWmSoundAndHaptic.isCrownHapticFeedback
        wmSoundAndHaptic?.isSystemHapticFeedback = backWmSoundAndHaptic.isSystemHapticFeedback

        setEmitter?.onSuccess(backWmSoundAndHaptic)
        observeEmitter?.onNext(backWmSoundAndHaptic)
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
                backWmSoundAndHaptic = obj

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
                                if (obj.isNotificationHaptic) 1 else 0
                            )
                        )
                    } else if (it.isCrownHapticFeedback != obj.isCrownHapticFeedback) {

                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                2.toByte(),
                                if (obj.isCrownHapticFeedback) 1 else 0
                            )
                        )
                    } else if (it.isSystemHapticFeedback != obj.isSystemHapticFeedback) {

                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                3.toByte(),
                                if (obj.isSystemHapticFeedback) 1 else 0
                            )
                        )
                    } else if (it.isMuted != obj.isMuted) {
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getSetDeviceRingStateCmd(
                                5.toByte(),
                                if (obj.isMuted) 1 else 0
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
                sjUniWatch.sendNormalMsg(CmdHelper.deviceRingStateCmd)
            }
        })
    }

}