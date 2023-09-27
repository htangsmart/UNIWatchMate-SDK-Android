package com.example.myapplication.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.base.api.UNIWatchMate
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.`interface`.AbWmConnect
import com.base.sdk.`interface`.log.WmLog
import com.permissionx.guolindev.PermissionX
import com.sjbt.sdk.sample.R
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_actiivty)

        val btnConnect = findViewById<Button>(R.id.btn_connect)
        val btnExchange = findViewById<Button>(R.id.btn_exchange)
        val btnUnBind = findViewById<Button>(R.id.btn_bind)
        checkPermission()

        btnConnect.setText("发现设备")
        btnConnect.setOnClickListener {
            //扫码切换sdk
//            UNIWatchMate.scanQr("www.shenju.watch?mac=00:00:56:78:9A:BC?name=SJ 8020N")
            checkPermission()
            startDiscoveryDevice()
        }

        btnExchange.setText("连接：15:7E:78:A2:4B:30")

        btnExchange.setOnClickListener {
            connectSample()
        }

        btnUnBind.setOnClickListener {
            unbind()
        }
    }

    private fun checkPermission() {
        val pList = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            pList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            pList.add(Manifest.permission.BLUETOOTH)
            pList.add(Manifest.permission.BLUETOOTH_CONNECT)
            pList.add(Manifest.permission.BLUETOOTH_ADMIN)
            pList.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            pList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            pList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            pList.add(Manifest.permission.BLUETOOTH)
            pList.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        PermissionX.init(this)
            .permissions(
                pList
            )
            .request { allGranted, grantedList, deniedList ->

                if (allGranted) {
                    WmLog.d(TAG, "allGranted:$allGranted")
                } else {
                    WmLog.d(TAG, "deniedList:$deniedList")
                }
            }
    }

    private fun unbind(){
        UNIWatchMate.wmConnect?.reset()
    }

    /**
     * 连接示例
     */
    private fun connectSample() {
        UNIWatchMate.wmConnect?.connect("15:7E:78:A2:4B:30",
            AbWmConnect.BindInfo(AbWmConnect.BindType.DISCOVERY,AbWmConnect.UserInfo("123456","张三"),""), WmDeviceModel.SJ_WATCH)
    }

    private fun startDiscoveryDevice() {

        UNIWatchMate.mInstance?.let {
            val observable = it.startDiscovery()
            val observer: Observer<BluetoothDevice> = object : Observer<BluetoothDevice> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    WmLog.e(TAG, "onError:$e")
                }

                override fun onNext(t: BluetoothDevice) {
                    WmLog.d(TAG, "onNext:$t")
                }

                override fun onComplete() {
                    WmLog.d(TAG, "onComplete")
                }
            }

            observable.subscribe(observer)
        }
    }

    /**
     * 设置配置信息示例
     */
    fun settingsSample() {
        //设置运动目标 示例：其他与此类似，都是通过模块实例调用对应的接口方法
        val sportGoal = WmSportGoal(10000, 200, 10000, 1000)
        val settingSingle = UNIWatchMate.wmSettings.settingSportGoal?.set(sportGoal)
        settingSingle?.subscribe(object : SingleObserver<WmSportGoal> {
            override fun onSubscribe(d: Disposable) {}
            override fun onSuccess(basicInfo: WmSportGoal) {

            }

            override fun onError(e: Throwable) {

            }
        })
    }
}

