package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmRealtimeRateData
import com.base.sdk.port.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncRealtimeRateData : AbSyncData<List<WmRealtimeRateData>>() {

    var is_support: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmRealtimeRateData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmRealtimeRateData>>? = null
    override fun isSupport(): Boolean {
        return is_support
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmRealtimeRateData>> {
        return Single.create { emitter -> activityObserveEmitter = emitter }
    }

    override var observeSyncData: Observable<List<WmRealtimeRateData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }


}