package com.sjbt.sdk.sample.ui.combine

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.settings.WmSportGoal
import com.github.kilnn.wheellayout.OneWheelLayout
import com.github.kilnn.wheellayout.WheelIntConfig
import com.github.kilnn.wheellayout.WheelIntFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentExerciseGoalBinding
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.*
import com.sjbt.sdk.sample.utils.FormatterUtil


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
    SelectIntDialogFragment.Listener, DistanceMetricDialogFragment.Listener,
    DistanceImperialDialogFragment.Listener {

    private val viewBind: FragmentExerciseGoalBinding by viewBinding()

    private val internalStorage: InternalStorage = Injector.getInternalStorage()

    //    private val deviceManager = Injector.getDeviceManager()
    private val exerciseGoalRepository = Injector.getExerciseGoalRepository()
    private val authedUserId = Injector.requireAuthedUserId()
    private var isLengthMetric: Boolean = true
    private lateinit var exerciseGoal: WmSportGoal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        isLengthMetric = !deviceManager.configFeature.getFunctionConfig().isFlagEnabled(FcFunctionConfig.Flag.LENGTH_UNIT)
        exerciseGoal = exerciseGoalRepository.flowCurrent.value
//        exerciseGoal=WmSportGoal(1,2.0,3.0,4)
        lifecycleScope.launchWhenStarted {
            internalStorage.flowAuthedUserId.collect {

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStep()
        updateDistance()
        updateCalories()
        updateActivityDuration()
        viewBind.itemStep.setOnClickListener(blockClick)
        viewBind.itemDistance.setOnClickListener(blockClick)
        viewBind.itemCalories.setOnClickListener(blockClick)
        viewBind.itemActivityDuration.setOnClickListener(blockClick)

        UNIWatchMate.wmSettings.settingSportGoal.get().subscribe()
    }

    private fun updateStep() {
        viewBind.itemStep.getTextView().text =
            getString(R.string.unit_step_param, exerciseGoal.steps)
    }

    private fun updateDistance() {
//        if (isLengthMetric) {
//            viewBind.itemDistance.getTextView().text = getString(
//                R.string.unit_km_param, FormatterUtil.decimal1Str(getHalfFloat(exerciseGoal.distance))
//            )
//        } else {
//            viewBind.itemDistance.getTextView().text = getString(
//                R.string.unit_mi_param, FormatterUtil.decimal1Str(getHalfFloat(exerciseGoal.distance*0.394))
//            )
//        }
    }

    private fun updateCalories() {
        viewBind.itemCalories.getTextView().text = getString(
            R.string.unit_k_calories_param, exerciseGoal.calories.toString()
        )
    }

    private fun updateActivityDuration() {
        viewBind.itemActivityDuration.getTextView().text = getString(
            R.string.unit_minute_param, exerciseGoal.activityDuration
        )
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStep -> {
                showExerciseStepDialog(exerciseGoal.steps)
            }

            viewBind.itemDistance -> {
                if (isLengthMetric) {
                    DistanceMetricDialogFragment().show(childFragmentManager, null)
                } else {
                    DistanceImperialDialogFragment().show(childFragmentManager, null)
                }
            }

            viewBind.itemCalories -> {
                showExerciseCalorieDialog(exerciseGoal.calories)
            }
            viewBind.itemActivityDuration -> {
                showExerciseDurationDialog(exerciseGoal.activityDuration)
            }
        }
    }

    override fun onDialogSelectInt(tag: String?, selectValue: Int) {
        if (DIALOG_EXERCISE_STEP == tag) {
            exerciseGoal = exerciseGoal.copy(selectValue)
            updateStep()
            exerciseGoalRepository.modify(authedUserId, exerciseGoal)
        } else if (DIALOG_EXERCISE_CALORIE == tag) {
            exerciseGoal = exerciseGoal.copy(calories = selectValue)
            updateCalories()
            exerciseGoalRepository.modify(authedUserId, exerciseGoal)
        } else if (DIALOG_EXERCISE_ACTIVITY_DURATION == tag) {
            exerciseGoal = exerciseGoal.copy(activityDuration = selectValue.toShort())
            updateActivityDuration()
            exerciseGoalRepository.modify(authedUserId, exerciseGoal)
        }

        setExerciseGoal()
    }

    private fun setExerciseGoal() {
        UNIWatchMate.wmSettings.settingSportGoal.set(exerciseGoal)
    }

    override fun dialogGetDistanceMetric(): Float {
        return getHalfFloat(exerciseGoal.distance.toDouble())
    }

    override fun dialogSetDistanceMetric(value: Float) {
        exerciseGoal = exerciseGoal.copy(distance = value.toInt())
        updateDistance()
        exerciseGoalRepository.modify(authedUserId, exerciseGoal)
    }

    override fun dialogGetDistanceImperial(): Float {
//        return getHalfFloat(exerciseGoal.distance.km2mi())
        return getHalfFloat(exerciseGoal.distance * 0.394)
    }

    override fun dialogSetDistanceImperial(value: Float) {
//        exerciseGoal = exerciseGoal.copy(distance = value * 0.394)
//        updateDistance()
//        exerciseGoalRepository.modify(authedUserId, exerciseGoal)
    }

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

class DistanceMetricDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val multiples = 0.5f
        val listener = parentFragment as? Listener

        val layout = OneWheelLayout(requireContext())
        layout.setConfig(
            WheelIntConfig(
                2,
                80,
                false,
                getString(R.string.unit_km),
                object : WheelIntFormatter {
                    override fun format(index: Int, value: Int): String {
                        return FormatterUtil.decimal1Str(value * multiples)
                    }
                })
        )

        if (listener != null) {
            layout.setValue((listener.dialogGetDistanceMetric() / multiples).toInt())
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exercise_goal_distance)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.dialogSetDistanceMetric(layout.getValue() * multiples)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun dialogGetDistanceMetric(): Float
        fun dialogSetDistanceMetric(value: Float)
    }
}

class DistanceImperialDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val multiples = 0.5f
        val listener = parentFragment as? Listener

        val layout = OneWheelLayout(requireContext())
        layout.setConfig(
            WheelIntConfig(
                1,
                50,
                false,
                getString(R.string.unit_mi),
                object : WheelIntFormatter {
                    override fun format(index: Int, value: Int): String {
                        return FormatterUtil.decimal1Str(value * multiples)
                    }
                })
        )

        if (listener != null) {
            layout.setValue((listener.dialogGetDistanceImperial() / multiples).toInt())
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.exercise_goal_distance)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                listener?.dialogSetDistanceImperial(layout.getValue() * multiples)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Listener {
        fun dialogGetDistanceImperial(): Float
        fun dialogSetDistanceImperial(value: Float)
    }
}
