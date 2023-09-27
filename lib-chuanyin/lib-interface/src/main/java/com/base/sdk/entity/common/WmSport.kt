package com.base.sdk.entity.common

/**
 * 运动id和类型 需要终端、设备端、云端统一，APP根据需求自行定义
 * type 可以作为二级分类标识
 */
data class WmSport(val id: Int, val type: Int) {
    override fun toString(): String {
        return "WmSport(id=$id, type=$type)"
    }
}

/**
 * 数据类型 需要终端、设备端、云端统一，APP根据需求自行定义
 */
data class WmValueTypeData(val id: Int, val value: Double)
