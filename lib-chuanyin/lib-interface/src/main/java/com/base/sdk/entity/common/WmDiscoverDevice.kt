package com.base.sdk.entity.common

import android.bluetooth.BluetoothDevice

data class WmDiscoverDevice(val device: BluetoothDevice, val rss:Short){
    override fun toString(): String {
        return "WmDiscoverDevice(device=$device, rss=$rss)"
    }
}