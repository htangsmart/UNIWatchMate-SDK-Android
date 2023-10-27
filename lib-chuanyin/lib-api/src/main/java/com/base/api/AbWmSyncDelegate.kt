package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.entity.data.*
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.entity.settings.*
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs

internal class AbWmSyncDelegate(
   private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmSyncs() {

    override val syncStepData: AbSyncData<List<WmStepData>>
        get() = watchObservable.value!!.wmSync.syncStepData
    override val syncOxygenData: AbSyncData<List<WmOxygenData>>
        get() = watchObservable.value!!.wmSync.syncOxygenData
    override val syncCaloriesData: AbSyncData<List<WmCaloriesData>>
        get() = watchObservable.value!!.wmSync.syncCaloriesData
    override val syncSleepData: AbSyncData<List<WmSleepData>>
        get() = watchObservable.value!!.wmSync.syncSleepData
    override val syncRealtimeRateData: AbSyncData<List<WmRealtimeRateData>>
        get() = watchObservable.value!!.wmSync.syncRealtimeRateData
    override val syncHeartRateData: AbSyncData<List<WmHeartRateData>>
        get() = watchObservable.value!!.wmSync.syncHeartRateData
    override val syncDistanceData: AbSyncData<List<WmDistanceData>>
        get() = watchObservable.value!!.wmSync.syncDistanceData
    override val syncActivityData: AbSyncData<List<WmActivityData>>
        get() = watchObservable.value!!.wmSync.syncActivityData
    override val syncSportSummaryData: AbSyncData<List<WmSportSummaryData>>
        get() = watchObservable.value!!.wmSync.syncSportSummaryData
    override val syncTodayInfoData: AbSyncData<WmTodayTotalData>
        get() = watchObservable.value!!.wmSync.syncTodayInfoData
}