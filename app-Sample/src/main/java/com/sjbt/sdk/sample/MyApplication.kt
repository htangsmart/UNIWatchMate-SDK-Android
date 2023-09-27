package com.sjbt.sdk.sample

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import com.base.api.UNIWatchMate
import com.base.sdk.`interface`.log.WmLog
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.example.myapplication.uniWatchInit
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.FormatterUtil
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope

class MyApplication : Application() {
    val TAG: String = "MyApplication"
    private lateinit var applicationScope: CoroutineScope


    companion object {
        lateinit var instance: MyApplication
            private set
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

        //监听sdk变化
        UNIWatchMate.observeUniWatchChange().subscribe {
            it.setLogEnable(true)
            WmLog.e(TAG, "SDK changed")
        }
        initAllProcess()
    }
    private fun initAllProcess() {
        FormatterUtil.init(Resources.getSystem().configuration.locale)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        FormatterUtil.init(Resources.getSystem().configuration.locale)

    }
    /**
     * 全局监听连接状态
     */
    private fun observeState() {
        //监听连接状态
        UNIWatchMate.wmConnect.observeConnectState.subscribe(object : Observer<WmConnectState> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(connectState: WmConnectState) {

                WmLog.e(TAG, "connect state: $connectState")

                when (connectState) {
                    WmConnectState.BT_DISABLE -> {

                    }

                    WmConnectState.BT_ENABLE -> {

                    }

                    WmConnectState.DISCONNECTED -> {

                    }

                    WmConnectState.CONNECTING -> {

                    }

                    WmConnectState.CONNECTED -> {

                    }

                    WmConnectState.VERIFIED -> {

                    }
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }
}