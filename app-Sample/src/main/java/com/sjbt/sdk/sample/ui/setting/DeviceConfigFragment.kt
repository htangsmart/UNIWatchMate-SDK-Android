package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceConfigBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class DeviceConfigFragment : BaseFragment(R.layout.fragment_device_config) {

    private val viewBind: FragmentDeviceConfigBinding by viewBinding()

//    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemNotification.setOnClickListener( blockClick)
        viewBind.itemFunction.setOnClickListener( blockClick)
        viewBind.itemHealthMonitor.setOnClickListener( blockClick)
        viewBind.itemSedentary.setOnClickListener( blockClick)
        viewBind.itemDrinkWater.setOnClickListener( blockClick)
        viewBind.itemBloodPressure.setOnClickListener( blockClick)
        viewBind.itemTurnWristLighting.setOnClickListener( blockClick)
        viewBind.itemDnd.setOnClickListener( blockClick)
        viewBind.itemScreenVibrate.setOnClickListener( blockClick)

        viewLifecycle.launchRepeatOnStarted {
            launch {

            }
            launch {
//                deviceManager.configFeature.observerDeviceInfo().startWithItem(
//                    deviceManager.configFeature.getDeviceInfo()
//                ).asFlow().collect {
//                    viewBind.itemPage.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SETTING_PAGE_CONFIG)
//                    viewBind.itemBloodPressure.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE) and !it.isSupportFeature(FcDeviceInfo.Feature.BLOOD_PRESSURE_AIR_PUMP)
//                    viewBind.itemDnd.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.DND)
//                    viewBind.itemScreenVibrate.isVisible = it.isSupportFeature(FcDeviceInfo.Feature.SCREEN_VIBRATE)
//                }
            }
        }
    }

    private val blockClick: (View) -> Unit = { view ->
        when (view) {

            viewBind.itemNotification -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toNotificationConfig())
            }
            viewBind.itemFunction -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toFunctionConfig())
            }
//            viewBind.itemHealthMonitor -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toHealthMonitorConfig())
//            }
//            viewBind.itemSedentary -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toSedentaryConfig())
//            }
//            viewBind.itemDrinkWater -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toDrinkWaterConfig())
//            }
//            viewBind.itemBloodPressure -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toBpConfig())
//            }
            viewBind.itemTurnWristLighting -> {
                findNavController().navigate(DeviceConfigFragmentDirections.toTurnWristLightingConfig())
            }
//            viewBind.itemDnd -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toDndConfig())
//            }
//            viewBind.itemScreenVibrate -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toScreenVibrateConfig())
//            }
        }
    }

}