package com.fitcloud.sdk

import android.app.Application
import android.bluetooth.BluetoothDevice
import com.base.sdk.AbUniWatch
import com.base.sdk.`interface`.AbWmConnect
import com.base.sdk.`interface`.WmTransferFile
import com.base.sdk.`interface`.app.AbWmApps
import com.base.sdk.`interface`.setting.AbWmSettings
import com.base.sdk.`interface`.sync.AbWmSyncs
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.WmScanDevice
import com.fitcloud.sdk.settings.FcSettings
import com.topstep.fitcloud.sdk.v2.FcSDK
import io.reactivex.rxjava3.core.Observable

abstract class FcUniWatch(
    private val application: Application,
) : AbUniWatch() {

    @Volatile
    private var fcSDK: FcSDK? = null

    /**
     * 只在必要的时候创建
     */
    abstract fun create(application: Application): FcSDK

    private fun requireSDK(): FcSDK {
        if (fcSDK == null) {
            synchronized(this) {
                if (fcSDK == null) {
                    fcSDK = create(application)
                    fcSDK?.isForeground = isForeground
                }
            }
        }
        return fcSDK!!
    }

    override val wmSettings: AbWmSettings by lazy(LazyThreadSafetyMode.NONE) {
        FcSettings(requireSDK().connector)
    }

    override val wmApps: AbWmApps
        get() = TODO("Not yet implemented")
    override val wmSync: AbWmSyncs
        get() = TODO("Not yet implemented")

    override val wmConnect: AbWmConnect by lazy(LazyThreadSafetyMode.NONE) {
        FcConnect(requireSDK().connector)
    }

    override val wmTransferFile: WmTransferFile
        get() = TODO("Not yet implemented")

    override fun getDeviceModel(): WmDeviceModel {
        return WmDeviceModel.FC_WATCH
    }

    override fun setDeviceMode(wmDeviceModel: WmDeviceModel): Boolean {
        return wmDeviceModel == WmDeviceModel.FC_WATCH
    }

    override fun startDiscovery(): Observable<BluetoothDevice> {
        TODO("Not yet implemented")
    }

    override fun parseScanQr(qrString: String): WmScanDevice {
        TODO("Not yet implemented")
    }

    var isForeground: Boolean = false
        set(value) {
            field = value
            fcSDK?.isForeground = value
        }

}