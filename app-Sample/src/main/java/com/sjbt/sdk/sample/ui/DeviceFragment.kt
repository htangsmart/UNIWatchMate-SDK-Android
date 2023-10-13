package com.sjbt.sdk.sample.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.TodayWeather
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmLocation
import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.entity.apps.WmNotificationType
import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherForecast
import com.base.sdk.entity.common.WmWeek
import com.base.sdk.entity.settings.WmUnitInfo
import com.blankj.utilcode.util.TimeUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.ui.bind.DeviceConnectDialogFragment
import com.sjbt.sdk.sample.ui.camera.CameraActivity
import com.sjbt.sdk.sample.ui.fileTrans.FileTransferActivity
import com.sjbt.sdk.sample.utils.*
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber

@StringRes
fun WmConnectState.toStringRes(): Int {
    return when (this) {
        WmConnectState.BT_DISABLE -> R.string.device_state_bt_disabled
        WmConnectState.DISCONNECTED -> R.string.device_state_disconnected
        WmConnectState.CONNECTING -> R.string.device_state_connecting
        WmConnectState.CONNECTED -> R.string.device_state_connected
        WmConnectState.VERIFIED -> R.string.device_state_verified
        else -> {
            R.string.device_state_other
        }
    }
}

class DeviceFragment : BaseFragment(R.layout.fragment_device) {

    private val viewBind: FragmentDeviceBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemDeviceBind.setOnClickListener(blockClick)
        viewBind.itemDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemDeviceConfig.setOnClickListener(blockClick)
        viewBind.itemQrCodes.setOnClickListener(blockClick)
        viewBind.itemAlarm.setOnClickListener(blockClick)
        viewBind.itemContacts.setOnClickListener(blockClick)
        viewBind.itemTestSendNotification.setOnClickListener(blockClick)
        viewBind.itemSportPush.setOnClickListener(blockClick)
        viewBind.itemDial.setOnClickListener(blockClick)
        viewBind.itemBasicDeviceInfo.setOnClickListener(blockClick)
        viewBind.itemCamera.setOnClickListener(blockClick)
        viewBind.itemTransferFile.setOnClickListener(blockClick)
        viewBind.itemTestWeather.setOnClickListener(blockClick)
//        viewBind.itemModifyLogo.clickTrigger(block = blockClick)
//        viewBind.itemEpoUpgrade.clickTrigger(block = blockClick)
//        viewBind.itemCricket.clickTrigger(block = blockClick)
        viewBind.itemOtherFeatures.setOnClickListener(blockClick)
//        viewBind.itemVersionInfo.clickTrigger(block = blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {

                deviceManager.flowDevice?.collect {
                    this::class.simpleName?.let { it1 -> Timber.tag(it1).i("flowDevice=$it") }
                    if (it == null) {
                        viewBind.itemDeviceBind.visibility = View.VISIBLE
                        viewBind.itemDeviceInfo.visibility = View.GONE
                    } else {
                        viewBind.itemDeviceBind.visibility = View.GONE
                        viewBind.itemDeviceInfo.visibility = View.VISIBLE
                        viewBind.tvDeviceName.text = it.name
                    }
                }
            }

            launch {
                deviceManager.flowConnectorState.collect {
                    this::class.simpleName?.let { it1 ->
                        Timber.tag(it1).i("flowConnectorState=$it")
                    }
                    viewBind.tvDeviceState.setText(it.toStringRes())
//                    viewBind.layoutContent.setAllChildEnabled(it == WmConnectState.VERIFIED)
                }
            }

            launch {
                deviceManager.flowBattery.collect {
                    this::class.simpleName?.let { it1 -> Timber.tag(it1).i("flowBattery=$it") }
                    if (it == null) {
                        viewBind.batteryView.setBatteryUnknown()
                    } else {
                        viewBind.batteryView.setBatteryStatus(it.isCharge, it.currValue)
                    }
                }
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

            viewBind.itemDeviceInfo -> {
                DeviceConnectDialogFragment().show(childFragmentManager, null)
            }

            viewBind.itemDeviceConfig -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceConfig())
            }

            viewBind.itemBasicDeviceInfo -> {
                findNavController().navigate(DeviceFragmentDirections.toDeviceInfo())
            }

            viewBind.itemDeviceConfig -> {
            }

            viewBind.itemOtherFeatures -> {
                findNavController().navigate(DeviceFragmentDirections.toOtherFeatures())
            }

            viewBind.itemAlarm -> {
                findNavController().navigate(DeviceFragmentDirections.toAlarm())
            }

            viewBind.itemTestWeather -> {
                applicationScope.launchWithLog {
                    val weatherForecastList = mutableListOf<WmWeatherForecast>()
                    val todayWeatherList = mutableListOf<TodayWeather>()
                    val wmWeatherForecast = WmWeatherForecast(
                        10,
                        30,
                        20,
                        WmUnitInfo.TemperatureUnit.CELSIUS,
                        90,
                        80,
                        1,
                        2,
                        "白天天气描述",
                        "夜晚天气描述",
                        System.currentTimeMillis(),
                        WmWeek.THURSDAY
                    )
                    val toDayWeather = TodayWeather(
                        10, WmUnitInfo.TemperatureUnit.CELSIUS, 90, 80,
                        1,"weatherDesc", System.currentTimeMillis(), 2
                    )
                    val wmLocation = WmLocation("cn", "xi'an", "district", 10.12345, 10.12345)
                    weatherForecastList.add(wmWeatherForecast)
                    todayWeatherList.add(toDayWeather)

                    val wmWeather = WmWeather(
                        System.currentTimeMillis(),
                        wmLocation,
                        weatherForecastList,
                        todayWeatherList
                    )
                    val result =  UNIWatchMate?.wmApps?.appWeather?.pushTodayWeather(wmWeather,WmUnitInfo.TemperatureUnit.CELSIUS)?.await()
                    UNIWatchMate.wmLog.logE(TAG,"set itemTestWeather $result")
                }
            }

            viewBind.itemTransferFile -> {
                activity?.let {
                    PermissionHelper.requestAppStoreage(this@DeviceFragment) { permission ->
                        if (permission) {
                            FileTransferActivity.launchActivity(it)
                        }
                    }
                }
            }

            viewBind.itemContacts -> {
                findNavController().navigate(DeviceFragmentDirections.toContacts())
            }

            viewBind.itemTestSendNotification -> {
                applicationScope.launchWithLog {
                    UNIWatchMate?.wmApps?.appNotification?.sendNotification(
                        WmNotification(
                            WmNotificationType.WECHAT,
                            "title_notification${TimeUtils.millis2String(System.currentTimeMillis())}",
                            "content_notification${TimeUtils.millis2String(System.currentTimeMillis())}",
                            "sub_content_notification"
                        )
                    )?.toObservable()?.asFlow()?.collect {
                        Timber.tag("appNotification").i("appNotification result=$it")
                    }
                }
            }
//            viewBind.itemGamePush -> {
//                findNavController().navigate(DeviceFragmentDirections.toGamePush())
//            }
//            viewBind.itemSportPush -> {
//                findNavController().navigate(DeviceFragmentDirections.toSportPush())
//            }

            viewBind.itemCamera -> {
                PermissionHelper.requestAppCameraAndStoreage(this@DeviceFragment) {
                    if (it) {
                        CacheDataHelper.cameraLaunchedBySelf = true
                        CameraActivity.launchActivity(activity)
                    }
                }
            }

            viewBind.itemDial -> {
                findNavController().navigate(DeviceFragmentDirections.toDialHomePage())
            }

            viewBind.itemCamera -> {
                PermissionHelper.requestAppCameraAndStoreage(this@DeviceFragment) {
                    if (it) {
                        CameraActivity.launchActivity(activity)
                    }
                }
            }
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

