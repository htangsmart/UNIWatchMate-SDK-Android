package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.spp.cmd.CMD_ID_800C
import com.sjbt.sdk.spp.cmd.CMD_ID_800D
import com.sjbt.sdk.spp.cmd.CMD_ID_800E
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSleepSet(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSleepSettings>() {

    var observeSleepSettingEmitter: ObservableEmitter<WmSleepSettings>? = null
    var setSleepSettingEmitter: SingleEmitter<WmSleepSettings>? = null
    var getSleepSettingEmitter: SingleEmitter<WmSleepSettings>? = null
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

    private fun setSleepConfigSuccess(result: Boolean) {
        if (result) {
            setSleepSettingEmitter?.onSuccess(wmSleepSettings)
        } else {
            setSleepSettingEmitter?.onSuccess(null)
        }
    }

    private fun observeSleepSetting(wmSleepSettings: WmSleepSettings) {
        this.wmSleepSettings = wmSleepSettings
        observeSleepSettingEmitter?.onNext(wmSleepSettings)
    }

    override fun set(obj: WmSleepSettings): Single<WmSleepSettings> {
        return Single.create { emitter ->
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
    }

    override fun get(): Single<WmSleepSettings> {
        return Single.create { emitter ->
            getSleepSettingEmitter = emitter
            sjUniWatch.sendNormalMsg(CmdHelper.getSleepSetCmd)
        };
    }

    fun sleepSetBusiness(msgBean: MsgBean, msg: ByteArray) {
        when (msgBean.cmdId.toShort()) {
            CMD_ID_800C -> {
                val sleepOpen = msg[16].toInt()
                val startHour = msg[17].toInt()
                val startMin = msg[18].toInt()
                val endHour = msg[19].toInt()
                val endMin = msg[20].toInt()

                val wmSleepSettings = WmSleepSettings(
                    sleepOpen == 1,
                    startHour,
                    startMin,
                    endHour,
                    endMin
                )

                getSleepSettingEmitter?.onSuccess(
                    wmSleepSettings
                )

                observeSleepSetting(
                    wmSleepSettings
                )
            }
            CMD_ID_800D -> {
                val sleepOpen2 = msg[16].toInt()
                val startHour2 = msg[17].toInt()
                val startMin2 = msg[18].toInt()
                val endHour2 = msg[19].toInt()
                val endMin2 = msg[20].toInt()

                observeSleepSetting(
                    WmSleepSettings(
                        sleepOpen2 == 1,
                        startHour2,
                        startMin2,
                        endHour2,
                        endMin2
                    )
                )

                sjUniWatch.sendNormalMsg(CmdHelper.getRespondSuccessCmd(CMD_ID_800D))
            }

            CMD_ID_800E -> {
                val setSleepResult = msg[16].toInt()
                setSleepConfigSuccess(setSleepResult == 1)
            }
        }
    }
}