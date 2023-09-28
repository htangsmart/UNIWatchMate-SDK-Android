package com.base.sdk

import android.bluetooth.BluetoothDevice
import com.base.sdk.port.AbWmConnect
import com.base.sdk.port.WmTransferFile
import com.base.sdk.port.app.AbWmApps
import com.base.sdk.port.log.WmLog
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.port.sync.AbWmSyncs
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.WmScanDevice
import io.reactivex.rxjava3.core.Observable

/**
 * sdk接口抽象类
 * 封装了几大功能模块
 * sdk需要实现此接口，并实现每一个功能模块下的功能
 * 拿到此接口实例即可操作sdk实现的所有功能
 *
 * sdk interface abstract class
 * Encapsulates several major functional modules
 * The sdk implementation class needs to implement this interface and implement the functions under each functional module
 * App can operate all functions implemented by sdk after getting an instance of this interface implementation class.
 */
abstract class AbUniWatch {

    /**
     * 设置模块
     */
    abstract val wmSettings: AbWmSettings

    /**
     * A应用模块
     */
    abstract val wmApps: AbWmApps

    /**
     * 同步模块
     */
    abstract val wmSync: AbWmSyncs

    /**
     * 连接模块
     */
    abstract val wmConnect: AbWmConnect

    /**
     * 文件传输
     */
    abstract val wmTransferFile: WmTransferFile

    /**
     * 获取设备模式。
     * 可能存在某个SDK支持多种模式的情况。
     */
    abstract fun getDeviceModel(): WmDeviceModel

    /**
     * 设置设备模式.
     * 如果一个SDK支持多个模式，需要保存当前模式，以便在 [getDeviceModel] 获取
     * @return 设置是否成功
     */
    abstract fun setDeviceMode(wmDeviceModel: WmDeviceModel): Boolean

    /**
     * 开始扫描设备
     */
    abstract fun startDiscovery(): Observable<BluetoothDevice>

    fun setLogEnable(logEnable: Boolean) {
        WmLog.logEnable = logEnable
    }

    abstract fun parseScanQr(qrString: String): WmScanDevice

}