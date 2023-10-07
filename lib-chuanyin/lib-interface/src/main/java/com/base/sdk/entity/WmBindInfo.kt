package com.base.sdk.entity

/**
 * 连接时所需的绑定信息
 */
data class WmBindInfo(
    val userId: String,
    val userName: String,
    val bindType: BindType,
    val model: WmDeviceModel
) {
    var randomCode: String? = null
}

enum class BindType {
    SCAN_QR,
    DISCOVERY,
    CONNECT_BACK
}