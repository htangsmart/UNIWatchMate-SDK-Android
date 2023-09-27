package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.settings.WmUnitInfo
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentFunctionConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcfunctionconfig
 *
 * ***Description**
 * Display and modify the simple functions on the device
 *
 * **Usage**
 * 1. [FunctionConfigFragment]
 * Display and modify
 */
//不需要连接也可以设置的
class FunctionConfigFragment : BaseFragment(R.layout.fragment_function_config),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentFunctionConfigBinding by viewBinding()

    //    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmUnitInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                config = UNIWatchMate.wmSettings.settingUnitInfo.get().blockingGet()
            }

            launch {
                UNIWatchMate.wmSettings.settingUnitInfo.observeChange().asFlow().collect {
                    if (config != it) {
                        config = it
                        updateUI()
                    }
                }
            }
        }

//        viewBind.itemWearRightHand.getSwitchCompat().setOnCheckedChangeListener(this)
//        viewBind.itemEnhancedMeasurement.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemTimeFormat12Hour.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemLengthUnitImperial.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemTemperatureUnitFahrenheit.getSwitchCompat().setOnCheckedChangeListener(this)
//        viewBind.itemDisplayWeather.getSwitchCompat().setOnCheckedChangeListener(this)
//        viewBind.itemDisconnectReminder.getSwitchCompat().setOnCheckedChangeListener(this)
//        viewBind.itemDisplayExerciseGoal.getSwitchCompat().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemTimeFormat12Hour.getSwitchCompat() -> {
                    config?.let {
                        it.timeFormat =
                            if (isChecked) WmUnitInfo.TimeFormat.TWELVE_HOUR else WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR
                    }
                }
                viewBind.itemLengthUnitImperial.getSwitchCompat() -> {
                    config?.let {
                        it.lengthUnit =
                            if (isChecked) WmUnitInfo.LengthUnit.CM else WmUnitInfo.LengthUnit.INCH
                    }
                }
                viewBind.itemTemperatureUnitFahrenheit.getSwitchCompat() -> {
                    config?.let {
                        it.temperatureUnit =
                            if (isChecked) WmUnitInfo.TemperatureUnit.CELSIUS else WmUnitInfo.TemperatureUnit.FAHRENHEIT
                    }
                }

                else -> {
                    throw IllegalArgumentException()
                }
            }
            config?.saveConfig()
        }
    }

    private fun WmUnitInfo.saveConfig() {
        applicationScope.launchWithLog {
            config?.let {
                UNIWatchMate.wmSettings.settingUnitInfo.set(it).await()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        config?.let {
            viewBind.itemTimeFormat12Hour.getSwitchCompat().isChecked =
                it.timeFormat == WmUnitInfo.TimeFormat.TWELVE_HOUR
            viewBind.itemLengthUnitImperial.getSwitchCompat().isChecked =
                it.lengthUnit == WmUnitInfo.LengthUnit.CM
            viewBind.itemTemperatureUnitFahrenheit.getSwitchCompat().isChecked =
                it.temperatureUnit == WmUnitInfo.TemperatureUnit.CELSIUS
        }

    }
}