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
import com.sjbt.sdk.sample.dialog.TimePickerDialogFragment
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcDNDConfig
import com.topstep.fitcloud.sdk.v2.model.config.FcDeviceInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

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

//        viewBind.itemPeriodTime.getSwitchView().setOnCheckedChangeListener(this)
//        viewBind.itemStartTime.clickTrigger(block = blockClick)
//        viewBind.itemEndTime.clickTrigger(block = blockClick)
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
//                showStartTimeDialog(config.startHour())
            }
            viewBind.itemEndTime -> {
//                showEndTimeDialog(config.getEnd())
            }
        }
    }

    override fun onDialogTimePicker(tag: String?, timeMinute: Int) {
//        if (DIALOG_START_TIME == tag) {
//            config.toBuilder().setStart(timeMinute).create().saveConfig()
//        } else if (DIALOG_END_TIME == tag) {
//            config.toBuilder().setEnd(timeMinute).create().saveConfig()
//        }
    }

    private fun FcDNDConfig.saveConfig() {
//        applicationScope.launchWithLog {
//            deviceManager.configFeature.setDNDConfig(this@saveConfig).await()
//        }
//        this@DNDConfigFragment.config = this
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled
//        ewBind.itemAllDay.getSwitchView().isChecked = config.isEnabledAllDay()
//        viewBind.itemPeriodTime.getSwitchView().isChecked = config.isEnabledPeriodTime()
//        if (isConfigEnabled) {//When device is disconnected, disabled the click event
//            viewBind.itemStartTime.isEnabled = config.isEnabledPeriodTime()
//            viewBind.itemEndTime.isEnabled = config.isEnabledPeriodTime()
//        }
//        viewBind.itemStartTime.getTextView().text = FormatterUtil.minute2Hmm(config.getStart())
//        viewBind.ite
//        vimEndTime.getTextView().text = FormatterUtil.minute2Hmm(config.getEnd())
    }

}