package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSleepSet(sjUniWatch: SJUniWatch) : AbWmSetting<WmSleepSettings>() {

    var observeSleepSettingEmitter: ObservableEmitter<WmSleepSettings>? = null
    var setSleepSettingEmitter: SingleEmitter<WmSleepSettings>? = null
    var getSleepSettingEmitter: SingleEmitter<WmSleepSettings>? = null
    private val sjUniWatch = sjUniWatch

    private var wmSleepSettings: WmSleepSettings? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSleepSettings> {
        return Observable.create(object : ObservableOnSubscribe<WmSleepSettings> {
            override fun subscribe(emitter: ObservableEmitter<WmSleepSettings>) {
                observeSleepSettingEmitter = emitter
            }
        })
    }

    fun setSleepConfigSuccess(result: Boolean) {
        if (result) {
            setSleepSettingEmitter?.onSuccess(wmSleepSettings)
        } else {
            setSleepSettingEmitter?.onSuccess(null)
        }
    }

    fun observeSleepSetting(wmSleepSettings: WmSleepSettings) {
        this.wmSleepSettings = wmSleepSettings
        observeSleepSettingEmitter?.onNext(wmSleepSettings)
    }

    override fun set(obj: WmSleepSettings): Single<WmSleepSettings> {
        return Single.create(object : SingleOnSubscribe<WmSleepSettings> {
            override fun subscribe(emitter: SingleEmitter<WmSleepSettings>) {
                setSleepSettingEmitter = emitter

                val status = if (obj.open) {
                    1
                } else {
                    0
                }

                sjUniWatch.sendNormalMsg(
                    CmdHelper.setSleepSetCmd(
                        status.toByte(),
                        obj.startHour.toByte(),
                        obj.startMinute.toByte(),
                        obj.endHour.toByte(),
                        obj.endMinute.toByte()
                    )
                )
            }
        })
    }

    override fun get(): Single<WmSleepSettings> {
        return Single.create(object : SingleOnSubscribe<WmSleepSettings> {
            override fun subscribe(emitter: SingleEmitter<WmSleepSettings>) {
                getSleepSettingEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.getSleepSetCmd)
            }
        });
    }
}