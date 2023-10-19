package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.port.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncHeartRateData : AbSyncData<List<WmHeartRateData>>() {
    var isSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmHeartRateData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmHeartRateData>>? = null
    override fun isSupport(): Boolean {
        return isSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmHeartRateData>> {
        return Single.create { emitter -> activityObserveEmitter = emitter }
    }

    override var observeSyncData: Observable<List<WmHeartRateData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }


}