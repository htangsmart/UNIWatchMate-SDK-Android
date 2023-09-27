package com.sjbt.sdk.sample.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDialogFragment
import com.base.sdk.entity.apps.WmNotification
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentNotificationConfigBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
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
//                deviceManager.flowStateConnected().collect {
//                    viewBind.layoutContent.setAllChildEnabled(it)
//                    updateUI()
//                }
            }
            launch {
//                deviceManager.configFeature.observerNotificationConfig().asFlow().collect {
////                    if (config != it) {
////                        config = it
////                        updateUI()
////                    }
//                }
            }
        }

        viewBind.itemTelephony.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemSms.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemQq.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemWechat.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemFacebook.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemTwitter.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemInstagram.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemWhatsapp.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemLine.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemMessenger.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemKakaoTalk.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemSkype.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemEmail.getSwitchCompat().setOnCheckedChangeListener(this)
        viewBind.itemOthers.getSwitchCompat().setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.isPressed) {
            when (buttonView) {
                viewBind.itemTelephony.getSwitchCompat() -> {
                    if (isChecked) {
                        PermissionHelper.requestTelephony(this) {
                            if (PermissionHelper.hasReceiveTelephony(requireContext())) {
//                                changeFlags(true, WmNotificationType.Flag.TELEPHONY)
                            } else {
                                buttonView.isChecked = false
                            }
                        }
                    } else {
//                        changeFlags(false, FcNotificationConfig.Flag.TELEPHONY)
                    }
                }

                viewBind.itemSms.getSwitchCompat() -> {
                    if (isChecked) {
                        PermissionHelper.requestSms(this) {
                            if (PermissionHelper.hasReceiveSms(requireContext())) {
//                                changeFlags(true, FcNotificationConfig.Flag.SMS)
                            } else {
                                buttonView.isChecked = false
                            }
                        }
                    } else {
//                        changeFlags(false, FcNotificationConfig.Flag.SMS)
                    }
                }

                viewBind.itemQq.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.QQ)
                    }
                }

                viewBind.itemWechat.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.WECHAT)
                    }
                }

                viewBind.itemFacebook.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.FACEBOOK)
                    }
                }

                viewBind.itemTwitter.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.TWITTER)
                    }
                }

                viewBind.itemInstagram.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.INSTAGRAM)
                    }
                }

                viewBind.itemWhatsapp.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.WHATSAPP)
                    }
                }

                viewBind.itemLine.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.LINE)
                    }
                }

                viewBind.itemMessenger.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.FACEBOOK_MESSENGER)
                    }
                }

                viewBind.itemKakaoTalk.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.KAKAO)
                    }
                }

                viewBind.itemSkype.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.SKYPE)
                    }
                }

                viewBind.itemEmail.getSwitchCompat() -> {
                    if (checkNotificationPermission(buttonView)) {
//                        changeFlags(isChecked, FcNotificationConfig.Flag.EMAIL)
                    }
                }

                viewBind.itemOthers.getSwitchCompat() -> {
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

private fun updateUI() {
//        viewBind.itemTelephony.getSwitchCompat().isChecked = config.isFlagEnabled(FcNotificationConfig.Flag.TELEPHONY) && PermissionHelper.hasReceiveTelephony(requireContext())
//        viewBind.itemSms.getSwitchCompat().isChecked = config.isFlagEnabled(FcNotificationConfig.Flag.SMS) && PermissionHelper.hasReceiveSms(requireContext())
//
//        val isNLSEnabled = NotificationListenerServiceUtil.isEnabled(requireContext())
//        val deviceInfo = deviceManager.configFeature.getDeviceInfo()
//
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemQq, FcNotificationConfig.Flag.QQ)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemWechat, FcNotificationConfig.Flag.WECHAT)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemFacebook, FcNotificationConfig.Flag.FACEBOOK)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemTwitter, FcNotificationConfig.Flag.TWITTER)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemInstagram, FcNotificationConfig.Flag.INSTAGRAM)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemWhatsapp, FcNotificationConfig.Flag.WHATSAPP)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemLine, FcNotificationConfig.Flag.LINE)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemMessenger, FcNotificationConfig.Flag.FACEBOOK_MESSENGER)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemKakaoTalk, FcNotificationConfig.Flag.KAKAO)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemSkype, FcNotificationConfig.Flag.SKYPE)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemEmail, FcNotificationConfig.Flag.EMAIL)
//        updateThirdApp(deviceInfo, isNLSEnabled, viewBind.itemOthers, FcNotificationConfig.Flag.OTHERS_APP)
}

//    private fun updateThirdApp(deviceInfo: FcDeviceInfo, isNLSEnabled: Boolean, item: PreferenceItem, flag: Int) {
//        if (deviceInfo.isSupportNotification(flag)) {
//            item.visibility = View.VISIBLE
//            item.getSwitchCompat().isChecked = isNLSEnabled && config.isFlagEnabled(flag)
//        } else {
//            item.visibility = View.GONE
//        }
//    }

//}

class CaptureNotificationDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tip_prompt)
            .setMessage(R.string.ds_capture_notice_msg)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
//                NotificationListenerServiceUtil.toSettings(requireContext())
            }
            .create()
    }
}