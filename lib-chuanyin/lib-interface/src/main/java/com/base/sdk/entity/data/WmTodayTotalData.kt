package com.base.sdk.entity.data

/**
 * Today total data(今日总数据)
 */
class WmTodayTotalData(
    timestamp: Long,

    /**
     * Total steps
     */
    val step: Int, //总步数

    /**
     * Total distance. (unit m)
     */
    val distance: Int,//总数据，单位米

    /**
     *Total calorie. (unit calorie, not kCal)
     */
    val calorie: Int, //总卡路里数，单位卡，不是千卡

    /**
     * Total deep sleep time. (unit minutes)
     */
    val deepSleep: Int,//深睡总时长，单位分钟

    /**
     * Total light sleep time. (unit minutes)
     */
    val lightSleep: Int, //浅睡总时长，单位分钟

    /**
     * Average heart rate. (beats per minute)
     */
    val heartRate: Int, //平均心率

    /**
     * Step not save in item.
     */
    val deltaStep: Int, //未保存在item中的步数

    /**
     * Distance not save in item. (unit m)
     */
    val deltaDistance: Int,//未保存在item中的距离，单位米

    /**
     * Calorie not save in item. (unit calorie, not kCal)
     */
    val deltaCalorie: Int,//未保存在item中的卡路里数，单位卡，不是千卡

) : WmBaseSyncData(timestamp)