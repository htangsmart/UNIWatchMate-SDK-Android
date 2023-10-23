package com.base.sdk.entity.apps

/**
 * 运动id和类型 需要终端、设备端、云端统一，APP根据需求自行定义
 * type 可以作为二级分类标识
 * buildIn 是否内置运动
 */
data class WmSport(val id: Int, val type: Int,val buildIn:Boolean) {
    override fun toString(): String {
        return "WmSport(id=$id, type=$type, buildIn=$buildIn)"
    }
}

/**
 * 数据类型 需要终端、设备端、云端统一，APP根据需求自行定义
 */
data class WmValueTypeData(val id: Int, val value: Double)
