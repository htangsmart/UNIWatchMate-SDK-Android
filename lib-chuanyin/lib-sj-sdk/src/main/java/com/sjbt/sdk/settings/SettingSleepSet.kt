package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSleepSettings
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
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
        }
    }

    fun onTimeOut(nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    fun sleepSetBusiness(msgBean: MsgBean) {
        when (msgBean.cmdId.toShort()) {
            CMD_ID_800C -> {
                val sleepOpen = msgBean.payload[0].toInt()
                val startHour = msgBean.payload[1].toInt()
                val startMin = msgBean.payload[2].toInt()
                val endHour = msgBean.payload[3].toInt()
                val endMin = msgBean.payload[4].toInt()

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
                val sleepOpen2 = msgBean.payload[0].toInt()
                val startHour2 = msgBean.payload[1].toInt()
                val startMin2 = msgBean.payload[2].toInt()
                val endHour2 = msgBean.payload[3].toInt()
                val endMin2 = msgBean.payload[4].toInt()

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
                val setSleepResult = msgBean.payload[0].toInt()
                setSleepConfigSuccess(setSleepResult == 1)
            }
        }
    }
}