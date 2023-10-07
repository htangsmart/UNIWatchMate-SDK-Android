package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmWistRaise
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentTurnWristLightingConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.topstep.fitcloud.sdk.v2.model.config.FcTurnWristLightingConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcturnwristlightingconfig
 *
 * ***Description**
 * Display and modify the config of raise hand to lighting the device
 *
 * **Usage**
 * 1. [TurnWristLightingConfigFragment]
 * Display and modify
 */
class TurnWristLightingConfigFragment : BaseFragment(R.layout.fragment_turn_wrist_lighting_config),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentTurnWristLightingConfigBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: WmWistRaise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmSettings.settingWistRaise.observeChange().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.wmConnect.observeConnectState.asFlow().collect {
                    viewBind.layoutContent.setAllChildEnabled(it.equals(WmConnectState.VERIFIED))
                    updateUI()
                }
            }
            launch {
                config = UNIWatchMate.wmSettings.settingWistRaise.get().blockingGet()
                updateUI()
            }
        }

        viewBind.itemIsEnabled.getSwitchView().setOnCheckedChangeListener(this)

    }
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemIsEnabled.getSwitchView()) {
                config.isScreenWakeEnabled = isChecked
            }
        }
    }

    private fun FcTurnWristLightingConfig.saveConfig() {
        applicationScope.launchWithLog {
            UNIWatchMate.wmSettings.settingWistRaise.set(config).await()
        }
        updateUI()
    }

    private fun updateUI() {
        val isConfigEnabled = viewBind.layoutContent.isEnabled

        viewBind.itemIsEnabled.getSwitchView().isChecked = config.isScreenWakeEnabled
        if (isConfigEnabled) {//When device is disconnected, disabled the click event
            viewBind.layoutContent.setAllChildEnabled(config.isScreenWakeEnabled)
        }
    }

}