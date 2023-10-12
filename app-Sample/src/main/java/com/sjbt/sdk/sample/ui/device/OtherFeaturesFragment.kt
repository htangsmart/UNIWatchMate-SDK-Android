package com.sjbt.sdk.sample.ui.device

import android.os.Bundle
import android.view.View
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmFind
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentOtherFeaturesBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import kotlinx.coroutines.launch

class OtherFeaturesFragment : BaseFragment(R.layout.fragment_other_features) {

    private val viewBind: FragmentOtherFeaturesBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.itemFindDevice.clickTrigger {
            viewLifecycleScope.launch {
                UNIWatchMate.wmApps.appFind.findWatch(WmFind(5, 5))
            }
        }

        viewBind.itemStopFindDevice.clickTrigger {
            viewLifecycleScope.launch {
                UNIWatchMate.wmApps.appFind.stopFindMobile()
            }
        }

        viewBind.itemDeviceReset.clickTrigger {
            viewLifecycleScope.launch {
                deviceManager.reset()
            }
        }
    }

}