package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmSoundAndHaptic
import com.base.sdk.entity.settings.WmUnitInfo
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentFunctionConfigBinding
import com.sjbt.sdk.sample.databinding.FragmentSoundTouchConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
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
 * 1. [SoundTouchFeedbackConfigFragment]
 * Display and modify
 */
//不需要连接也可以设置的
class SoundTouchFeedbackConfigFragment : BaseFragment(R.layout.fragment_sound_touch_config),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentSoundTouchConfigBinding by viewBinding()

    //    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmSoundAndHaptic? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate.wmSettings.settingSoundAndHaptic.get().toObservable().asFlow().collect{
                        config = it
                        updateUI()
                }
            }
            launch {
                UNIWatchMate.observeConnectState.asFlow().collect {
                    viewBind.layoutContent.setAllChildEnabled(it.equals(WmConnectState.VERIFIED))
                    updateUI()
                }
            }
            launch {
                UNIWatchMate.wmSettings.settingSoundAndHaptic.observeChange().asFlow().collect {
                        config = it
                        updateUI()
                }
            }
        }

        viewBind.itemIsCrownHasTactileFeedback.getSwitchCompat().setOnCheckedChangeListener(this)
//        viewBind.itemIsMute.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemIsNotificationTouch.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemIsSystemHasTactileFeedback.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemRingWhenCallComeIn.getSwitchCompat().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemIsCrownHasTactileFeedback.getSwitchCompat() -> {
                    config?.let {
                        it.isCrownHapticFeedback =isChecked
                    }
                }
//                viewBind.itemIsMute.getSwitchCompat() -> {
//                    config?.let {
//                        it.isMuted=isChecked
//                    }
//                }
                viewBind.itemIsNotificationTouch.getSwitchCompat() -> {
                    config?.let {
                        it.isNotificationHaptic=isChecked
                    }
                }
                viewBind.itemIsSystemHasTactileFeedback.getSwitchCompat() -> {
                    config?.let {
                        it.isSystemHapticFeedback=isChecked
                    }
                }
                viewBind.itemRingWhenCallComeIn.getSwitchCompat() -> {
                    config?.let {
                        it.isRingtoneEnabled=isChecked
                    }
                }
                else -> {
                    throw IllegalArgumentException()
                }
            }
            config?.saveConfig()
        }
    }

    private fun WmSoundAndHaptic.saveConfig() {
        applicationScope.launchWithLog {
            config?.let {
                UNIWatchMate.wmSettings.settingSoundAndHaptic.set(it).await()
            }
        }
        updateUI()
    }

    private fun updateUI() {
        config?.let {
            viewBind.itemRingWhenCallComeIn.getSwitchCompat().isChecked =
                it.isRingtoneEnabled
            viewBind.itemIsSystemHasTactileFeedback.getSwitchCompat().isChecked =
                it.isSystemHapticFeedback
            viewBind.itemIsCrownHasTactileFeedback.getSwitchCompat().isChecked =
                it.isCrownHapticFeedback
//            viewBind.itemIsMute.getSwitchCompat().isChecked =
//                it.isMuted
            viewBind.itemIsNotificationTouch.getSwitchCompat().isChecked =
                it.isNotificationHaptic
        }

    }
}