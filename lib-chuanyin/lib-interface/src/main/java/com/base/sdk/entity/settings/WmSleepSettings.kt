package com.base.sdk.entity.settings

/**
 * The date and time information of the watch
 * (日期与时间同步信息)
 */
data class WmSleepSettings(
    var open: Boolean,
    var startHour: Int,
    var startMinute: Int,
    var endHour: Int,
    var endMinute: Int,
) {
    override fun toString(): String {
        return "WmSleepSettings(open=$open, startHour=$startHour, startMinute=$startMinute, endHour=$endHour, endMinute=$endMinute)"
    }
}

