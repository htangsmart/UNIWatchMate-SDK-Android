package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.*
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.sjbt.sdk.SJUniWatch

class SJSettings(sjUniWatch: SJUniWatch) : AbWmSettings() {

    override val settingSportGoal: AbWmSetting<WmSportGoal> = SettingSportGoal(sjUniWatch)
    override val settingDateTime: AbWmSetting<WmDateTime> = SettingDateTime(sjUniWatch)
    override val settingPersonalInfo: AbWmSetting<WmPersonalInfo> = SettingPersonalInfo()
    override val settingSedentaryReminder: AbWmSetting<WmSedentaryReminder> = SettingSedentaryReminder()
    override val settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic> = SettingSoundAndHaptic(sjUniWatch)
    override val settingUnitInfo: AbWmSetting<WmUnitInfo> = SettingUnitInfo()
    override val settingWistRaise: AbWmSetting<WmWistRaise> = SettingWistRaise()
    override val settingAppView: AbWmSetting<WmAppView> = SettingAppView(sjUniWatch)
    override val settingDrinkWater: AbWmSetting<WmSedentaryReminder> = SettingSedentaryReminder()
    override val settingHeartRate: AbWmSetting<WmHeartRateAlerts> = SettingHeartRateAlerts()
    override val settingSleepSettings: AbWmSetting<WmSleepSettings> = SettingSleepSet(sjUniWatch)
}