package com.base.sdk.entity

/**
 * 连接时所需的绑定信息
 */
data class WmBindInfo(
    val userId: String,
    val userName: String,
    val bindType: BindType,
    var model: WmDeviceModel = WmDeviceModel.NOR_REG
) {
    var randomCode: String? = null
    override fun toString(): String {
        return "WmBindInfo(userId='$userId', userName='$userName', bindType=$bindType, model=$model, randomCode=$randomCode)"
    }
}

enum class BindType {
    SCAN_QR,
    DISCOVERY,
//    CONNECT_BACK
}