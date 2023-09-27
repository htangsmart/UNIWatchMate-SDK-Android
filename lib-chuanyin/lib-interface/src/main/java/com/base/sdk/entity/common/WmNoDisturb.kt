package com.base.sdk.entity.common

import com.base.sdk.entity.data.WmTimeRange

/**
 * No-disturb(免打扰)
 */
data class WmNoDisturb(var isEnabled: Boolean,
                       var timeRange: WmTimeRange
                       ) {
    init {
        println("Initialized with isEnabled: $isEnabled and timeRange: $timeRange")
    }

    constructor() : this(false, WmTimeRange(0, 0, 0, 0))

    override fun toString(): String {
        return "WmNoDisturb(isEnabled=$isEnabled, timeRange=$timeRange)"
    }
}
