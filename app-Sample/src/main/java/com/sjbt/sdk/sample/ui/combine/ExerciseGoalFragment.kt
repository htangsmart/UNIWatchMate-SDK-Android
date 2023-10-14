package com.sjbt.sdk.sample.ui.combine

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmSedentaryReminder
import com.base.sdk.entity.settings.WmSportGoal
import com.blankj.utilcode.util.LogUtils
import com.github.kilnn.wheellayout.OneWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentExerciseGoalBinding
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.*
import com.sjbt.sdk.sample.utils.FormatterUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import kotlinx.coroutines.launch
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
class ExerciseGoalFragment : BaseFragment(R.layout.fragment_exercise_goal),
    SelectIntDialogFragment.Listener {

    private val viewBind: FragmentExerciseGoalBinding by viewBinding()

    private val applicationScope = Injector.getApplicationScope()

    private val deviceManager = Injector.getDeviceManager()
    private val exerciseGoalRepository = Injector.getExerciseGoalRepository()
    private val authedUserId = Injector.requireAuthedUserId()
    private var exerciseGoal: WmSportGoal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            launch {
                exerciseGoal = exerciseGoalRepository.flowCurrent.value
                updateUI()
            }
        }
        viewBind.itemStep.setOnClickListener(blockClick)
        viewBind.itemCalories.setOnClickListener(blockClick)
        viewBind.itemActivityDuration.setOnClickListener(blockClick)

        UNIWatchMate.wmSettings.settingSportGoal.get().subscribe{ it->
            UNIWatchMate.wmLog.logE("体育消息",""+it)
        }
    }

    private fun updateStep() {
        exerciseGoal?.let {
            viewBind.itemStep.getTextView().text =
                getString(R.string.unit_step_param, exerciseGoal!!.steps)
        }
    }

    private fun WmSportGoal.saveConfig() {
        applicationScope.launchWithLog {
            if (deviceManager.flowConnectorState.value == WmConnectState.VERIFIED) {
                UNIWatchMate.wmSettings.settingSportGoal.set(this@saveConfig).await()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        updateStep()
        updateCalories()
        updateActivityDuration()
    }

    private fun updateCalories() {
        exerciseGoal?.let {
            viewBind.itemCalories.getTextView().text = getString(
                R.string.unit_k_calories_param, exerciseGoal!!.calories.toString()
            )
        }

    }

    private fun updateActivityDuration() {
        exerciseGoal?.let {
            viewBind.itemActivityDuration.getTextView().text = getString(
                R.string.unit_minute_param, exerciseGoal!!.activityDuration
            )
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStep -> {
                exerciseGoal?.let { showExerciseStepDialog(it.steps) }
            }

            viewBind.itemCalories -> {
                exerciseGoal?.let { showExerciseCalorieDialog(it.calories) }
            }

            viewBind.itemActivityDuration -> {
                exerciseGoal?.let { showExerciseDurationDialog(it.activityDuration) }
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_EXERCISE_STEP == tag) {
            exerciseGoal = exerciseGoal?.copy(selectValue)
            updateStep()
            exerciseGoal?.let {
                exerciseGoalRepository.modify(authedUserId, it)
                it.saveConfig()
            }
        } else if (DIALOG_EXERCISE_CALORIE == tag) {
            exerciseGoal = exerciseGoal?.copy(calories = selectValue)
            updateCalories()
            exerciseGoal?.let {
                exerciseGoalRepository.modify(authedUserId, it)
                it.saveConfig()
            }
        } else if (DIALOG_EXERCISE_ACTIVITY_DURATION == tag) {
            exerciseGoal = exerciseGoal?.copy(activityDuration = selectValue.toShort())
            updateActivityDuration()
            exerciseGoal?.let {
                exerciseGoalRepository.modify(authedUserId, it)
                it.saveConfig()
            }
        }
    }


//    override fun dialogGetDistanceMetric(): Float {
//        return getHalfFloat(exerciseGoal!!.distance.toDouble())
//    }
//
//    override fun dialogSetDistanceMetric(value: Float) {
//        exerciseGoal = exerciseGoal?.copy(distance = value.toInt())
////        updateDistance()
//        exerciseGoal?.let { exerciseGoalRepository.modify(authedUserId, it) }
//    }

//    override fun dialogGetDistanceImperial(): Float {
////        return getHalfFloat(exerciseGoal.distance.km2mi())
//        return getHalfFloat(exerciseGoal!!.distance * 0.394)
//    }

//    override fun dialogSetDistanceImperial(value: Float) {
//        exerciseGoal = exerciseGoal.copy(distance = value * 0.394)
//        updateDistance()
//        exerciseGoalRepository.modify(authedUserId, exerciseGoal)
//    }

    /**
     * Convert to numbers in multiples of 0.5。Such as：
     * 0.24 -> 0
     * 0.25 -> 0.5
     * 0.49 -> 0.5
     * 1.74 -> 1.5
     */
    private fun getHalfFloat(value: Double): Float {
        if (value <= 0) return 0.0f
        val count1 = (value / 0.5f).toInt()
        val count2 = (value / 0.25f).toInt()
        return if (count1 * 2 != count2) (count1 + 1) * 0.5f else count1 * 0.5f
    }
}

