package com.sjbt.sdk.sync

import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.port.sync.AbSyncData
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SyncDeviceInfo(val sjUniWatch: SJUniWatch) : AbSyncData<WmDeviceInfo>() {

    var deviceEmitter: SingleEmitter<WmDeviceInfo>? = null
    var observeDeviceEmitter: ObservableEmitter<WmDeviceInfo>? = null
    private var lastSyncTime: Long = 0

    override fun isSupport(): Boolean {
        return true
    }

    override fun latestSyncTime(): Long {
        return lastSyncTime
    }

    override fun syncData(startTime: Long): Single<WmDeviceInfo> {
        return Single.create {
            deviceEmitter = it
            getBasicInfo()
        }
    }

    /**
     * 获取基本信息
     * @param
     */
    private fun getBasicInfo() {
        sjUniWatch.sendNormalMsg(CmdHelper.baseInfoCmd)
    }

    override var observeSyncData: Observable<WmDeviceInfo> =
        Observable.create { emitter -> observeDeviceEmitter = emitter }

}