package com.sjbt.sdk.app

import com.base.sdk.entity.apps.AlarmRepeatOption
import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.port.app.AbAppAlarm
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.charset.StandardCharsets

class AppAlarm(val sjUniWatch: SJUniWatch) : AbAppAlarm() {

    private var _isSupport: Boolean = true
    private var alarmListEmitter: ObservableEmitter<List<WmAlarm>>? = null
    private var addAlarmEmitter: SingleEmitter<WmAlarm>? = null
    private var deleteAlarmEmitter: SingleEmitter<Boolean>? = null
    private var updateAlarmEmitter: SingleEmitter<WmAlarm>? = null

    private var mAlarm: WmAlarm? = null

    private val TAG = "AppAlarm"

    override fun isSupport(): Boolean {
        return _isSupport
    }

    override var syncAlarmList: Observable<List<WmAlarm>> = Observable.create {
        alarmListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadAlarmListCmd())
    }

    private fun deleteSuccess(success: Boolean) {
        deleteAlarmEmitter?.onSuccess(
            success
        )
    }

    private fun updateSuccess(success: Boolean) {
        mAlarm?.let {
            updateAlarmEmitter?.onSuccess(
                if (success) {
                    it
                } else {
                    null
                }
            )
        }
    }

    private fun syncAlarmListSuccess(alarmList: List<WmAlarm>) {
        alarmList?.let {
            alarmListEmitter?.onNext(it)
        }
    }

    override fun addAlarm(alarm: WmAlarm): Single<WmAlarm> {
        mAlarm = alarm
        return Single.create {
            addAlarmEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteAddAlarmCmd(alarm))
        }
    }

    override fun deleteAlarm(alarms: List<WmAlarm>): Single<Boolean> {
        return Single.create { emitter ->
            deleteAlarmEmitter = emitter
            val itArray = alarms.map { it.alarmId.toByte() }
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteDeleteAlarmCmd(itArray))
        }
    }

    override fun updateAlarm(alarm: WmAlarm): Single<WmAlarm> {
        mAlarm = alarm
        return Single.create {
            updateAlarmEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteModifyAlarmCmd(alarm))
        }
    }

    fun alarmBusiness(nodeData: NodeData) {
        when (nodeData.urn[2]) {

            URN_APP_ALARM_ADD -> {
                val alarmId = nodeData.data[0].toInt()
                sjUniWatch.wmLog.logD(TAG, "add alarm result:$alarmId")
                mAlarm?.let {
                    it.alarmId = alarmId
                    addAlarmEmitter?.onSuccess(
                        it
                    )
                }
            }

            URN_APP_ALARM_DELETE -> {
                deleteSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }

            URN_APP_ALARM_LIST -> {
                val alarmList = mutableListOf<WmAlarm>()
                val count = nodeData.dataLen / 25
                sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")

                if (count > 0) {
                    for (i in 0 until count) {

                        val alarmArray = nodeData.data.copyOfRange(i * 25, i * 25 + 25)
                        val id = alarmArray[0].toInt()
                        val nameArray = alarmArray.copyOfRange(1, 21).takeWhile { it.toInt() != 0 }
                            .toByteArray()
                        val name = String(nameArray, StandardCharsets.UTF_8)
                        sjUniWatch.wmLog.logD(TAG, "name:$name")

                        val hour = alarmArray[21].toInt()
                        val minute = alarmArray[22].toInt()
                        val repeatOptions = alarmArray[23].toInt()
                        val isEnable = alarmArray[24].toInt()

                        val wmAlarm =
                            WmAlarm(
                                name,
                                hour,
                                minute,
                                AlarmRepeatOption.fromValue(repeatOptions)
                            )

                        wmAlarm.isOn = isEnable == 1
                        wmAlarm.alarmId = id
                        sjUniWatch.wmLog.logD(TAG, "Alarm INFO:$wmAlarm ")

                        if (id != 0) {
                            alarmList.add(wmAlarm)
                        }
                    }
                }

                syncAlarmListSuccess(alarmList)
            }

            URN_APP_ALARM_UPDATE -> {
                updateSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }
        }
    }
}