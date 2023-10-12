package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.port.app.AbAppAlarm
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class AppAlarm(val sjUniWatch: SJUniWatch) : AbAppAlarm() {

    private var _isSupport: Boolean = true
    private lateinit var alarmListEmitter: ObservableEmitter<List<WmAlarm>>
    private lateinit var addAlarmEmitter: SingleEmitter<WmAlarm>
    private lateinit var deleteAlarmEmitter: SingleEmitter<WmAlarm>
    private lateinit var updateAlarmEmitter: SingleEmitter<WmAlarm>

    private var mAlarm: WmAlarm? = null

    override fun isSupport(): Boolean {
        return _isSupport
    }

    override var syncAlarmList: Observable<List<WmAlarm>> = Observable.create {
        alarmListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadAlarmListCmd())
    }

    private fun addSuccess(success: Boolean) {
        mAlarm?.let {
            addAlarmEmitter?.onSuccess(if(success){it}else{null})
        }
    }

    private fun deleteSuccess(success: Boolean) {
        mAlarm?.let {
            deleteAlarmEmitter?.onSuccess(if(success){it}else{null})
        }
    }

    private fun updateSuccess(success: Boolean) {
        mAlarm?.let {
            updateAlarmEmitter?.onSuccess(if(success){it}else{null})
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
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteAddAlarmCmd(alarm))
        }
    }

    fun alarmBusiness(it: NodeData) {
        when (it.urn[2]) {

            URN_APP_ALARM_ADD -> {
                addSuccess(it.data[0].toInt() == 1)
            }

            URN_APP_ALARM_DELETE -> {
                deleteSuccess(it.data[0].toInt() == 1)
            }

            URN_APP_ALARM_LIST -> {
                val alarmList = mutableListOf<WmAlarm>()
                syncAlarmListSuccess(alarmList)
            }

            URN_APP_ALARM_UPDATE -> {
                updateSuccess(it.data[0].toInt() == 1)
            }
        }
    }
}