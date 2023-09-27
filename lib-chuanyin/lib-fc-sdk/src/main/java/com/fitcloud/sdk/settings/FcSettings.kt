package com.fitcloud.sdk.settings

import com.base.sdk.`interface`.setting.AbWmSetting
import com.base.sdk.`interface`.setting.AbWmSettings
import com.base.sdk.entity.settings.*
import com.topstep.fitcloud.sdk.v2.FcConnector

class FcSettings(
    connector: FcConnector
) : AbWmSettings() {

    override val settingSportGoal: AbWmSetting<WmSportGoal> = SettingSportGoal(connector)

    override val settingDateTime: AbWmSetting<WmDateTime>
        get() = TODO("Not yet implemented")
    override val settingPersonalInfo: AbWmSetting<WmPersonalInfo>
        get() = TODO("Not yet implemented")
    override val settingSedentaryReminder: AbWmSetting<WmSedentaryReminder>
        get() = TODO("Not yet implemented")
    override val settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic>
        get() = TODO("Not yet implemented")
    override val settingUnitInfo: AbWmSetting<WmUnitInfo>
        get() = TODO("Not yet implemented")
    override val settingWistRaise: AbWmSetting<WmWistRaise>
        get() = TODO("Not yet implemented")
    override val settingAppView: AbWmSetting<WmAppView>
        get() = TODO("Not yet implemented")
    override val settingDrinkWater: AbWmSetting<WmSedentaryReminder>
        get() = TODO("Not yet implemented")
    override val settingHeartRate: AbWmSetting<WmHeartRateAlerts>
        get() = TODO("Not yet implemented")

}