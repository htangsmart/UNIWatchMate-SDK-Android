package com.sjbt.sdk.sample.model.device

import com.base.sdk.entity.WmDeviceModel
import com.sjbt.sdk.sample.entity.DeviceBindEntity


/**
 * ToNote:Avoid declare as a data class, because the [DeviceManager.rebind] need trigger connection, even when the device is not changed
 */
class ConnectorDevice(
    /**
     * Device mac address
     */
    val address: String,

    /**
     * Device name
     */
    val name: String,
    val wmDeviceMode: WmDeviceModel,

    /**
     * Is trying to bind
     */
    val isTryingBind: Boolean
) {
    override fun toString(): String {
        return "[address:$address name:$name isTryingBind:$isTryingBind]"
    }
}
fun ConnectorDevice.deviceModeToInt(): Int {
    if (wmDeviceMode == WmDeviceModel.SJ_WATCH) {
        return 0
    }else{
        return 1
    }
}