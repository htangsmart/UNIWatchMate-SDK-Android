package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.*
import com.base.sdk.`interface`.setting.AbWmSetting
import com.base.sdk.`interface`.setting.AbWmSettings
import com.sjbt.sdk.SJUniWatch

class SJSettings(sjUniWatch: SJUniWatch) : AbWmSettings() {

    override val settingSportGoal: AbWmSetting<WmSportGoal> = SettingSportGoal(sjUniWatch)
    override val settingDateTime: AbWmSetting<WmDateTime> = SettingDateTime()
    override val settingPersonalInfo: AbWmSetting<WmPersonalInfo> = SettingPersonalInfo()
    override val settingSedentaryReminder: AbWmSetting<WmSedentaryReminder> = SettingSedentaryReminder()
    override val settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic> = SettingSoundAndHaptic()
    override val settingUnitInfo: AbWmSetting<WmUnitInfo> = SettingUnitInfo()
    override val settingWistRaise: AbWmSetting<WmWistRaise> = SettingWistRaise()
    override val settingAppView: AbWmSetting<WmAppView> = SettingAppView()
    override val settingDrinkWater: AbWmSetting<WmSedentaryReminder> = SettingSedentaryReminder()
    override val settingHeartRate: AbWmSetting<WmHeartRateAlerts> = SettingHeartRateAlerts()
}