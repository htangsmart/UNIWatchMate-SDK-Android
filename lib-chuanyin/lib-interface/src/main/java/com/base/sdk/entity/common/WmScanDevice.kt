package com.base.sdk.entity.common

import android.bluetooth.BluetoothDevice

data class WmScanDevice(val device: BluetoothDevice, val rss:Short){
    override fun toString(): String {
        return "WmScanDevice(device=$device, rss=$rss)"
    }
}