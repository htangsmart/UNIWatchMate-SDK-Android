package com.sjbt.sdk.settings

import com.base.sdk.port.setting.AbWmSettings
import com.sjbt.sdk.SJUniWatch

class SJSettings(val sjUniWatch: SJUniWatch) : AbWmSettings() {

    override val settingSportGoal = SettingSportGoal(sjUniWatch)
    override val settingPersonalInfo = SettingPersonalInfo(sjUniWatch)
    override val settingSedentaryReminder = SettingSedentaryReminder(sjUniWatch)
    override val settingSoundAndHaptic = SettingSoundAndHaptic(sjUniWatch)
    override val settingUnitInfo = SettingUnitInfo(sjUniWatch)
    override val settingWistRaise = SettingWistRaise(sjUniWatch)
    override val settingAppView = SettingAppView(sjUniWatch)
    override val settingDrinkWater = SettingDrinkWaterReminder(sjUniWatch)
    override val settingHeartRate = SettingHeartRateAlerts()
    override val settingSleepSettings = SettingSleepSet(sjUniWatch)
}