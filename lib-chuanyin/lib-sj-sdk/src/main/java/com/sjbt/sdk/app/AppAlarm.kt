package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.port.app.AbAppAlarm
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
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

    var mAlarm: WmAlarm? = null

    override fun isSupport(): Boolean {
        return _isSupport
    }

    override var syncAlarmList: Observable<List<WmAlarm>> = Observable.create {
        alarmListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadAlarmListCmd())
    }

    fun addSuccess() {
        mAlarm?.let {
            addAlarmEmitter?.onSuccess(it)
        }
    }

    fun deleteSuccess() {
        mAlarm?.let {
            deleteAlarmEmitter?.onSuccess(it)
        }
    }

    fun updateSuccess() {
        mAlarm?.let {
            updateAlarmEmitter?.onSuccess(it)
        }
    }

    fun syncAlarmListSuccess(alarmList: List<WmAlarm>) {

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
}