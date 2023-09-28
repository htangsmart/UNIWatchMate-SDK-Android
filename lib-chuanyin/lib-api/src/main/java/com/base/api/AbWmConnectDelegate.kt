package com.base.api

import android.bluetooth.BluetoothDevice
import com.base.sdk.AbUniWatch
import com.base.sdk.port.AbWmConnect
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

internal class AbWmConnectDelegate(
    private val watchSubject: BehaviorSubject<AbUniWatch>,
) : AbWmConnect() {

    override fun connect(address: String, bindInfo: BindInfo, deviceMode: WmDeviceModel): WmDevice {
       return watchSubject.value!!.wmConnect.connect(address, bindInfo, deviceMode)
    }

    override fun connect(
        address: BluetoothDevice,
        bindInfo: BindInfo,
        deviceMode: WmDeviceModel
    ): WmDevice {
        return watchSubject.value!!.wmConnect.connect(address, bindInfo, deviceMode)
    }

    override fun disconnect() {
        return watchSubject.value!!.wmConnect.disconnect()
    }

    override fun reset() {
        return watchSubject.value!!.wmConnect.reset()
    }

    override val observeConnectState: Observable<WmConnectState> = watchSubject.switchMap {
        it.wmConnect.observeConnectState
    }.distinctUntilChanged()

    override fun getConnectState(): WmConnectState {
        val watch = watchSubject.value ?: return WmConnectState.DISCONNECTED
        return watch.wmConnect.getConnectState()
    }

}