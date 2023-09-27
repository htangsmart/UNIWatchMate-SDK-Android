package com.sjbt.sdk

import android.bluetooth.BluetoothDevice
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.`interface`.AbWmConnect
import com.base.sdk.`interface`.log.WmLog
import com.sjbt.sdk.log.SJLog
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

class SJConnect(sjUniWatch: SJUniWatch) : AbWmConnect() {

    private var connectEmitter: ObservableEmitter<WmConnectState>? = null
    private val TAG = TAG_SJ + "Connect"
    private var btEngine = sjUniWatch.mBtEngine
    private var mBtAdapter = sjUniWatch.mBtAdapter
    var mBindInfo: BindInfo? = null
    private val sjUniWatch: SJUniWatch = sjUniWatch

    var mCurrDevice: BluetoothDevice? = null
    var mCurrAddress: String? = null
    var mConnectTryCount = 0
    var mConnectState:WmConnectState = WmConnectState.DISCONNECTED

    /**
     * 通过address 连接
     */
    override fun connect(address: String, bindInfo: BindInfo, deviceMode: WmDeviceModel): WmDevice {
        mCurrAddress = address
        val device = WmDevice(deviceMode)
        device.address = address
        device.mode = deviceMode
        mBindInfo = bindInfo
        device.isRecognized = deviceMode == WmDeviceModel.SJ_WATCH

        if (device.isRecognized) {
            SJLog.logBt(TAG, " connect:${address}")
            connectEmitter?.onNext(WmConnectState.CONNECTING)

            try {
                mCurrDevice = mBtAdapter.getRemoteDevice(address)
                btEngine.connect(mCurrDevice)
            } catch (e: Exception) {
                e.printStackTrace()
                connectEmitter?.onNext(WmConnectState.DISCONNECTED)
            }
        } else {
            connectEmitter?.onNext(WmConnectState.DISCONNECTED)
        }

        return device
    }

    /**
     * 通过BluetoothDevice 连接
     */
    override fun connect(
        bluetoothDevice: BluetoothDevice,
        bindInfo: BindInfo,
        deviceMode: WmDeviceModel
    ): WmDevice {
        mCurrDevice = bluetoothDevice
        val wmDevice = WmDevice(deviceMode)
        mCurrAddress = bluetoothDevice.address
        wmDevice.address = bluetoothDevice.address
        wmDevice.isRecognized = deviceMode == WmDeviceModel.SJ_WATCH

        if (wmDevice.isRecognized) {
            WmLog.e(TAG, " connect:${wmDevice}")
            connectEmitter?.onNext(WmConnectState.CONNECTING)
            btEngine.connect(bluetoothDevice)
        } else {
            connectEmitter?.onError(RuntimeException("not recognized device"))
        }

        return wmDevice
    }

    /**
     * 重连
     */
    fun reConnect(device: BluetoothDevice) {
        mBindInfo?.let {
            connect(device, it, WmDeviceModel.SJ_WATCH)
        }
    }

    fun btStateChange(state: WmConnectState) {
        connectEmitter?.onNext(state)
        mConnectState = state
    }

    override fun disconnect() {
        sjUniWatch.mBtEngine.closeSocket("user", true)
    }

    override fun reset() {
        sjUniWatch.sendNormalMsg(CmdHelper.getUnBindCmd())
    }

    override val observeConnectState: Observable<WmConnectState> = Observable.create {
        connectEmitter = it
    }

    override fun getConnectState(): WmConnectState {
        return mConnectState
    }

}