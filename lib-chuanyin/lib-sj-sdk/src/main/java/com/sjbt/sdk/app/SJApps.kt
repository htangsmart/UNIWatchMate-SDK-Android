package com.sjbt.sdk.app

import com.base.sdk.`interface`.app.*

class SJApps : AbWmApps() {

    override var appAlarm: AbAppAlarm = AppAlarm()

    override var appCamera: AbAppCamera = AppCamera()

    override var appContact: AbAppContact = AppContact()

    override var appFind: AbAppFind = AppFind()

    override var appWeather: AbAppWeather = AppWeather()

    override var appSport: AbAppSport = AppSport()

    override var appNotification: AbAppNotification = AppNotification()

    override var appDial: AbAppDial = AppDial()

    override var appLanguage: AbAppLanguage = AppLanguage()
}