package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmActivityData
import com.base.sdk.port.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncActivityData : AbSyncData<List<WmActivityData>>() {

    private var is_support: Boolean = true
    var lastSyncTime: Long = 0
    lateinit var activityObserveEmitter: SingleEmitter<List<WmActivityData>>
    lateinit var observeChangeEmitter: ObservableEmitter<List<WmActivityData>>
    override fun isSupport(): Boolean {
        return is_support
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmActivityData>> {
        return Single.create { emitter -> activityObserveEmitter = emitter }
    }

    override var observeSyncData: Observable<List<WmActivityData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}