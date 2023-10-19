package com.sjbt.sdk.sync

import com.base.sdk.entity.data.*
import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs
import com.sjbt.sdk.SJUniWatch

class SJSyncData(sjUniWatch: SJUniWatch) : AbWmSyncs() {

    override var syncStepData = SyncStepData()
    override var syncOxygenData = SyncOxygenData()
    override var syncCaloriesData = SyncCaloriesData()
    override var syncSleepData = SyncSleepData()
    override var syncRealtimeRateData = SyncRealtimeRateData()
    override var syncHeartRateData = SyncHeartRateData()
    override var syncDistanceData = SyncDistanceData()
    override var syncActivityData = SyncActivityData()
    override var syncSportSummaryData = SyncSportSummaryData()
    override var syncDeviceInfoData = SyncDeviceInfo(sjUniWatch)
    override var syncTodayInfoData = SyncTodayTotalData()
    override val syncBatteryInfo = SyncBatteryInfo(sjUniWatch)

}