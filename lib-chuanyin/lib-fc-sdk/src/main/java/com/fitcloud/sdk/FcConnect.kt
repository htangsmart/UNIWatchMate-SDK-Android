package com.fitcloud.sdk

import android.bluetooth.BluetoothDevice
import com.base.sdk.port.AbWmConnect
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.topstep.fitcloud.sdk.connector.FcConnectorState
import com.topstep.fitcloud.sdk.v2.FcConnector
import io.reactivex.rxjava3.core.Observable

class FcConnect(
    private val connector: FcConnector,
) : AbWmConnect() {

    override fun connect(address: String, bindInfo: BindInfo, deviceMode: WmDeviceModel): WmDevice {
        connector.connect(
            address = address,
            userId = bindInfo.userInfo.userId,
            bindOrLogin = true,
            sex = true,
            age = 20,
            height = 180f,
            weight = 75f
        )
        TODO()
    }

    override fun connect(
        address: BluetoothDevice,
        bindInfo: BindInfo,
        deviceMode: WmDeviceModel
    ): WmDevice {
        connector.connect(
            device = address,
            userId = bindInfo.userInfo.userId,
            bindOrLogin = true,
            sex = true,
            age = 20,
            height = 180f,
            weight = 75f
        )
        TODO()
    }

    override fun disconnect() {
        connector.close()
    }

    override fun reset() {
        connector.settingsFeature().deviceReset().onErrorComplete().subscribe()
    }

    override val observeConnectState: Observable<WmConnectState> =
        connector.observerConnectorState().map {
            mapState(it)
        }

    override fun getConnectState(): WmConnectState {
        return mapState(connector.getConnectorState())
    }

    private fun mapState(state: FcConnectorState): WmConnectState {
        return when (state) {
            FcConnectorState.DISCONNECTED -> WmConnectState.DISCONNECTED
            FcConnectorState.PRE_CONNECTING -> WmConnectState.CONNECTING
            FcConnectorState.CONNECTING -> WmConnectState.CONNECTING
            FcConnectorState.PRE_CONNECTED -> WmConnectState.CONNECTED
            FcConnectorState.CONNECTED -> WmConnectState.VERIFIED
        }
    }

}