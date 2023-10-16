package com.sjbt.sdk.sample.utils

import com.base.sdk.entity.settings.WmDeviceInfo

object CacheDataHelper {

    private var transferringFile = false
    private var basicInfo: WmDeviceInfo? = null
    var measureWidth = -1
    var cameraLaunchedByDevice = false
    var cameraLaunchedBySelf = false

    fun clearCachedData() {
//        setCurrentDeviceBean(null)
        basicInfo = null
    }

    fun clearDataWithOutAccount() {
//        setCurrentDeviceBean(null)
        basicInfo = null
    }

    private var currDeviceBean: WmDeviceInfo? = null

    fun setCurrentDeviceInfo(deviceBean: WmDeviceInfo?) {
        currDeviceBean = deviceBean
    }
    fun getCurrentDeiceBean(): WmDeviceInfo? {
        return currDeviceBean
    }



    fun getTransferring(): Boolean {
        return transferringFile
    }

    fun setTransferring(boolean: Boolean) {
        transferringFile = boolean
    }

}