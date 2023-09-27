package com.base.sdk.entity.settings

/**
 * Heart rate alerts(心率提醒)
 */
class WmHeartRateAlerts(
    /**
     * Heart rate auto measure(心率自动测量开关)
     */
    var isEnableHrAutoMeasure: Boolean,
    /**
     * Max heart rate(最大心率)
     */
    var maxHeartRate: Int = DEFAULT_MAX_HEART_RATE,
    /**
     * Exercise heart rate alert(运动心率提醒)
     */
    var exerciseHeartRateAlert: HeartRateThresholdAlert = HeartRateThresholdAlert(),
    /**
     * Resting heart rate alert(静息心率提醒)
     */
    var restingHeartRateAlert: HeartRateThresholdAlert = HeartRateThresholdAlert(),
    /**
     * Age(年龄)
     */
    val age: Int
) {
    constructor(age: Int) : this(
        false, DEFAULT_MAX_HEART_RATE, HeartRateThresholdAlert(), HeartRateThresholdAlert(), age)

    companion object {
        /**
         * Default max heart rate(默认最大心率)
         */
        const val DEFAULT_MAX_HEART_RATE = 200
        /**
         * Heart rate thresholds, 0  (心率阈值列表，0表示关闭)
         */
        val THRESHOLDS = listOf(0, 100, 110, 120, 130, 140, 150)

        lateinit var HEART_RATE_INTERVALS: List<Int>
    }

    // 内部类，用于表示心率过高提醒的设置
    data class HeartRateThresholdAlert(
        var isEnabled: Boolean = false,
        var threshold: Int = THRESHOLDS[0]  // 默认值为阈值列表的第一个值
    )

    init {
        if (maxHeartRate <= 0) {
            maxHeartRate = DEFAULT_MAX_HEART_RATE
        }

        /**
         *
         * 心率区间计算公式：
         * 1. 热身---(0,（最大心率-年龄）* 0.6)
         * 2. 减脂---[最大心率-年龄）*0.6,（最大心率-年龄）*0.7)
         * 3. 耐力----[（最大心率-年龄）*0.7, （最大心率-年龄）*0.8)
         * 4. 无氧----[（最大心率-年龄）*0.8, （最大心率-年龄）*0.9)
         * 5. 极限----[（最大心率-年龄）*0.9, 最大心率]
         *
         */
        HEART_RATE_INTERVALS = listOf(
            ((maxHeartRate - age) * 0.6).toInt(),
            ((maxHeartRate - age) * 0.7).toInt(),
            ((maxHeartRate - age) * 0.8).toInt(),
            ((maxHeartRate - age) * 0.9).toInt(),
            maxHeartRate
        )

    }


    /**
     * Restore default max heart rate(恢复默认最大心率)
     */
    fun restoreDefaultMaxHeartRate() {
        maxHeartRate = DEFAULT_MAX_HEART_RATE
    }


    fun setExerciseHeartRateAlert(isEnabled: Boolean, threshold: Int) {
        exerciseHeartRateAlert.isEnabled = isEnabled
        if (threshold in THRESHOLDS) {
            exerciseHeartRateAlert.threshold = threshold
        } else {
            exerciseHeartRateAlert.threshold = THRESHOLDS[0]
        }
    }

    fun setRestingHeartRateAlert(isEnabled: Boolean, threshold: Int) {
        restingHeartRateAlert.isEnabled = isEnabled
        if (threshold in THRESHOLDS) {
            restingHeartRateAlert.threshold = threshold
        } else {
            restingHeartRateAlert.threshold = THRESHOLDS[0]
        }
    }

    override fun toString(): String {
        return "WmHeartRateAlerts(maxHeartRate=$maxHeartRate, exerciseHeartRateAlert=$exerciseHeartRateAlert, restingHeartRateAlert=$restingHeartRateAlert)"
    }
}