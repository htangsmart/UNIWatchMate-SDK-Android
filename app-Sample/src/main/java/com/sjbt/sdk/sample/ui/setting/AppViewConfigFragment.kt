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
const val APP_VIEW_GRID_ID = 1
const val APP_VIEW_LIST_ID = 3
class AppViewConfigFragment : BaseFragment(R.layout.fragment_app_view) {
    private val viewBind: FragmentAppViewBinding by viewBinding()

    private val deviceManager = Injector.getDeviceManager()
    private val applicationScope = Injector.getApplicationScope()

    private var config: WmAppView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideAllView()
        viewBind.itemAppViewGridding.getImageView().setImageResource(R.drawable.ic_baseline_done_24)
        viewBind.itemAppViewList.getImageView().setImageResource(R.drawable.ic_baseline_done_24)
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
                UNIWatchMate.wmSettings.settingAppView.get().toObservable().asFlow().collect {
                    config = it
                    updateUI()
                }
            }
        }

        viewBind.itemAppViewGridding.setOnClickListener {
            config?.let {
                for (bean in it.appViewList) {
                    if (bean.id == APP_VIEW_GRID_ID) {
                        bean.status=1
                    }else{
                        bean.status=0
                    }
                }
                it.saveConfig()
            }
        }

        viewBind.itemAppViewList.setOnClickListener {
            config?.let {
                for (bean in it.appViewList) {
                    if (bean.id == APP_VIEW_LIST_ID) {
                        bean.status=1
                    }else{
                        bean.status=0
                    }
                }
                it.saveConfig()
            }
        }

    }

    private fun WmAppView.saveConfig() {
        applicationScope.launchWithLog {
            UNIWatchMate.wmSettings.settingAppView.set(this@saveConfig).await()
        }
        updateUI()
    }

    private fun updateUI() {
//        1是瀑布刘3是列表
        hideAllView()
        config?.let {
            for (bean in it.appViewList) {
                if (bean.id == APP_VIEW_GRID_ID) {
                    viewBind.itemAppViewGridding.getImageView().visibility=if(bean.status==1) View.VISIBLE else View.INVISIBLE
                }else if(bean.id == APP_VIEW_LIST_ID){
                    viewBind.itemAppViewList.getImageView().visibility=if(bean.status==1) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    private fun hideAllView() {
        viewBind.itemAppViewGridding.getImageView().visibility=View.INVISIBLE
        viewBind.itemAppViewList.getImageView().visibility=View.INVISIBLE
    }

}