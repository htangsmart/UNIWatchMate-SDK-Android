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
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class AppAlarm(val sjUniWatch: SJUniWatch) : AbAppAlarm() {

    private var _isSupport: Boolean = true
    private var alarmListEmitter: ObservableEmitter<List<WmAlarm>>? = null
    private var addAlarmEmitter: SingleEmitter<WmAlarm>? = null
    private var deleteAlarmEmitter: SingleEmitter<WmAlarm>? = null
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

    private fun addSuccess(success: Boolean) {
        mAlarm?.let {
            addAlarmEmitter?.onSuccess(
                if (success) {
                    it
                } else {
                    null
                }
            )
        }
    }

    private fun deleteSuccess(success: Boolean) {
        mAlarm?.let {
            deleteAlarmEmitter?.onSuccess(
                if (success) {
                    it
                } else {
                    null
                }
            )
        }
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

    override fun deleteAlarm(alarm: WmAlarm): Single<WmAlarm> {
        mAlarm = alarm
        return Single.create {
            deleteAlarmEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteDeleteAlarmCmd(alarm))
        }
    }

    override fun updateAlarm(alarm: WmAlarm): Single<WmAlarm> {
        mAlarm = alarm
        return Single.create {
            updateAlarmEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteModifyAlarmCmd(alarm))
        }
    }

    fun alarmBusiness(it: NodeData) {
        when (it.urn[2]) {

            URN_APP_ALARM_ADD -> {
                val result = it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                sjUniWatch.wmLog.logD(TAG, "add alarm result:$result")
                addSuccess(result)
            }

            URN_APP_ALARM_DELETE -> {
                deleteSuccess(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }

            URN_APP_ALARM_LIST -> {
                val alarmList = mutableListOf<WmAlarm>()
                val count = it.dataLen / 25
                sjUniWatch.wmLog.logD(TAG, "Alarm Count：$count")

                if (count > 0) {
                    val byteBuffer = ByteBuffer.wrap(it.data)
                    for (i in 0..count) {
                        val id = byteBuffer.get()
                        val nameArray =
                            byteBuffer.array().copyOfRange(1, 21).takeWhile { it > 0 }.toByteArray()
                        val name = String(nameArray, StandardCharsets.UTF_8)
                        sjUniWatch.wmLog.logD(TAG, "Alarm Name：$name")

                        val hour = byteBuffer.get()
                        val minute = byteBuffer.get()
                        val repeatOptions = byteBuffer.get()
                        val isEnable = byteBuffer.get()

                        val wmAlarm =
                            WmAlarm(
                                name,
                                hour.toInt(),
                                minute.toInt(),
                                AlarmRepeatOption.fromValue(repeatOptions.toInt())
                            )
                        wmAlarm.isOn = isEnable.toInt() == 1
                        wmAlarm.alarmId = id.toInt()
                        alarmList.add(wmAlarm)
                    }
                }

                syncAlarmListSuccess(alarmList)
            }

            URN_APP_ALARM_UPDATE -> {
                updateSuccess(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }
        }
    }
}