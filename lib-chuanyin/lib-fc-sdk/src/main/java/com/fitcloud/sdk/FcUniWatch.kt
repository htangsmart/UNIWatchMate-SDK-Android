package com.fitcloud.sdk

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.base.sdk.AbUniWatch
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.common.WmTimeUnit
import com.base.sdk.entity.common.WmDiscoverDevice
import com.base.sdk.port.WmTransferFile
import com.base.sdk.port.app.AbWmApps
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.port.sync.AbWmSyncs
import com.fitcloud.sdk.settings.FcSettings
import com.topstep.fitcloud.sdk.connector.FcConnectorState
import com.topstep.fitcloud.sdk.v2.FcSDK
import io.reactivex.rxjava3.core.Completable
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
    override val wmTransferFile: WmTransferFile
        get() = TODO("Not yet implemented")

    override fun connect(address: String, bindInfo: WmBindInfo): WmDevice? {
        if (bindInfo.model != getDeviceModel())
            return null
        requireSDK().connector.connect(
            address = address,
            userId = bindInfo.userId,
            bindOrLogin = true,
            sex = true,
            age = 20,
            height = 180f,
            weight = 75f
        )
        return WmDevice(bindInfo.model)
    }

    override fun connect(device: BluetoothDevice, bindInfo: WmBindInfo): WmDevice? {
        if (bindInfo.model != getDeviceModel())
            return null
        requireSDK().connector.connect(
            device = device,
            userId = bindInfo.userId,
            bindOrLogin = true,
            sex = true,
            age = 20,
            height = 180f,
            weight = 75f
        )
        val name = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || ContextCompat.checkSelfPermission(application, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        ) {
            device.name
        } else {
            null
        }
        return WmDevice(bindInfo.model)
    }

    override fun connectScanQr(qrString: String, bindInfo: WmBindInfo): WmDevice? {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        fcSDK?.connector?.disconnect()
    }

    override fun reset(): Completable {
        TODO("Not yet implemented")
    }

    override val observeConnectState: Observable<WmConnectState> =
        requireSDK().connector.observerConnectorState().map {
            mapState(it)
        }

    override fun getConnectState(): WmConnectState {
        return mapState(fcSDK?.connector?.getConnectorState())
    }

    private fun mapState(state: FcConnectorState?): WmConnectState {
        return when (state) {
            null, FcConnectorState.DISCONNECTED -> WmConnectState.DISCONNECTED
            FcConnectorState.PRE_CONNECTING -> WmConnectState.CONNECTING
            FcConnectorState.CONNECTING -> WmConnectState.CONNECTING
            FcConnectorState.PRE_CONNECTED -> WmConnectState.CONNECTED
            FcConnectorState.CONNECTED -> WmConnectState.VERIFIED
        }
    }

    override fun getDeviceModel(): WmDeviceModel? {
        return WmDeviceModel.FC_WATCH
    }

    override fun setDeviceModel(wmDeviceModel: WmDeviceModel): Boolean {
        return wmDeviceModel == WmDeviceModel.FC_WATCH
    }

    override fun startDiscovery(scanTime: Int, wmTimeUnit: WmTimeUnit): Observable<WmDiscoverDevice> {
        TODO("Not yet implemented")
    }

    var isForeground: Boolean = false
        set(value) {
            field = value
            fcSDK?.isForeground = value
        }

}