package com.sjbt.sdk.sample

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmFind
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.settings.WmUnitInfo
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.example.myapplication.uniWatchInit
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.ui.camera.CameraActivity
import com.sjbt.sdk.sample.utils.FormatterUtil
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.getTestWeatherdata
import com.sjbt.sdk.sample.utils.promptToast
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

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
        applicationScope.launch {
            UNIWatchMate.wmApps.appWeather.observeWeather.asFlow().collect {
                if (it.wmWeatherTime == WmWeatherTime.SEVEN_DAYS) {
                    val result2 = UNIWatchMate?.wmApps?.appWeather?.pushSevenTodayWeather(
                        getTestWeatherdata(WmWeatherTime.SEVEN_DAYS),
                        WmUnitInfo.TemperatureUnit.CELSIUS
                    )?.await()
                    UNIWatchMate.wmLog.logE(TAG, "push seven_days weather result = $result2")
                    ToastUtil.showToast(
                        "push seven_days weather test ${
                            if (result2) getString(R.string.tip_success) else getString(
                                R.string.tip_failed
                            )
                        }"
                    )
                } else if (it.wmWeatherTime == WmWeatherTime.TODAY) {
                    val result = UNIWatchMate?.wmApps?.appWeather?.pushSevenTodayWeather(
                        getTestWeatherdata(WmWeatherTime.TODAY),
                        WmUnitInfo.TemperatureUnit.CELSIUS
                    )?.await()
                    UNIWatchMate.wmLog.logE(
                        TAG,
                        "push today weather result = $result"
                    )
                    ToastUtil.showToast(
                        "push today weather test ${
                            if (result) getString(R.string.tip_success) else getString(
                                R.string.tip_failed
                            )
                        }"
                    )
                }
            }
        }
        applicationScope.launch {
            UNIWatchMate.wmApps.appCamera.observeCameraOpenState.asFlow().collect {
                if (it) {
                    if (ActivityUtils.getTopActivity() != null) {//打开CameraActivity后需要传输什么数据吗
                        UNIWatchMate.wmLog.logE(
                            TAG,
                            "CameraActivity.launchActivity"
                        )
                        CameraActivity.launchActivity(ActivityUtils.getTopActivity(),)
                    }
                } else if (ActivityUtils.getTopActivity() is CameraActivity) {
                    ActivityUtils.getTopActivity().finish()
                }
            }
        }
        applicationScope.launch {
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

        applicationScope.launch {
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