package com.sjbt.sdk.spp

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import com.sjbt.sdk.TAG_SJ
import com.sjbt.sdk.log.SJLog
import java.io.IOException

/**
 * 监听蓝牙广播-各种状态
 */
@SuppressLint("MissingPermission")
class BtStateReceiver(cxt: Context, private val mOnBtStateListener: OnBtStateListener?) :
    BroadcastReceiver() {

    private var mCurrAddress: String? = null
    private var mSocket: BluetoothSocket? = null
    private val TAG = TAG_SJ + "BtStateReceiver"

    init {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //蓝牙开关状态
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //蓝牙开始搜索
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //蓝牙搜索结束
        filter.addAction(BluetoothDevice.ACTION_FOUND) //蓝牙发现新设备(未配对的设备)
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST) //在系统弹出配对框之前(确认/输入配对码)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) //设备配对状态改变
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //最底层连接建立
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //最底层连接断开
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) //BluetoothAdapter连接状态
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) //BluetoothHeadset连接状态
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) //BluetoothA2dp连接状态
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        cxt.registerReceiver(this, filter)
    }

    fun setmCurrDevice(address: String?) {
        mCurrAddress = address
    }

    fun setmSocket(mSocket: BluetoothSocket?) {
        this.mSocket = mSocket
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                if (status == BluetoothAdapter.STATE_ON) {
                    mOnBtStateListener?.onClassicBtOpen()
                } else if (status == BluetoothAdapter.STATE_OFF) {
                    SJLog.logBt(TAG, "系统蓝牙已关闭:$status")
                    mOnBtStateListener?.onClassicBtDisabled()
                }
            }

            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                SJLog.logBt(TAG, "bt start discovery")
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                SJLog.logBt(TAG, "bt end discovery")
            }

            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED, BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED, BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                SJLog.logBt(TAG, "ACTION_CONNECTION_STATE_CHANGED")
            }

            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (!TextUtils.isEmpty(device!!.name)) {
                    mOnBtStateListener?.onDiscoveryDevice(device)
                }
            }

            BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                val pairing_device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                SJLog.logBt(TAG, "配请求对中...：" + pairing_device!!.name)
            }

            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val bond_device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                when (bond_device!!.bondState) {
                    BluetoothDevice.BOND_NONE -> {
                        SJLog.logBt(TAG, "配对取消：" + bond_device.name)
                    }

                    BluetoothDevice.BOND_BONDING -> {
                        SJLog.logBt(TAG, "配对中：" + bond_device.name)
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        SJLog.logBt(TAG, "配对成功：" + bond_device.name)
                    }
                }
                mOnBtStateListener?.onBindState(bond_device, bond_device.bondState)
            }

            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                SJLog.logBt(TAG, "ACTION_ACL_CONNECTED")
                val connect_device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (connect_device!!.type == BluetoothDevice.DEVICE_TYPE_LE) {
                    return
                }
                mOnBtStateListener?.onClassicBtConnect(connect_device)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val dis_connect_device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                SJLog.logBt(TAG, "ACTION_ACL_DISCONNECTED 当前的设备 address:$mCurrAddress")
                SJLog.logBt(
                    TAG,
                    "ACTION_ACL_DISCONNECTED name:" + dis_connect_device!!.name + " address:" + dis_connect_device.address
                )

                if (dis_connect_device.type == BluetoothDevice.DEVICE_TYPE_LE) return

                if (!TextUtils.isEmpty(mCurrAddress) && dis_connect_device.address == mCurrAddress) {
                    try {
                        mSocket?.let {
                            val b = ByteArray(1)
                            it.outputStream.write(b)
                            it.close()
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        mOnBtStateListener?.onClassicBtDisConnect(dis_connect_device)
                    }
                }
            }
        }
    }
}