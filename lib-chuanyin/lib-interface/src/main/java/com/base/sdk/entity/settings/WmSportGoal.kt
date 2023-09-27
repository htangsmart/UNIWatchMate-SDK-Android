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
     * Calories(卡路里)
     */
    val calories: Double,
    /**
     * Distance(距离)
     */
    val distance: Double,
    /**
     * Activity duration(活动时长 分钟)
     */
    val activityMinutes: Long
) {
    override fun toString(): String {
        return "WmSportGoal(steps=$steps, calories=$calories, distance=$distance, activityDuration=$activityMinutes)"
    }
}
