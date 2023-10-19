package com.sjbt.sdk.app

import com.base.sdk.port.app.*
import com.sjbt.sdk.SJUniWatch

class SJApps(val sjUniWatch: SJUniWatch) : AbWmApps() {

    override var appAlarm = AppAlarm(sjUniWatch)

    override var appCamera = AppCamera(sjUniWatch)

    override var appContact = AppContact(sjUniWatch)

    override var appFind = AppFind(sjUniWatch)

    override var appWeather = AppWeather(sjUniWatch)

    override var appSport = AppSport(sjUniWatch)

    override var appNotification = AppNotification(sjUniWatch)

    override var appDial = AppDial(sjUniWatch)

    override var appLanguage = AppLanguage(sjUniWatch)

    override val appMusicControl = AppMusicControl(sjUniWatch)

    override val appDateTime = AppDateTime(sjUniWatch)
}