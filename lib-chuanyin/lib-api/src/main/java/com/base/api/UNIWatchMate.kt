package com.base.api

import android.app.Application
import com.base.sdk.AbUniWatch
import com.base.sdk.port.AbWmConnect
import com.base.sdk.port.WmTransferFile
import com.base.sdk.port.app.AbWmApps
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.port.sync.AbWmSyncs
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.port.log.WmLog
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

object UNIWatchMate {
    private val TAG = "UNIWatchMate"

    private lateinit var application: Application
    private val uniWatches: MutableList<AbUniWatch> = ArrayList()

//    var mWmTransferFile: WmTransferFile? = null
//    var mWmApps: AbWmApps? = null
//    var mWmSyncs: AbWmSyncs? = null

    var mInstance: AbUniWatch? = null

    private val uniWatchSubject = BehaviorSubject.create<AbUniWatch>()
    private val uniWatchObservable = BehaviorObservable<AbUniWatch>(uniWatchSubject)

    val wmConnect: AbWmConnect = AbWmConnectDelegate(uniWatchSubject)
    val wmSettings: AbWmSettings = AbWmSettingsDelegate(uniWatchObservable)

    fun init(application: Application, uniWatches: List<AbUniWatch>) {
        if (this::application.isInitialized) {
            return
        }
        this.application = application
        this.uniWatches.addAll(uniWatches)

        if (uniWatches.isEmpty()) {
            throw RuntimeException("No Sdk Register Exception!")
        }
    }

    fun setDeviceModel(deviceMode: WmDeviceModel) {
        if (uniWatchSubject.value?.getDeviceModel() == deviceMode) {
            //deviceMode不变
            return
        }

        for (i in uniWatches.indices) {
            val uniWatch = uniWatches[i]
            if (uniWatch.setDeviceMode(deviceMode)) {
                uniWatchSubject.onNext(uniWatch)
                mInstance = uniWatch
                return
            }
        }

        //出现此情况，说明调用者没有正确调用 init
        throw RuntimeException("No Sdk Match Exception!")
    }

    fun scanQr(qrString: String, bindInfo: AbWmConnect.BindInfo) {
        uniWatches.forEach {
            val scanDevice = it.parseScanQr(qrString)

            WmLog.e(TAG, "scanQr: $scanDevice")

            scanDevice?.let { device ->
                if (device.isRecognized) {
//                    mWmApps = it.wmApps
//                    mWmSyncs = it.wmSync
//                    mWmTransferFile = it.wmTransferFile

                    mInstance = it
                    uniWatchSubject.onNext(it)

                    bindInfo.randomCode = scanDevice.randomCode
                    wmConnect?.connect(device.address!!, bindInfo, device.mode)
                }
            }
        }
    }

    fun observeUniWatchChange(): Observable<AbUniWatch> {
        return uniWatchObservable
    }

}