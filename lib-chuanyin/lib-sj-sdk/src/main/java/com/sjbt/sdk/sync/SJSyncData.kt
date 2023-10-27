package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs
import com.sjbt.sdk.SJUniWatch

class SJSyncData(val sjUniWatch: SJUniWatch) : AbWmSyncs() {

    override var syncStepData: AbSyncData<List<WmStepData>> = SyncStepData(sjUniWatch)
    override var syncOxygenData: AbSyncData<List<WmOxygenData>> = SyncOxygenData(sjUniWatch)
    override var syncCaloriesData: AbSyncData<List<WmCaloriesData>> = SyncCaloriesData(sjUniWatch)
    override var syncSleepData: AbSyncData<List<WmSleepData>> = SyncSleepData(sjUniWatch)
    override var syncRealtimeRateData: AbSyncData<List<WmRealtimeRateData>> = SyncRealtimeRateData(sjUniWatch)
    override var syncHeartRateData: AbSyncData<List<WmHeartRateData>> = SyncHeartRateData(sjUniWatch)
    override var syncDistanceData: AbSyncData<List<WmDistanceData>> = SyncDistanceData(sjUniWatch)
    override var syncActivityData: AbSyncData<List<WmActivityData>> = SyncActivityData(sjUniWatch)
    override var syncSportSummaryData: AbSyncData<List<WmSportSummaryData>> = SyncSportSummaryData(sjUniWatch)
    override var syncTodayInfoData: AbSyncData<WmTodayTotalData> = SyncTodayTotalData(sjUniWatch)

}