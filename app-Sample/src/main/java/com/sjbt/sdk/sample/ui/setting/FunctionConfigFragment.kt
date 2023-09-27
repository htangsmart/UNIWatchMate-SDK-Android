package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentFunctionConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

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
class FunctionConfigFragment : BaseFragment(R.layout.fragment_function_config), CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentFunctionConfigBinding by viewBinding()

//    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

//    private lateinit var config: FcFunctionConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        config = deviceManager.configFeature.getFunctionConfig()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
//                deviceManager.flowStateConnected().collect {
//                    viewBind.layoutContent.setAllChildEnabled(it)
//                    updateUI()
//                }
            }
            launch {
//                deviceManager.configFeature.observerFunctionConfig().asFlow().collect {
//                    if (config != it) {
//                        config = it
//                        updateUI()
//                    }
//                }
            }
        }

        viewBind.itemWearRightHand.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemEnhancedMeasurement.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemTimeFormat12Hour.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemLengthUnitImperial.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemTemperatureUnitFahrenheit.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemDisplayWeather.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemDisconnectReminder.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemDisplayExerciseGoal.getSwitchCompat().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
//            val flag = when (buttonView) {
//                viewBind.itemWearRightHand.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.WEAR_WAY
//                }
//                viewBind.itemEnhancedMeasurement.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.ENHANCED_MEASUREMENT
//                }
//                viewBind.itemTimeFormat12Hour.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.TIME_FORMAT
//                }
//                viewBind.itemLengthUnitImperial.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.LENGTH_UNIT
//                }
//                viewBind.itemTemperatureUnitFahrenheit.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.TEMPERATURE_UNIT
//                }
//                viewBind.itemDisplayWeather.getSwitchCompat() -> {
//                    if (isChecked) {
//                        //General weather function depends on location function
//                        if (!SystemUtil.isLocationEnabled(requireContext())) {
//                            buttonView.isChecked = false
//                            requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                            return
//                        }
//                        PermissionHelper.requestWeatherLocation(this) {
//                        }
//                    }
//                    FcFunctionConfig.Flag.WEATHER_DISPLAY
//                }
//                viewBind.itemDisconnectReminder.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.DISCONNECT_REMINDER
//                }
//                viewBind.itemDisplayExerciseGoal.getSwitchCompat() -> {
//                    FcFunctionConfig.Flag.EXERCISE_GOAL_DISPLAY
//                }
//                else -> {
//                    throw IllegalArgumentException()
//                }
//            }
//            config.toBuilder().setFlagEnabled(flag, isChecked).create().saveConfig()
        }
    }

//    private fun FcFunctionConfig.saveConfig() {
//        applicationScope.launchWithLog {
//            deviceManager.configFeature.setFunctionConfig(this@saveConfig).await()
//        }
//        this@FunctionConfigFragment.config = this
//        updateUI()
//    }

    private fun updateUI() {
//        viewBind.itemWearRightHand.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.WEAR_WAY)
//        viewBind.itemEnhancedMeasurement.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.ENHANCED_MEASUREMENT)
//        viewBind.itemTimeFormat12Hour.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.TIME_FORMAT)
//        viewBind.itemLengthUnitImperial.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
//        viewBind.itemTemperatureUnitFahrenheit.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.TEMPERATURE_UNIT)
//        viewBind.itemDisplayWeather.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.WEATHER_DISPLAY)
//        viewBind.itemDisconnectReminder.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.DISCONNECT_REMINDER)
//        viewBind.itemDisplayExerciseGoal.getSwitchCompat().isChecked = config.isFlagEnabled(FcFunctionConfig.Flag.EXERCISE_GOAL_DISPLAY)
    }
}