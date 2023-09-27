package com.sjbt.sdk.sample.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.sjbt.sdk.sample.base.BaseActivity

/**
 * Splash screen.
 * According to whether there are currently has authed user, choose to enter the [MainActivity] or the [AuthActivity]
 */
class LaunchActivity : BaseActivity() {

    private val viewModel by viewModels<LaunchViewMode>()

    private var launchNavigation: LaunchNavigation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//      startService(Intent(this, DeviceService::class.java))
        installSplashScreen()
        lifecycleScope.launchWhenStarted {
            launchNavigation = viewModel.getLaunchNavigation()
            when (launchNavigation) {
                LaunchNavigation.NavToMain -> {
                    MainActivity.start(this@LaunchActivity)
                }
                else -> {
                    throw IllegalStateException()
                }
            }
            finish()
        }
    }

}

sealed class LaunchNavigation {
    object NavToMain : LaunchNavigation()
}

class LaunchViewMode : ViewModel() {
    suspend fun getLaunchNavigation(): LaunchNavigation {
        return LaunchNavigation.NavToMain
    }
}