package com.base.sdk.`interface`

import android.bluetooth.BluetoothDevice
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 连接模块
 */
abstract class AbWmConnect {
    /**
     * 连接方法
     */
    abstract fun connect(address: String, bindInfo: BindInfo, deviceMode: WmDeviceModel): WmDevice
    abstract fun connect(
        address: BluetoothDevice,
        bindInfo: BindInfo,
        deviceMode: WmDeviceModel
    ): WmDevice

    /**
     * 断开连接
     */
    abstract fun disconnect()

    /**
     * 恢复出厂设置
     */
    abstract fun reset()

    /**
     * 连接状态监听
     */
    abstract val observeConnectState: Observable<WmConnectState>

    abstract fun getConnectState(): WmConnectState

    /**
     * 当扫码的时候要传随机码
     */
    data class BindInfo(val bindType: BindType, val userInfo: UserInfo, val scanCode: String)

    /**
     * 绑定类型
     */
    enum class BindType {
        SCAN_QR,
        DISCOVERY
    }

    /**
     * 用户信息
     */
    data class UserInfo(val userId: String, val userName: String)

}