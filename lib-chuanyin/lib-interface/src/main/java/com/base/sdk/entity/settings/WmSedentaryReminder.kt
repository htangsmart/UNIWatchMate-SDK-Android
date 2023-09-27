package com.base.sdk.entity.settings

import com.base.sdk.entity.common.WmNoDisturb
import com.base.sdk.entity.common.WmTimeFrequency
import com.base.sdk.entity.data.WmTimeRange

/**
 * Sedentary reminder(久坐提醒)
 */
class WmSedentaryReminder(
    /**
     * Whether to enable(是否启用)
     */
    var isEnabled: Boolean,
    /**
     * Time range(时间范围)
     */
    var timeRange: WmTimeRange,
    /**
     * Frequency(频率)
     */
    var frequency: WmTimeFrequency,
    /**
     * No-disturb lunch break(午休免打扰)
     */
    var noDisturbLunchBreak: WmNoDisturb
) {
    override fun toString(): String {
        return "WmSedentaryReminder(isEnabled=$isEnabled, timeRange=$timeRange, frequency=$frequency, noDisturbInNoon=$noDisturbLunchBreak)"
    }
}
