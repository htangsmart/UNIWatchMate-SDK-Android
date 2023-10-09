package com.base.sdk.entity.settings

/**
 * Device information(设备信息)
 */
data class WmDeviceInfo(
    /**
     * device model(设备型号)
     */
    val model: String,
    /**
     * device mac address(设备mac地址)
     */
    val macAddress: String,
    /**
     * device version(设备版本)
     */
    val version: String,
    /**
     * device id(设备id)
     */
    val deviceId: String,
    /**
     * bluetooth name(蓝牙名称)
     */
    val bluetoothName: String,
    /**
     * device name(设备名称)
     */
    val deviceName: String,
    /**
     * 适配表盘
     */
    val dialAbility: String,
    /**
     * 屏幕规格 screen model
     */
    val screen: String,
    /**
     * 设备当前语言 device language
     */
    var lang: Int,
    /**
     * 设备屏幕 宽 screen width
     */
    var cw: Int,
    /**
     * 设备屏幕 高 screen height
     */
    var ch: Int
) {
    override fun toString(): String {
        return "WmDeviceInfo(model='$model', macAddress='$macAddress', version='$version', deviceId='$deviceId', bluetoothName='$bluetoothName', deviceName='$deviceName', screen='$screen', lang=$lang, cw=$cw, ch=$ch)"
    }
}