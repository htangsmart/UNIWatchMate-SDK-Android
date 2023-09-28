package com.sjbt.sdk.app

import com.base.sdk.port.app.*
import com.sjbt.sdk.SJUniWatch

class SJApps(sjUniWatch: SJUniWatch) : AbWmApps() {

    override var appAlarm: AbAppAlarm = AppAlarm()

    override var appCamera: AbAppCamera = AppCamera(sjUniWatch)

    override var appContact: AbAppContact = AppContact()

    override var appFind: AbAppFind = AppFind()

    override var appWeather: AbAppWeather = AppWeather()

    override var appSport: AbAppSport = AppSport()

    override var appNotification: AbAppNotification = AppNotification()

    override var appDial: AbAppDial = AppDial(sjUniWatch)

    override var appLanguage: AbAppLanguage = AppLanguage()
}