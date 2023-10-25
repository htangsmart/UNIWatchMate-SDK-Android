package com.sjbt.sdk.sample.ui.setting.notification

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.navigation.fragment.findNavController
import com.base.sdk.entity.apps.WmNotification
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentNotificationConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.ui.DeviceFragmentDirections
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

/**
 * **Document**
 * https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/04.Device-info-and-configs#fcnotificationconfig
 *
 * ***Description**
 * Display and modify the config of Notification
 *
 * **Usage**
 * 1.[NotificationConfigFragment]
 * Display and modify
 */
class NotificationConfigFragment : BaseFragment(R.layout.fragment_notification_config),
    CompoundButton.OnCheckedChangeListener {

    private val viewBind: FragmentNotificationConfigBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()

    //    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private lateinit var config: WmNotification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        config = deviceManager.configFeature.getNotificationConfig()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                }
            }

            launch {
//                通知设置是新的，通知是旧的
//                UNIWatchMate.wmApps.appNotification.sendNotification(WmNotification())?.asFlow()?.collect {
//                    config = it
//                    updateUI()
//                }
            }

        }

        viewBind.itemTelephony.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemSms.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemQq.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemWechat.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemFacebook.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemTwitter.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemInstagram.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemWhatsapp.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemLine.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemMessenger.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemKakaoTalk.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemSkype.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemEmail.getSwitchView().setOnCheckedChangeListener(this)
        viewBind.itemOthers.setOnClickListener {
            findNavController().navigate(NotificationConfigFragmentDirections.toOtherNotification())
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemTelephony.getSwitchView() -> {
                    if (isChecked) {

                    } else {
                    }
                }

                viewBind.itemSms.getSwitchView() -> {
                    if (isChecked) {

                    } else {
                    }
                }

                viewBind.itemQq.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                    }
                }

                viewBind.itemWechat.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                    }
                }

                viewBind.itemFacebook.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                    }
                }

                viewBind.itemTwitter.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
                    }
                }

                viewBind.itemInstagram.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.INSTAGRAM)
                    }
                }

                viewBind.itemWhatsapp.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.WHATSAPP)
                    }
                }

                viewBind.itemLine.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.LINE)
                    }
                }

                viewBind.itemMessenger.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.FACEBOOK_MESSENGER)
                    }
                }

                viewBind.itemKakaoTalk.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.KAKAO)
                    }
                }

                viewBind.itemSkype.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.SKYPE)
                    }
                }

                viewBind.itemEmail.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.EMAIL)
                    }
                }

                viewBind.itemOthers.getSwitchView() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.OTHERS_APP)
                    }
                }
            }
        }
    }

//    private fun FcNotificationConfig.saveConfig() {
//        applicationScope.launchWithLog {
//            deviceManager.configFeature.setNotificationConfig(this@saveConfig).await()
//        }
//        this@NotificationConfigFragment.config = this
//        updateUI()
//    }
//
//    private fun changeFlags(isChecked: Boolean, flag: Int) {
//        config.toBuilder().setFlagEnabled(flag, isChecked).create().saveConfig()
}

private fun checkNotificationPermission(compoundButton: CompoundButton): Boolean {
//        if (NotificationListenerServiceUtil.isEnabled(requireContext())) return true
//        CaptureNotificationDialogFragment().show(childFragmentManager, null)
//        compoundButton.isChecked = false
    return false
}
