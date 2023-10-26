package com.sjbt.sdk.sync

import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class SyncBatteryInfo(val sjUniWatch: SJUniWatch) : AbSyncData<WmBatteryInfo>() {

    var batteryEmitter: SingleEmitter<WmBatteryInfo>? = null
    var observeBatteryEmitter: ObservableEmitter<WmBatteryInfo>? = null
    private var lastSyncTime: Long = 0

    override fun isSupport(): Boolean {
        return true
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    fun onTimeOut(nodeData: NodeData) {
        TODO("Not yet implemented")
    }
    override fun syncData(startTime: Long): Single<WmBatteryInfo> {
        return Single.create {
            batteryEmitter = it
            getBatteryInfo()
        }
    }

    /**
     * 获取电池信息
     */
    private fun getBatteryInfo() {
        sjUniWatch.sendNormalMsg(CmdHelper.batteryInfo)
    }

    override var observeSyncData: Observable<WmBatteryInfo> =
        Observable.create { emitter -> observeBatteryEmitter = emitter }

}