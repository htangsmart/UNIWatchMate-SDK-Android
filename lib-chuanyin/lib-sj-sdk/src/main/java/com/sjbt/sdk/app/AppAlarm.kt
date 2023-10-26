package com.sjbt.sdk.app

import android.text.TextUtils
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
    private var observeAlarmListEmitter: ObservableEmitter<List<WmAlarm>>? = null
    private var updateAlarmEmitter: SingleEmitter<Boolean>? = null

    private val TAG = "AppAlarm"

    override fun isSupport(): Boolean {
        return _isSupport
    }

    override fun updateAlarmList(alarms: List<WmAlarm>): Single<Boolean> {
        return Single.create {
            updateAlarmEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteUpdateAlarmCmd(alarms))
        }
    }

    override var observeAlarmList: Observable<List<WmAlarm>> = Observable.create {
        observeAlarmListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadAlarmListCmd())
    }

    private fun syncAlarmListSuccess(alarmList: List<WmAlarm>) {
        alarmList?.let {
            observeAlarmListEmitter?.onNext(it)
        }
    }
    fun onTimeOut(nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    fun alarmBusiness(nodeData: NodeData) {
        when (nodeData.urn[2]) {

            URN_APP_ALARM_LIST -> {

                if (nodeData.data.size == 1) {
                    updateAlarmEmitter?.let {
                        it.onSuccess(nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                    }
                } else {
                    val alarmList = mutableListOf<WmAlarm>()
                    val count = nodeData.dataLen / 25
                    sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")

                    if (count > 0) {
                        for (i in 0 until count) {

                            val alarmArray = nodeData.data.copyOfRange(i * 25, i * 25 + 25)
                            val id = alarmArray[0].toInt()
                            val nameArray =
                                alarmArray.copyOfRange(1, 21).takeWhile { it.toInt() != 0 }
                                    .toByteArray()
                            val name = String(nameArray, StandardCharsets.UTF_8)
                            sjUniWatch.wmLog.logD(TAG, "id$id name:$name")

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

//                            alarmIdStates.forEach {
//                                it.used = it.value== id
//                            }

                            sjUniWatch.wmLog.logD(TAG, "Alarm INFO:$wmAlarm ")

                            if (id != 0) {
                                alarmList.add(wmAlarm)
                            }
                        }
                    }

                    syncAlarmListSuccess(alarmList)
                }
            }

        }
    }



}