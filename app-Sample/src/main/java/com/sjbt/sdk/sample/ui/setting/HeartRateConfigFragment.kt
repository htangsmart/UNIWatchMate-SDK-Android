package com.sjbt.sdk.sample.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.github.kilnn.wheellayout.OneWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.databinding.FragmentHeartRateConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.*
import com.sjbt.sdk.sample.utils.FormatterUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/10.Other-Features#setting-exercise-goal
 *
 * ***Description**
 * Display and modify the exercise goal
 *
 * **Usage**
 * 1. [ExerciseGoalFragment]
 * Display and modify
 *
 * 2.[DeviceManager]
 * Set the exercise goal to device when device connected or goal changed.
 */
class HeartRateConfigFragment : BaseFragment(R.layout.fragment_heart_rate_config),
    SelectIntDialogFragment.Listener {

    private val viewBind: FragmentHeartRateConfigBinding by viewBinding()
    private val applicationScope = Injector.getApplicationScope()

    private val deviceManager = Injector.getDeviceManager()
    private var isLengthMetric: Boolean = true
    private var wmHeartRateAlerts: WmHeartRateAlerts? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        isLengthMetric = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
//        exerciseGoal=WmSportGoal(1,2.0,3.0,4)
        wmHeartRateAlerts = WmHeartRateAlerts(21)

        lifecycleScope.launchWhenStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                }

                launch {
                    UNIWatchMate.wmSettings.settingHeartRate.observeChange().asFlow().collect {
                        wmHeartRateAlerts = it
                        updateUi()
                    }
                }
                launch {
                    UNIWatchMate.wmSettings.settingHeartRate.get().toObservable().asFlow().collect {
                        wmHeartRateAlerts = it
                        updateUi()
                    }
                }
            }
        }

    }

    private fun updateUi() {
        wmHeartRateAlerts?.let {
            viewBind.itemAutoHeartRateMeasurementSwitch.getSwitchView().isChecked =
                it.isEnableHrAutoMeasure

            viewBind.itemMaxHeartRate.getTextView().text =
                it.maxHeartRate.toString() + getString(R.string.unit_bmp)
            viewBind.itemHeartRateInterval.text =
                getString(R.string.ds_heart_rate_interva) + ":\n0-${WmHeartRateAlerts.HEART_RATE_INTERVALS[0]}-${WmHeartRateAlerts.HEART_RATE_INTERVALS[1]}" +
                        "-${WmHeartRateAlerts.HEART_RATE_INTERVALS[2]}-${WmHeartRateAlerts.HEART_RATE_INTERVALS[3]}-${WmHeartRateAlerts.HEART_RATE_INTERVALS[4]}"

            viewBind.itemExerciseHeartRateHighAlertSwitch.getSwitchView().isChecked =
                it.exerciseHeartRateAlert.threshold > 0
            viewBind.itemExerciseHeartRateHighAlert.getTextView().text =
                it.exerciseHeartRateAlert.threshold.toString() + getString(R.string.unit_bmp)
            viewBind.itemExerciseHeartRateHighAlert.isEnabled =
                it.exerciseHeartRateAlert.threshold > 0

            viewBind.itemQuietHeartRateHighAlertSwitch.getSwitchView().isChecked =
                it.restingHeartRateAlert.threshold > 0
            viewBind.itemQuietHeartRateHighAlert.getTextView().text =
                it.restingHeartRateAlert.threshold.toString() + getString(R.string.unit_bmp)
            viewBind.itemQuietHeartRateHighAlert.isEnabled = it.restingHeartRateAlert.threshold > 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUi()
        viewBind.itemMaxHeartRate.setOnClickListener {
            wmHeartRateAlerts?.let {
                showHeartRateDialog(it.maxHeartRate, 10, 22, DIALOG_MAX_HEART_RATE)
            }
        }
        viewBind.itemExerciseHeartRateHighAlert.setOnClickListener {
            wmHeartRateAlerts?.let {
                showHeartRateDialog(
                    it.exerciseHeartRateAlert.threshold,
                    10,
                    15,
                    DIALOG_EXERCISE_HEART_RATE_HIGH_ALERT
                )
            }
        }
        viewBind.itemQuietHeartRateHighAlert.setOnClickListener {
            wmHeartRateAlerts?.let {
                showHeartRateDialog(
                    it.restingHeartRateAlert.threshold,
                    10,
                    15,
                    DIALOG_QUIET_HEART_RATE_HIGH_ALERT
                )
            }
        }
        viewBind.itemAutoHeartRateMeasurementSwitch.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                wmHeartRateAlerts?.let {
                    it.isEnableHrAutoMeasure = isChecked
                    updateUi()
                }
            }

        viewBind.itemExerciseHeartRateHighAlertSwitch.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                wmHeartRateAlerts?.let {
                    if (isChecked) {
                        it.exerciseHeartRateAlert.threshold =
                            WmHeartRateAlerts.THRESHOLDS[1]
                    } else {
                        it.exerciseHeartRateAlert.threshold = 0
                    }
                    it.save()
                    updateUi()
                }
            }
        viewBind.itemQuietHeartRateHighAlertSwitch.getSwitchView()
            .setOnCheckedChangeListener { buttonView, isChecked ->
                wmHeartRateAlerts?.let {
                    if (isChecked) {
                        it.restingHeartRateAlert.threshold =
                            WmHeartRateAlerts.THRESHOLDS[1]
                    } else {
                        it.restingHeartRateAlert.threshold = 0
                    }
                    it.save()
                    updateUi()

                }
            }
    }

    private fun WmHeartRateAlerts.save() {
        applicationScope.launchWithLog {
            UNIWatchMate.wmSettings.settingHeartRate.set(this@save).await()
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_MAX_HEART_RATE == tag) {
            wmHeartRateAlerts?.let {
                it.maxHeartRate = selectValue
                it.save()
            }
            updateUi()
        } else if (DIALOG_EXERCISE_HEART_RATE_HIGH_ALERT == tag) {
            wmHeartRateAlerts?.let {
                it.exerciseHeartRateAlert.threshold = selectValue
                it.save()
            }
            updateUi()
        } else if (DIALOG_QUIET_HEART_RATE_HIGH_ALERT == tag) {
            wmHeartRateAlerts?.let {
                it.restingHeartRateAlert.threshold = selectValue
                it.save()
            }
            updateUi()
        }
    }

}



