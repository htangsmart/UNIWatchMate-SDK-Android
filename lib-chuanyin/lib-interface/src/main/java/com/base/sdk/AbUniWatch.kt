package com.base.sdk

import android.bluetooth.BluetoothDevice
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.common.WmDiscoverDevice
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.common.WmTimeUnit
import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.app.AbWmApps
import com.base.sdk.port.log.WmLog
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.port.sync.AbWmSyncs
import io.reactivex.rxjava3.core.Completable
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
     * 应用模块
     */
    abstract val wmApps: AbWmApps

    /**
     * 同步模块
     */
    abstract val wmSync: AbWmSyncs

    /**
     * 文件传输
     */
    abstract val wmTransferFile: AbWmTransferFile

    /**
     * 连接方法
     * @return 如果返回null，表示无法识别此[deviceMode]
     */
    abstract fun connect(
        address: String,
        bindInfo: WmBindInfo
    ): WmDevice?

    /**
     * 连接方法
     * @return 如果返回null，表示无法识别此[deviceMode]
     */
    abstract fun connect(
        device: BluetoothDevice,
        bindInfo: WmBindInfo
    ): WmDevice?

    /**
     * 连接方法
     * @return 如果返回null，表示无法识别此[qrString]
     */
    abstract fun connectScanQr(
        qrString: String,
        bindInfo: WmBindInfo,
    ): WmDevice?

    /**
     * 断开连接
     */
    abstract fun disconnect()

    /**
     * 恢复出厂设置
     */
    abstract fun reset(): Completable

    /**
     * 监听连接状态
     */
    abstract val observeConnectState: Observable<WmConnectState>

    /**
     * 获取连接状态
     */
    abstract fun getConnectState(): WmConnectState

    /**
     * 获取当前设备模式。
     * @return null表示尚未
     */
    abstract fun getDeviceModel(): WmDeviceModel?

    /**
     * 设置设备模式.
     * 如果一个SDK支持多个模式，需要保存当前模式，以便在 [getDeviceModel] 获取
     * @return 设置是否成功
     */
    abstract fun setDeviceModel(wmDeviceModel: WmDeviceModel): Boolean

    /**
     * 开始扫描设备
     */
    abstract fun startDiscovery(scanTime: Int, wmTimeUnit: WmTimeUnit): Observable<WmDiscoverDevice>

    fun setLogEnable(logEnable: Boolean) {
        WmLog.logEnable = logEnable
    }

}