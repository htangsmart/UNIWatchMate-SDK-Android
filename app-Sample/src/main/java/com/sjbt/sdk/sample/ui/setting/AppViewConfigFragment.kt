package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmAppView
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentAppViewBinding
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
 * 1. [AppViewConfigFragment]
 * Display and modify
 */
class AppViewConfigFragment : BaseFragment(R.layout.fragment_app_view),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentAppViewBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmAppView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmSettings.settingAppView.observeChange().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.observeConnectState.asFlow().collect {
                    viewBind.llContent.setAllChildEnabled(it.equals(WmConnectState.VERIFIED))
                    updateUI()
                }
            }
            launch {
                config = UNIWatchMate.wmSettings.settingAppView.get().blockingGet()
                updateUI()
            }
        }

        viewBind.itemAppViewGridding.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemAppViewList.getSwitchView().setOnCheckedChangeListener(this)

    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            if (buttonView == viewBind.itemAppViewGridding.getSwitchView()) {
//                config?.appViewList = isChecked
            } else if (buttonView == viewBind.itemAppViewList.getSwitchView()) {

            }
        }
    }

    private fun FcTurnWristLightingConfig.saveConfig() {
        applicationScope.launchWithLog {
            config?.let { UNIWatchMate.wmSettings.settingAppView.set(it).await() }
        }
        updateUI()
    }

    private fun updateUI() {

    }

}