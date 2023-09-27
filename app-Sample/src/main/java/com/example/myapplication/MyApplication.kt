package com.example.myapplication

import android.app.Application
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.`interface`.log.WmLog
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class MyApplication : Application() {

    companion object {
        @JvmStatic
        lateinit var instance: MyApplication
         const val TAG: String = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

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

    override fun onTerminate() {
        super.onTerminate()

    }

}