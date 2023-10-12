package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.entity.data.*
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.entity.settings.*
import com.base.sdk.port.app.*
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs

internal class AbWmAppDelegate(
   private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmApps() {

    override val appAlarm: AbAppAlarm
        get() = watchObservable.value!!.wmApps.appAlarm
    override val appCamera: AbAppCamera
        get() = watchObservable.value!!.wmApps.appCamera
    override val appContact: AbAppContact
        get() = watchObservable.value!!.wmApps.appContact
    override val appFind: AbAppFind
        get() = watchObservable.value!!.wmApps.appFind
    override val appWeather: AbAppWeather
        get() = watchObservable.value!!.wmApps.appWeather
    override val appSport: AbAppSport
        get() = watchObservable.value!!.wmApps.appSport
    override val appNotification: AbAppNotification
        get() = watchObservable.value!!.wmApps.appNotification
    override val appDial: AbAppDial
        get() = watchObservable.value!!.wmApps.appDial
    override val appLanguage: AbAppLanguage
        get() = watchObservable.value!!.wmApps.appLanguage
    override val appMusicControl: AbAppMusicControl
        get() = watchObservable.value!!.wmApps.appMusicControl
}