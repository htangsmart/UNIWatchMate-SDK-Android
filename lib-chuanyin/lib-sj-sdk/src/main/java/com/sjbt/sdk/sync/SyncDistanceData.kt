package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmDistanceData
import com.base.sdk.port.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncDistanceData : AbSyncData<List<WmDistanceData>>() {

    var is_support: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmDistanceData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmDistanceData>>? = null
    override fun isSupport(): Boolean {
        return is_support
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmDistanceData>> {
        return Single.create { emitter -> activityObserveEmitter = emitter }
    }

    override var observeSyncData: Observable<List<WmDistanceData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }


}