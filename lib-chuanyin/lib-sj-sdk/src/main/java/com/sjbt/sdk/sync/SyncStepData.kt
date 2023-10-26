package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmStepData
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper.getReadSportSyncData
import com.sjbt.sdk.spp.cmd.URN_SPORT_STEP
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncStepData(val sjUniWatch: SJUniWatch) : AbSyncData<List<WmStepData>>() {

    var isActionSupport: Boolean = true
    var lastSyncTime: Long = 0
    private var activityObserveEmitter: SingleEmitter<List<WmStepData>>? = null
    private var observeChangeEmitter: ObservableEmitter<List<WmStepData>>? = null
    override fun isSupport(): Boolean {
        return isActionSupport
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(msg: MsgBean, nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    override fun syncData(startTime: Long): Single<List<WmStepData>> {
        return Single.create { emitter ->
            activityObserveEmitter = emitter
            sjUniWatch.sendReadSubPkObserveNode(getReadSportSyncData(URN_SPORT_STEP))
        }
    }

    override var observeSyncData: Observable<List<WmStepData>> =
        Observable.create { emitter -> observeChangeEmitter = emitter }



}