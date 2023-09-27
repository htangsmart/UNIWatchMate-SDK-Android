package com.sjbt.sdk.sample.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

//@StringRes
//fun ConnectorState.toStringRes(): Int {
//    return when (this) {
//        ConnectorState.NO_DEVICE -> R.string.device_state_no_device
//        ConnectorState.BT_DISABLED -> R.string.device_state_bt_disabled
//        ConnectorState.DISCONNECTED, ConnectorState.PRE_CONNECTING -> R.string.device_state_disconnected
//        ConnectorState.CONNECTING -> R.string.device_state_connecting
//        ConnectorState.CONNECTED -> R.string.device_state_connected
//    }
//}

class DeviceFragment : BaseFragment(R.layout.fragment_device) {

    private val viewBind: FragmentDeviceBinding by viewBinding()
//    private val viewModel: DeviceViewMode by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemDeviceBind.setOnClickListener(blockClick)
        viewBind.itemDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemDeviceConfig.setOnClickListener(blockClick)
        viewBind.itemQrCodes.setOnClickListener(blockClick)
        viewBind.itemAlarm.setOnClickListener(blockClick)
        viewBind.itemContacts.setOnClickListener(blockClick)
        viewBind.itemPowerSaveMode.setOnClickListener(blockClick)
        viewBind.itemSportPush.setOnClickListener(blockClick)
        viewBind.itemDial.setOnClickListener(blockClick)
//        viewBind.itemCamera.clickTrigger(block = blockClick)
//        viewBind.itemModifyLogo.clickTrigger(block = blockClick)
//        viewBind.itemEpoUpgrade.clickTrigger(block = blockClick)
//        viewBind.itemCricket.clickTrigger(block = blockClick)
//        viewBind.itemOtherFeatures.clickTrigger(block = blockClick)
//        viewBind.itemVersionInfo.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {
//                deviceManager.flowDevice.collect {
//                    if (it == null) {
//                        viewBind.itemDeviceBind.isVisible = true
//                        viewBind.itemDeviceInfo.isVisible = false
//                    } else {
//                        viewBind.itemDeviceBind.isVisible = false
//                        viewBind.itemDeviceInfo.isVisible = true
//                        viewBind.tvDeviceName.text = it.name
//                    }
//                }
            }
            launch {
//                deviceManager.flowConnectorState.collect {
//                    viewBind.tvDeviceState.setText(it.toStringRes())
//                    viewBind.layoutContent.setAllChildEnabled(it == ConnectorState.CONNECTED)
//                }
            }
            launch {
//                deviceManager.flowBattery.collect {
//                    if (it == null) {
//                        viewBind.batteryView.setBatteryUnknown()
//                    } else {
//                        val percentage: Int = it.percentage / 10 * 10
//                        viewBind.batteryView.setBatteryStatus(it.isCharging, percentage)
//                    }
//                }
            }
            launch {
//                deviceManager.configFeature.observerDeviceInfo().startWithItem(
//                    deviceManager.configFeature.getDeviceInfo()
//                ).asFlow().collect {
//                    viewBind.itemQrCodes.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.COLLECTION_CODE) ||
//                            it.isSupportFeature(FcDeviceInfo.Feature.BUSINESS_CARD) ||
//                            it.isSupportFeature(FcDeviceInfo.Feature.NUCLEIC_ACID_CODE) ||
//                            it.isSupportFeature(FcDeviceInfo.Feature.QR_CODE_EXTENSION_1)
//                    viewBind.itemContacts.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.CONTACTS)
//                    viewBind.itemPowerSaveMode.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.POWER_SAVE_MODE)
//                    viewBind.itemGamePush.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.GAME_PUSH)
//                    viewBind.itemSportPush.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SPORT_PUSH)
//                    viewBind.itemCricket.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.CRICKET_MATCH)
//                    viewBind.itemVersionInfo.getTextView().text = it.hardwareInfoDisplay()
//                }
            }
            launch {
//                viewModel.flowState.collect {
//                    if (it.asyncCheckUpgrade is Loading) {
//                        promptProgress.showProgress(R.string.tip_please_wait)
//                    } else {
//                        promptProgress.dismiss()
//                    }
//                }
            }
            launch {
//                viewModel.flowEvent.collect {
//                    when (it) {
//                        is AsyncEvent.OnFail -> promptToast.showFailed(it.error)
//                        is AsyncEvent.OnSuccess<*> -> {
//                            if (it.property == State::asyncCheckUpgrade) {
//                                val info = it.value as HardwareUpgradeInfo?
//                                if (info == null) {
//                                    promptToast.showInfo(R.string.version_is_latest_version)
//                                } else {
//                                    findNavController().navigate(DeviceFragmentDirections.toHardwareUpgrade(info))
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {
            viewBind.itemDeviceBind -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceBind())
            }

//            viewBind.itemDeviceInfo -> {
//                DeviceConnectDialogFragment().show(childFragmentManager, null)
//            }
            viewBind.itemDeviceConfig -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceConfig())
            }
//            viewBind.itemQrCodes -> {
//                findNavController().navigate(DeviceFragmentDirections.toQrCodes())
//            }
            viewBind.itemAlarm -> {
                findNavController().navigate(DeviceFragmentDirections.toAlarm())
            }
//            viewBind.itemContacts -> {
//                findNavController().navigate(DeviceFragmentDirections.toContacts())
//            }
//            viewBind.itemPowerSaveMode -> {
//                findNavController().navigate(DeviceFragmentDirections.toPowerSaveMode())
//            }
//            viewBind.itemGamePush -> {
//                findNavController().navigate(DeviceFragmentDirections.toGamePush())
//            }
//            viewBind.itemSportPush -> {
//                findNavController().navigate(DeviceFragmentDirections.toSportPush())
//            }
//            viewBind.itemDial -> {
//                findNavController().navigate(DeviceFragmentDirections.toDialHomePage())
//            }
//            viewBind.itemCamera -> {
//                CameraActivity.start(requireContext(), false)
//            }
//            viewBind.itemModifyLogo -> {
//                findNavController().navigate(DeviceFragmentDirections.toModifyLogo())
//            }
//            viewBind.itemEpoUpgrade -> {
//                findNavController().navigate(DeviceFragmentDirections.toEpoUpgrade())
//            }
//            viewBind.itemCricket -> {
//                findNavController().navigate(DeviceFragmentDirections.toCricket())
//            }
//            viewBind.itemOtherFeatures -> {
//                findNavController().navigate(DeviceFragmentDirections.toOtherFeatures())
//            }
//            viewBind.itemVersionInfo -> {
//                viewModel.checkUpgrade()
////If you jump directly , you can select a local file for OTA. This may be more convenient for testing
////                findNavController().navigate(DeviceFragmentDirections.toHardwareUpgrade(null))
//            }
        }
    }

//    override fun navToConnectHelp() {
//        findNavController().navigate(DeviceFragmentDirections.toConnectHelp())
//    }
//
//    override fun navToBgRunSettings() {
//        findNavController().navigate(DeviceFragmentDirections.toBgRunSettings())
//    }

//    data class State(
//        val asyncCheckUpgrade: Async<HardwareUpgradeInfo?> = Uninitialized
//    )

}

//class DeviceViewMode : AsyncViewModel<DeviceFragment.State>(DeviceFragment.State()) {
//
////    private val versionRepository = Injector.getVersionRepository()
////
////    fun checkUpgrade() {
////        suspend {
////            versionRepository.checkUpgrade()
////        }.execute(DeviceFragment.State::asyncCheckUpgrade) {
////            copy(asyncCheckUpgrade = it)
////        }
////    }
//
//}