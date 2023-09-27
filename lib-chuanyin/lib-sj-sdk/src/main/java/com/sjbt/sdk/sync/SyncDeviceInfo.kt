package com.sjbt.sdk.sync

import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.`interface`.sync.AbSyncData
import io.reactivex.rxjava3.core.*

class SyncDeviceInfo : AbSyncData<WmDeviceInfo>() {

    var deviceEmitter: SingleEmitter<WmDeviceInfo>? = null
    var observeDeviceEmitter: ObservableEmitter<WmDeviceInfo>? = null
    var lastSyncTime: Long = 0

    override fun isSupport(): Boolean {
        return true
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<WmDeviceInfo> {
        return Single.create{
            deviceEmitter = it
        }
    }

    override var observeSyncData: Observable<WmDeviceInfo> =
        Observable.create { emitter -> observeDeviceEmitter = emitter }


}