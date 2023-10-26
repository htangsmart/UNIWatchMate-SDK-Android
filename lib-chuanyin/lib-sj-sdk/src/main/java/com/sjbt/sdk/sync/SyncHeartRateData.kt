package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_SPORT_RATE
import com.sjbt.sdk.spp.cmd.URN_SPORT_RATE_REALTIME
import com.sjbt.sdk.spp.cmd.URN_SPORT_RATE_RECORD
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncHeartRateData(val sjUniWatch: SJUniWatch) : AbSyncData<List<WmHeartRateData>>() {
    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmHeartRateData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmHeartRateData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<List<WmHeartRateData>> {

        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(
                CmdHelper.getReadSportSyncData(
                    URN_SPORT_RATE,
                    URN_SPORT_RATE_RECORD
                )
            )
        }
    }

    override var observeSyncData: Observable<List<WmHeartRateData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }


}