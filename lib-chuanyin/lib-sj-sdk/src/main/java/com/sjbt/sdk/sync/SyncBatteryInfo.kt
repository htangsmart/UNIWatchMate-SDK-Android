package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.`interface`.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncBatteryInfo:AbSyncData<WmBatteryInfo>() {

    var batteryEmitter: SingleEmitter<WmBatteryInfo>? = null
    var observeBatteryEmitter: ObservableEmitter<WmBatteryInfo>? = null
    var lastSyncTime: Long = 0


    override fun isSupport(): Boolean {
        return true
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<WmBatteryInfo> {
        return Single.create{
            batteryEmitter = it
        }
    }

    override var observeSyncData: Observable<WmBatteryInfo> =
        Observable.create { emitter -> observeBatteryEmitter = emitter }


}