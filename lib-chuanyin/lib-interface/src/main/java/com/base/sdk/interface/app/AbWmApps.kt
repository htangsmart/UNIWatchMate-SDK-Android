package com.base.sdk.`interface`.app

/**
 * 应用模块功能聚合
 */
abstract class AbWmApps {
    /**
     * 闹钟功能
     */
    abstract val appAlarm: AbAppAlarm

    /**
     * 相机功能
     */
    abstract val appCamera: AbAppCamera

    /**
     * 通讯录
     */
    abstract val appContact: AbAppContact

    /**
     * 查找功能
     */
    abstract val appFind: AbAppFind

    /**
     * 天气功能
     */
    abstract val appWeather: AbAppWeather

    /**
     * 运动功能
     */
    abstract val appSport: AbAppSport

    /**
     * 通知功能
     */
    abstract val appNotification: AbAppNotification

    /**
     * 表盘
     */
    abstract val appDial: AbAppDial

    /**
     * 语言
     */
    abstract val appLanguage: AbAppLanguage

}