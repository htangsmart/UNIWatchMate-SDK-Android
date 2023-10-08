package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.entity.settings.*

internal class AbWmSettingsDelegate(
    private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmSettings() {

    override var settingSportGoal: AbWmSetting<WmSportGoal> =
        watchObservable.value!!.wmSettings?.settingSportGoal
    override var settingDateTime: AbWmSetting<WmDateTime> =
        watchObservable.value!!.wmSettings?.settingDateTime
    override var settingPersonalInfo: AbWmSetting<WmPersonalInfo> =
        watchObservable.value!!.wmSettings?.settingPersonalInfo
    override var settingSedentaryReminder: AbWmSetting<WmSedentaryReminder> =
        watchObservable.value!!.wmSettings?.settingSedentaryReminder
    override var settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic> =
        watchObservable.value!!.wmSettings?.settingSoundAndHaptic
    override var settingUnitInfo: AbWmSetting<WmUnitInfo> =
        watchObservable.value!!.wmSettings?.settingUnitInfo
    override var settingWistRaise: AbWmSetting<WmWistRaise> =
        watchObservable.value!!.wmSettings?.settingWistRaise
    override var settingAppView: AbWmSetting<WmAppView> =
        watchObservable.value!!.wmSettings?.settingAppView
    override var settingDrinkWater: AbWmSetting<WmSedentaryReminder> =
        watchObservable.value!!.wmSettings?.settingDrinkWater
    override var settingHeartRate: AbWmSetting<WmHeartRateAlerts> =
        watchObservable.value!!.wmSettings?.settingHeartRate
    override val settingSleepSettings: AbWmSetting<WmSleepSettings> =
        watchObservable.value!!.wmSettings?.settingSleepSettings
}