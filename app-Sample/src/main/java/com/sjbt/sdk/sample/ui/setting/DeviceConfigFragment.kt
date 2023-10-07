package com.sjbt.sdk.sample.ui.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.entity.settings.WmWistRaise
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceConfigBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class DeviceConfigFragment : BaseFragment(R.layout.fragment_device_config) {

    private val viewBind: FragmentDeviceConfigBinding by viewBinding()

    //    private val deviceManager = Injector.getDeviceManager()
    private var wistRaiseConfig: WmWistRaise? = null
    private var dateTimeConfig: WmDateTime? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemNotification.setOnClickListener(blockClick)
        viewBind.itemFunction.setOnClickListener(blockClick)
        viewBind.itemHealthMonitor.setOnClickListener(blockClick)
        viewBind.itemSedentary.setOnClickListener(blockClick)
        viewBind.itemDrinkWater.setOnClickListener(blockClick)
        viewBind.itemBloodPressure.setOnClickListener(blockClick)
//        viewBind.itemTurnWristLighting.setOnClickListener( blockClick)
        viewBind.itemDnd.setOnClickListener(blockClick)
        viewBind.itemScreenVibrate.setOnClickListener(blockClick)
//        viewBind.itemWristLightingEnabled.getSwitchView()
//            .setOnCheckedChangeListener { buttonView, isChecked ->
//
//            }
//        viewBind.itemSynchronizeDateAndTimeEnabled.getSwitchView()
//            .setOnCheckedChangeListener { buttonView, isChecked ->
//
//            }
        viewLifecycle.launchRepeatOnStarted {
            viewLifecycle.launchRepeatOnStarted {
                launch {
                    UNIWatchMate.wmConnect.observeConnectState.asFlow().collect {
                        viewBind.layoutContent.setAllChildEnabled(it.equals(WmConnectState.VERIFIED))
                    }
                }
                launch {
//                    UNIWatchMate.wmSettings.settingWistRaise.observeChange().asFlow().collect {
//                        wistRaiseconfig = it
//                        viewBind.itemWristLightingEnabled.getSwitchView().isChecked=it.isScreenWakeEnabled
//                    }
                }

                launch {
//                    wistRaiseconfig = UNIWatchMate.wmSettings.settingWistRaise.get().blockingGet()
//                    viewBind.itemWristLightingEnabled.getSwitchView().isChecked=wistRaiseconfig?.isScreenWakeEnabled?:false
                }


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
//            viewBind.itemTurnWristLighting -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toTurnWristLightingConfig())
//            }
//            viewBind.itemDnd -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toDndConfig())
//            }
//            viewBind.itemScreenVibrate -> {
//                findNavController().navigate(DeviceConfigFragmentDirections.toScreenVibrateConfig())
//            }
        }
    }

}