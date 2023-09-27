package com.base.sdk.entity.data

interface ICalculateSleepItem {
    fun getCalculateStatus(): Int
    fun getCalculateStartTime(): Long
}

/**
 * Sleep data(睡眠数据)
 */
class WmSleepData(
    val timestamp: Long,
    val items: List<WmSleepItem>
)

class WmSleepItem(
    val status: Int,//状态
    val startTime: Long//开始时间
) : ICalculateSleepItem {
    companion object {
        /**
         * Sleep status of deep sleep
         */
        const val STATUS_DEEP = 1 //深睡

        /**
         * Sleep status of light sleep
         */
        const val STATUS_LIGHT = 2 //浅睡

        /**
         * Sleep status of sober sleep
         */
        const val STATUS_SOBER = 3 //清醒

        /**
         * Sleep status of REM sleep
         */
        const val STATUS_REM = 4 //快速眼动

    }

    //计算状态
    override fun getCalculateStatus(): Int {
        return status
    }

    //计算开始时间
    override fun getCalculateStartTime(): Long {
        return startTime
    }

}