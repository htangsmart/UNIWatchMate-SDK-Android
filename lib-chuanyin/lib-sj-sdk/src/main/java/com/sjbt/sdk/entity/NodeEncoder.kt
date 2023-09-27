package com.sjbt.sdk.entity

import java.nio.ByteBuffer

object NodeEncoder {
    fun buildWriteMsg(payLoad: PayLoad): ByteArray {
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(payLoad.totalLen)
        byteBuffer.put(payLoad.requestId)
        byteBuffer.putInt(payLoad.packageSeq)
        byteBuffer.put(payLoad.request_type)
        byteBuffer.put(payLoad.package_limit)
        byteBuffer.put(payLoad.item_count)

        payLoad.nodeItems.forEach {
            byteBuffer.put(it.nodeId)
            byteBuffer.put(it.reserved)
            byteBuffer.put(it.data_fmt)
            byteBuffer.put(it.data_len)
            byteBuffer.put(it.data)
        }

        return byteBuffer.array()
    }

}