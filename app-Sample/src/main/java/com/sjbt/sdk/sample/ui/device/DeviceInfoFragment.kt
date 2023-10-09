package com.sjbt.sdk.sample.ui.device

import android.os.Bundle
import android.view.View
import com.base.api.UNIWatchMate
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceBinding
import com.sjbt.sdk.sample.databinding.FragmentDeviceInfoBinding
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow

class DeviceInfoFragment : BaseFragment(R.layout.fragment_device_info) {

    private val viewBind: FragmentDeviceInfoBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycle.launchRepeatOnStarted {
            launch {
                UNIWatchMate?.wmSync?.syncDeviceInfoData?.observeSyncData?.asFlow()?.collect{
                    viewBind.itemDeviceInfo.text = "syncDeviceInfoData\nmac=${it.macAddress}\nbluetoothName=${it.bluetoothName}\n" +
                            "deviceName=${it.deviceName}\n" +
                            "deviceId=${it.deviceId}\n" +
                            "version=${it.version}\n"+
                            "model=${it.model}"
                }
            }

            launch {
                UNIWatchMate?.wmSync?.syncDeviceInfoData?.syncData(System.currentTimeMillis())?.toObservable()?.asFlow()?.collect{
                    viewBind.itemDeviceInfo.text = "syncData\nmac=${it.macAddress}\nbluetoothName=${it.bluetoothName}\n" +
                            "deviceName=${it.deviceName}\n" +
                            "deviceId=${it.deviceId}\n" +
                            "version=${it.version}\n"+
                            "model=${it.model}"
                }
            }
        }
    }

}