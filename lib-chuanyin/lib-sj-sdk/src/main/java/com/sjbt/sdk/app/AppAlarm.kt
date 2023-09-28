package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.port.app.AbAppAlarm
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single

class AppAlarm : AbAppAlarm() {

    private var _isSupport: Boolean = false
    lateinit var alarmListEmitter: ObservableEmitter<List<WmAlarm>>

    override fun isSupport(): Boolean {
        return _isSupport
    }

    override var syncAlarmList: Observable<List<WmAlarm>> = Observable.create {
        alarmListEmitter = it
    }

    override fun addAlarm(alarm: WmAlarm): Single<WmAlarm> {
        TODO("Not yet implemented")
    }

    override fun deleteAlarm(alarm: WmAlarm): Single<WmAlarm> {
        TODO("Not yet implemented")
    }

    override fun updateAlarm(alarm: WmAlarm): Single<WmAlarm> {
        TODO("Not yet implemented")
    }
}