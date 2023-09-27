package com.base.sdk.entity.data

/**
 * Time range(时间范围)
 */
data class WmTimeRange(var startHour: Int,
                       var startMinute: Int,
                       var endHour: Int,
                       var endMinute: Int
                       ) {
    override fun toString(): String {
        return "WmTimeRange(startHour=$startHour, startMinute=$startMinute, endHour=$endHour, endMinute=$endMinute)"
    }
}
