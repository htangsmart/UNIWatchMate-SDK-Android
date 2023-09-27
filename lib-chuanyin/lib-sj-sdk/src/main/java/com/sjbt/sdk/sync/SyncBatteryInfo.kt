package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.data.WmTodayTotalData
import com.base.sdk.`interface`.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class SyncBatteryInfo:AbSyncData<WmBatteryInfo>() {

    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override fun latestSyncTime(): Long {
        TODO("Not yet implemented")
    }

    override fun syncData(startTime: Long): Single<WmBatteryInfo> {
        TODO("Not yet implemented")
    }

    override var observeSyncData: Observable<WmBatteryInfo>
        get() = TODO("Not yet implemented")
        set(value) {}

}