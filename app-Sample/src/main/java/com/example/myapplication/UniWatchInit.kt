package com.example.myapplication

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.base.api.UNIWatchMate
import com.fitcloud.sdk.FcUniWatch
import com.polidea.rxandroidble3.LogConstants
import com.polidea.rxandroidble3.LogOptions
import com.polidea.rxandroidble3.RxBleClient
import com.sjbt.sdk.BuildConfig
import com.sjbt.sdk.SJUniWatch
import com.topstep.fitcloud.sdk.v2.FcSDK
import timber.log.Timber

fun uniWatchInit(application: Application) {
    //1.设置log输出
    //"UNIWatchMate"使用Timber作为日志输出，所以需要配置Timber
    if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
    } else {
        Timber.plant(object : Timber.DebugTree() {
            override fun isLoggable(tag: String?, priority: Int): Boolean {
                return priority > Log.DEBUG
            }
        })
    }

    //2.配置支持不同厂商的手表
    UNIWatchMate.init(
        application, listOf(
            SJUniWatchImpl(application, 10000),
            FcUniWatchImpl(application)
        )
    )
}

class SJUniWatchImpl(override var mContext: Application, override var mMsgTimeOut: Int):
    SJUniWatch(mContext, mMsgTimeOut) {
}

class FcUniWatchImpl(application: Application) : FcUniWatch(application) {
    init {
        //1. FitCloud-SDK 需要知道当前APP的前后台状态
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            var startCount = 0
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
                if (startCount == 0) {
                    //At this time, the APP enters the foreground
                    isForeground = true
                }
                startCount++
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                startCount--
                if (startCount == 0) {
                    //At this time, the APP enters the background
                    isForeground = false
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })

        //2.FitCloud-SDK 内部使用 "RxAndroidBLE"，所以需要配置RxAndroidBLE的日志输出
        RxBleClient.updateLogOptions(
            LogOptions.Builder()
                .setShouldLogAttributeValues(BuildConfig.DEBUG)
                .setShouldLogScannedPeripherals(false)
                .setMacAddressLogSetting(if (BuildConfig.DEBUG) LogConstants.MAC_ADDRESS_FULL else LogConstants.NONE)
                .setUuidsLogSetting(if (BuildConfig.DEBUG) LogConstants.UUIDS_FULL else LogConstants.NONE)
                .setLogLevel(LogConstants.WARN)
                .setLogger { level, tag, msg ->
                    Timber.tag(tag).log(level, msg)
                }
                .build()
        )
    }

    /**
     * 延迟创建具体的SDK示例，提高性能
     */
    override fun create(application: Application): FcSDK {
        val sdk = FcSDK
            .Builder(application)
            // 因为FitCloud-SDK 内部使用 "RxAndroidBLE"，需要创建RxBleClient对象。
            // 如果项目里有其他SDK也使用了RxBleClient，那么需要自己维持一个单例，然后设置到这里。
//          .setRxBleClient(RxBleClient.create(application))
            .build()
        sdk.setReConnectFrequent(false)//在后台时减少回连的次数，节省电量
        return sdk
    }

}