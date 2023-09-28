package com.sjbt.sdk.sample.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeviceBindEntity(
    /**
     * 用户Id
     */
    @PrimaryKey
    val userId: Long,

    /**
     * 设备地址
     */
    val address: String,

    /**
     * 设备名称
     */
    val name: String,
)

//internal fun DeviceBindEntity?.toModel(): WmDeviceInfo? {
//    return if (this == null) {
//        null
//    } else {
//        WmDeviceInfo(
//            address = address,
//            name = name,
//            isTryingBind = false
//        )
//    }
//}