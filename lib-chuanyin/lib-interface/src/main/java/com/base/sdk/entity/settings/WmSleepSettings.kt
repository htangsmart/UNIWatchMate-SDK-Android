package com.base.sdk.entity.settings

/**
 * The date and time information of the watch
 * (日期与时间同步信息)
 */
data class WmSleepSettings(
    val open: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
) {
    override fun toString(): String {
        return "WmSleepSettings(open=$open, startHour=$startHour, startMinute=$startMinute, endHour=$endHour, endMinute=$endMinute)"
    }
}

