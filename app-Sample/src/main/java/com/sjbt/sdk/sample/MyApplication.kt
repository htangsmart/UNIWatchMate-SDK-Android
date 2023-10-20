package com.sjbt.sdk.sample

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmMusicControlType
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.ui.camera.CameraActivity
import com.sjbt.sdk.sample.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow


class MyApplication : Application() {
    val TAG: String = "MyApplication"
    private lateinit var applicationScope: CoroutineScope

    companion object {
        lateinit var instance: MyApplication
            private set
        val mHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        applicationScope = Injector.getApplicationScope()
        //第一步：初始化，需要传入支持的sdk实例
        uniWatchInit(this)

        //第二步：通过setDeviceModel选定SDK(发现设备场景)，如果是扫码场景则用scanQr，二选一
        UNIWatchMate.setDeviceModel(WmDeviceModel.SJ_WATCH)
        //UNIWatchMate.scanQr("www.shenju.watch?mac=00:00:56:78:9A:BC?name=SJ 8020N")
        //全局监听
        observeState()
        Utils.init(instance)
        //监听sdk变化
        UNIWatchMate.observeUniWatchChange().subscribe {
            it.setLogEnable(true)
        }

        UNIWatchMate.wmLog.logI(TAG, "APP onCreate")

        initAllProcess()
    }

    private fun initAllProcess() {
        FormatterUtil.init(Resources.getSystem().configuration.locale)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        FormatterUtil.init(Resources.getSystem().configuration.locale)
    }

    private fun observeState() {

        UNIWatchMate.observeConnectState.subscribe {

            UNIWatchMate.wmLog.logE(TAG, it.name)

            when (it) {
                WmConnectState.BT_DISABLE -> {

                }

                WmConnectState.VERIFIED -> {
                    UNIWatchMate.wmApps.appCamera.observeCameraOpenState.subscribe {
                        UNIWatchMate.wmLog.logE(TAG, "设备相机状态1：$it")
                    }
                }

                WmConnectState.CONNECTED -> {

                }
            }
        }

        applicationScope.launch {
            launchWithLog {
//                UNIWatchMate.wmApps.appWeather.observeWeather.asFlow().collect {
//                    if (it.wmWeatherTime == WmWeatherTime.SEVEN_DAYS) {
//                        val result2 = UNIWatchMate?.wmApps?.appWeather?.pushSevenTodayWeather(
//                            getTestWeatherdata(WmWeatherTime.SEVEN_DAYS),
//                            WmUnitInfo.TemperatureUnit.CELSIUS
//                        )?.await()
//                        UNIWatchMate.wmLog.logE(TAG, "push seven_days weather result = $result2")
//                        ToastUtil.showToast(
//                            "push seven_days weather test ${
//                                if (result2) getString(R.string.tip_success) else getString(
//                                    R.string.tip_failed
//                                )
//                            }"
//                        )
//                    } else if (it.wmWeatherTime == WmWeatherTime.TODAY) {
//                        val result = UNIWatchMate?.wmApps?.appWeather?.pushTodayWeather(
//                            getTestWeatherdata(WmWeatherTime.TODAY),
//                            WmUnitInfo.TemperatureUnit.CELSIUS
//                        )?.await()
//                        UNIWatchMate.wmLog.logE(
//                            TAG,
//                            "push today weather result = $result"
//                        )
//                        ToastUtil.showToast(
//                            "push today weather test ${
//                                if (result) getString(R.string.tip_success) else getString(
//                                    R.string.tip_failed
//                                )
//                            }"
//                        )
//                    }
//                }
            }
            launchWithLog {
                UNIWatchMate.wmApps.appCamera.observeCameraOpenState.asFlow().collect {
                    if (it) {//
                        if (ActivityUtils.getTopActivity() != null) {
                            UNIWatchMate.wmLog.logE(TAG, "设备相机状态1：$it")
                            CacheDataHelper.cameraLaunchedByDevice = true
                            CameraActivity.launchActivity(ActivityUtils.getTopActivity())
                        }
                    } else if (ActivityUtils.getTopActivity() is CameraActivity) {
                        ActivityUtils.getTopActivity().finish()
                    }
                }
            }
            launchWithLog {
                UNIWatchMate.wmApps.appFind.observeFindMobile.asFlow().catch {
                    it.message?.let { it1 ->
                        UNIWatchMate.wmLog.logE(
                            TAG,
                            it1
                        )
                    }
                    ToastUtil.showToast(it.toString(), false)

                }.collect {
                    ToastUtil.showToast(it.toString(), true)
                }
            }
            launchWithLog {
                UNIWatchMate.wmApps.appMusicControl.observableMusicControl.asFlow().collect {
                    simulateMediaButton(it)
                    UNIWatchMate.wmLog.logE(
                        TAG,
                        "receive music control type= $it"
                    )
                }
            }
            launchWithLog {
                UNIWatchMate.wmApps.appFind.stopFindMobile().asFlow().onCompletion {
                    UNIWatchMate.wmLog.logE(
                        TAG,
                        "onCompletion"
                    )
                }.catch {
                    it.message?.let { it1 ->
                        UNIWatchMate.wmLog.logE(
                            TAG,
                            it1
                        )
                    }
                    ToastUtil.showToast(it.toString(), false)
                }.collect {
                    ToastUtil.showToast(it.toString(), true)
                }
            }
        }
    }

    private fun simulateMediaButton(musicType: WmMusicControlType) {
        var keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        when (musicType) {
            WmMusicControlType.PREV_SONG -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS
                ToastUtil.showToast("PREV_SONG")
            }

            WmMusicControlType.NEXT_SONG -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_NEXT
                ToastUtil.showToast("NEXT_SONG")
            }

            WmMusicControlType.PLAY -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_PLAY
                ToastUtil.showToast("PLAY")
            }

            WmMusicControlType.PAUSE -> {
                keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE
                ToastUtil.showToast("PAUSE")
            }

            WmMusicControlType.VOLUME_UP -> {
                keyCode = KeyEvent.KEYCODE_VOLUME_UP
                ToastUtil.showToast("VOLUME_UP")
            }

            WmMusicControlType.VOLUME_DOWN -> {
                keyCode = KeyEvent.KEYCODE_VOLUME_DOWN
                ToastUtil.showToast("VOLUME_DOWN")
            }
        }
        sendKeyCode(keyCode)
    }

}