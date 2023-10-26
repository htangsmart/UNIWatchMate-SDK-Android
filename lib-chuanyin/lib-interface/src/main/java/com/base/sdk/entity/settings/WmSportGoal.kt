package com.base.sdk.entity.settings

/**
 * Sport goal(运动目标)
 */
data class WmSportGoal(
    /**
     * Steps(步数)
     */
    val steps: Int,
    /**
     * Calories(卡路里 卡)
     */
    val calories: Int,
    /**
     * Distance(距离（米）)
     */
    val distance: Int,
    /**
     * Activity duration(活动时长 分钟)
     */
    val activityDuration: Short
) {
    override fun toString(): String {
        return "WmSportGoal(steps=$steps, calories=$calories, distance=$distance, activityDuration=$activityDuration)"
    }
}
