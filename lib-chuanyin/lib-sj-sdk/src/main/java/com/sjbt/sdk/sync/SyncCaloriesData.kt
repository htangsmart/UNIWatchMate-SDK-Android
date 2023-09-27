package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmCaloriesData
import com.base.sdk.`interface`.sync.AbSyncData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncCaloriesData : AbSyncData<List<WmCaloriesData>>() {
    var is_support: Boolean = true
    var lastSyncTime: Long = 0
    lateinit var activityObserveEmitter: SingleEmitter<List<WmCaloriesData>>
    lateinit var observeChangeEmitter: ObservableEmitter<List<WmCaloriesData>>
    override fun isSupport(): Boolean {
        return is_support
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmCaloriesData>> {
        return Single.create { emitter -> activityObserveEmitter = emitter }
    }

    override var observeSyncData: Observable<List<WmCaloriesData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }

}