package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.settings.WmSleepSettings
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentSleepConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.dialog.*
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcdndconfig
 *
 * ***Description**
 * Display and modify the dnd config
 *
 * **Usage**
 * 1. [DeviceConfigFragment]
 * According to whether [FcDeviceInfo.Feature.DND] supports, show or hide the entrance
 *
 * 2.[DNDConfigFragment]
 * Display and modify
 */
class SleepConfigFragment : BaseFragment(R.layout.fragment_sleep_config),
    CompoundButton.OnCheckedChangeListener, TimePickerDialogFragment.Listener {

    private val viewBind: FragmentSleepConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmSleepSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                    updateUI()
                }
            }
            launch {
                UNIWatchMate?.wmSettings?.settingSleepSettings?.observeChange()?.asFlow()?.collect {
                    config = it
                    updateUI()
                }
            }
            launch {
                UNIWatchMate?.wmSettings?.settingSleepSettings?.get()?.toObservable()?.asFlow()
                    ?.collect {
                        config = it
                        updateUI()
                    }
            }
        }

        viewBind.itemSleepEnable.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemStartTime.setOnClickListener { blockClick }
        viewBind.itemEndTime.setOnClickListener(blockClick)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemSleepEnable.getSwitchView()) {
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemStartTime -> {
                config?.let {
                    showStartTimeDialog(it.startHour * 60 + it.startMinute)
                }
            }
            viewBind.itemEndTime -> {
                config?.let {
                    showEndTimeDialog(it.endHour * 60 + it.endMinute)
                }
            }
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
        if (DIALOG_START_TIME == tag) {
//            config.toBuilder().setStart(timeMinute).create().saveConfig()
        } else if (DIALOG_END_TIME == tag) {
//            config.toBuilder().setEnd(timeMinute).create().saveConfig()
        }
        config?.saveConfig()
    }

    private fun WmSleepSettings.saveConfig() {
        applicationScope.launchWithLog {
            UNIWatchMate?.wmSettings?.settingSleepSettings?.set(config!!).await()
        }
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

//        viewBind.itemAllDay.getSwitchView().isChecked = config.isEnabledAllDay()
//        viewBind.itemPeriodTime.getSwitchView().isChecked = config.isEnabledPeriodTime()
//        if (isConfigEnabled) {//When device is disconnected, disabled the click event
//            viewBind.itemStartTime.isEnabled = config.isEnabledPeriodTime()
//            viewBind.itemEndTime.isEnabled = config.isEnabledPeriodTime()
//        }
//        viewBind.itemStartTime.getTextView().text = FormatterUtil.minute2Hmm(config.getStart())
//        viewBind.itemEndTime.getTextView().text = FormatterUtil.minute2Hmm(config.getEnd())
    }

}