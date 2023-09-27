package com.sjbt.sdk.entity

import java.nio.ByteBuffer

class PayloadPackage {
    var _id: Short = 0
    var packageSeq: Int = 0
    var type: Int = 0
    var packageLimit: Short = 0
    var itemCount: Byte = 0
    var itemList: MutableList<NodeData> = mutableListOf()

    init {
        this._id = RequestIdGenerator.instance.generateRequestId()
    }

    companion object {

        /**
         * 从字节数组中解析出"首包"PayloadPackage
         *
         * @param data
         * @return
         */
        fun fromByteArray(payloadData:ByteArray):PayloadPackage {
            val payload = PayloadPackage()
            val bytes = ByteBuffer.wrap(payloadData)
            bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
            payload._id = bytes.short
            payload.packageSeq = bytes.int

            payload.type = bytes.get().toInt()
            payload.packageLimit = bytes.short
            payload.itemCount = bytes.get()

            //判断bytes是否读完
            while (bytes.hasRemaining()) {
                val nextNode = NodeData.fromByteBuffer(bytes, payload.type)
                payload.itemList.add(nextNode)
            }

            return payload
        }
    }

    private fun buildPackageHeader(bytes:ByteBuffer, isFirst:Boolean = true) {
        bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        bytes.putShort(_id)
        bytes.putInt(packageSeq++)
        if (isFirst) {
            bytes.put(type.toByte())
            bytes.putShort(packageLimit)
            bytes.put(itemCount)
        }
    }

    /**
     * 判断是否还有下一个包
     * @return
     */
    fun hasNext():Boolean {
        return packageSeq != 0xFFFFFFFF.toInt()
    }

    /**
     * 从字节数组中解析出"非首包"PayloadPackage
     *
     * @param data
     */
    fun next(data:ByteArray) {
        val bytes = ByteBuffer.wrap(data)
        bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        _id = bytes.short
        packageSeq = bytes.int
        //判断bytes是否读完
        while (bytes.hasRemaining()) {
            val nextNode = NodeData.fromByteBuffer(bytes, this.type)
            itemList.add(nextNode)
        }
    }

    /**
     * 添加数据
     * @param urn 资源名
     * @param data 数据
     */
    fun putData(urn:ByteArray, data:ByteArray, dataFmt:DataFormat = DataFormat.FMT_BIN) {
        val nodeData = NodeData(urn, data, dataFmt)
        itemList.add(nodeData)
        itemCount++
    }

    /**
     * 将payload转换为byte数组
     * @param mtu MTU
     * @return
     */
    fun toByteArray(mtu:Int = 500): List<ByteArray> {
        val limitation = mtu
        val payloadList = mutableListOf<ByteArray>() //payload列表
        val bytes:ByteBuffer = ByteBuffer.allocate(limitation) //payload
        buildPackageHeader(bytes, true)

        itemList.mapIndexed() { index, item ->
            val nextNode = item.toBytes(type)
            //如果现有的payload长度加上当前item的长度超过了限制，则将现有的payload加入到payloadList中，
            // 并重新计算payload长度
            if (bytes.position() + nextNode.size > limitation) {
                bytes.flip() // Now the limit is set to position
                val actualData = ByteArray(bytes.limit())
                bytes.get(actualData)
                payloadList.add(actualData)

                bytes.clear()
                buildPackageHeader(bytes, false)
            } else {
                bytes.put(nextNode)
            }
        }

        // 如果是最后一个payload，则将packageSeq重置为0xFFFF,并将payload加入到payloadList中
        if (bytes.position() > 0) {
            bytes.putInt(2, 0xFFFFFFFF.toInt())
        }
        payloadList.add(bytes.array())

        return payloadList
    }
}

/**
 * requestId生成器
 */
class RequestIdGenerator {
    companion object {
        val instance: RequestIdGenerator by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RequestIdGenerator()
        }
        var currentRequestId: Short = 0
    }

    /**
     * 生成请求ID,避免多线程并发问题
     * @return 请求ID
     */
    @Synchronized
    fun generateRequestId(): Short {
        //自动生成双字节范围的ID，从0开始逐次递增,到65535后重新开始
        currentRequestId++
        if (currentRequestId == Short.MAX_VALUE) {
            currentRequestId = 0
        }
        return currentRequestId
    }

}