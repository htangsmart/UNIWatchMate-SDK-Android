package com.base.sdk.entity

/**
 * 扫描二维码返回设备信息数据结构
 */
class WmScanDevice(mode: WmDeviceModel?) : WmDevice(mode!!) {
    var randomCode: String? = null
}