package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmOxygenData
import com.base.sdk.port.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncOxygenData : AbSyncData<List<WmOxygenData>>() {
    var isSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmOxygenData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmOxygenData>>? = null
    override fun isSupport(): Boolean {
        return isSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmOxygenData>> {
        return Single.create { emitter -> activityObserveEmitter = emitter }
    }

    override var observeSyncData: Observable<List<WmOxygenData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }


}