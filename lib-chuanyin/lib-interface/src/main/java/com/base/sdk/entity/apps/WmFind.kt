package com.base.sdk.entity.apps

/**
 * 查找（手机/手表）定义
 * 响铃次数
 * 响铃时长 单位秒(S)
 */
data class WmFind(val count: Int, val timeSeconds: Int) {
    override fun toString(): String {
        return "WmFind(count=$count, timeSeconds=$timeSeconds)"
    }
}