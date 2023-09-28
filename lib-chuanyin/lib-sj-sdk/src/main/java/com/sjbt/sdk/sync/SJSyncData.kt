package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs
import com.sjbt.sdk.SJUniWatch

class SJSyncData(sjUniWatch: SJUniWatch) : AbWmSyncs() {

    override var syncStepData: AbSyncData<List<WmStepData>> = SyncStepData()
    override var syncOxygenData: AbSyncData<List<WmOxygenData>> = SyncOxygenData()
    override var syncCaloriesData: AbSyncData<List<WmCaloriesData>> = SyncCaloriesData()
    override var syncSleepData: AbSyncData<List<WmSleepData>> = SyncSleepData()
    override var syncRealtimeRateData: AbSyncData<List<WmRealtimeRateData>> = SyncRealtimeRateData()
    override var syncHeartRateData: AbSyncData<List<WmHeartRateData>> = SyncHeartRateData()
    override var syncDistanceData: AbSyncData<List<WmDistanceData>> = SyncDistanceData()
    override var syncActivityData: AbSyncData<List<WmActivityData>> = SyncActivityData()
    override var syncSportSummaryData: AbSyncData<List<WmSportSummaryData>> = SyncSportSummaryData()
    override var syncDeviceInfoData: AbSyncData<WmDeviceInfo> = SyncDeviceInfo(sjUniWatch)
    override var syncTodayInfoData: AbSyncData<WmTodayTotalData> = SyncTodayTotalData()
    override val syncBatteryInfo: AbSyncData<WmBatteryInfo> = SyncBatteryInfo(sjUniWatch)

}