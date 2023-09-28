package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSleepSet(sjUniWatch: SJUniWatch) : AbWmSetting<WmSleepSettings>() {

    lateinit var observeSleepSettingEmitter: ObservableEmitter<WmSleepSettings>
    lateinit var setSleepSettingEmitter: SingleEmitter<WmSleepSettings>
    lateinit var getSleepSettingEmitter: SingleEmitter<WmSleepSettings>
    private val sjUniWatch = sjUniWatch

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