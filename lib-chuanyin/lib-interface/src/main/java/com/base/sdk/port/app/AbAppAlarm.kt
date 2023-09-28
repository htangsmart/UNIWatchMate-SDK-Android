package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-闹钟
 */
abstract class AbAppAlarm : IWmSupport {
    /**
     * syncAlarmList 同步闹钟列表
     */
    abstract var syncAlarmList: Observable<List<WmAlarm>>

    /**
     * addAlarm 添加闹钟
     */
    abstract fun addAlarm(alarm: WmAlarm): Single<WmAlarm>

    /**
     * deleteAlarm 删除闹钟
     */
    abstract fun deleteAlarm(alarm: WmAlarm): Single<WmAlarm>

    /**
     * updateAlarm 更新闹钟
     */
    abstract fun updateAlarm(alarm: WmAlarm): Single<WmAlarm>
}