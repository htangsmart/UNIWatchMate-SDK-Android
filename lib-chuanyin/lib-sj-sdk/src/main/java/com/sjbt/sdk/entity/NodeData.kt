package com.sjbt.sdk.entity

import java.nio.ByteBuffer

class NodeData(
    /**
     * unique resource name, 4 bytes
     */
    var urn: ByteArray,
    var data: ByteArray,
    var dataFmt: DataFormat = DataFormat.FMT_BIN
) {
    var dataLen: Short = 0

    init {
        this.dataLen = data.size.toShort()
    }

    constructor() : this(byteArrayOf(), byteArrayOf(), DataFormat.FMT_BIN) {
    }

    companion object {
        fun fromByteBuffer(bytes: ByteBuffer, type: Int): NodeData {
            val nodeData = NodeData()
            nodeData.urn = ByteArray(4)
            bytes.get(nodeData.urn)
            if (type != RequestType.REQ_TYPE_READ.type) {
                nodeData.dataFmt = DataFormat.values()[bytes.get().toInt()]
                nodeData.dataLen = bytes.short
                nodeData.data = ByteArray(nodeData.dataLen.toInt())
                bytes.get(nodeData.data)
            }
            return nodeData
        }
    }

    override fun toString(): String {
        return "BaseNodeData(urn=${urn.contentToString()}, dataFmt=$dataFmt, dataLen=$dataLen, data=$data)"
    }

    fun toBytes(request_type: Int): ByteArray {
        val size = 4 + (if (request_type != RequestType.REQ_TYPE_READ.type) 3 + data.size else 0)
        val bytes: ByteBuffer = ByteBuffer.allocate(size)
        bytes.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        bytes.put(urn)
        if (request_type != RequestType.REQ_TYPE_READ.type) {
            bytes.put(dataFmt.ordinal.toByte())
            bytes.putShort(dataLen)
            for (item in data) {
                bytes.put(item)
            }
        }

        return bytes.array()
    }
}

enum class DataFormat {
    FMT_BIN,
    FMT_PLAIN_TXT,
    FMT_JSON,
    FMT_NODATA,
    FMT_ERRCODE
}

enum class ErrorCode {
    ERR_CODE_OK,
    ERR_CODE_FAIL,
    ERR_CODE_NODATA,
    ERR_CODE_INVALID_PARAM,
    ERR_CODE_INVALID_URN,
    ERR_CODE_INVALID_DATA,
    ERR_CODE_INVALID_CMD,
    ERR_CODE_INVALID_PACKAGE,
    ERR_CODE_INVALID_PACKAGE_SEQ,
    ERR_CODE_INVALID_PACKAGE_LIMIT,
    ERR_CODE_INVALID_ITEM_COUNT,
    ERR_CODE_INVALID_ITEM_LIST,
    ERR_CODE_INVALID_ITEM_DATA,
    ERR_CODE_INVALID_ITEM_DATA_LEN,
    ERR_CODE_INVALID_ITEM_DATA_FMT,
    ERR_CODE_INVALID_ITEM_DATA_URN,
}

enum class RequestType(val type: Int) {
    REQ_TYPE_INVALID(0),
    REQ_TYPE_READ(1),
    REQ_TYPE_WRITE(2),
    REQ_TYPE_EXECUTE(3),

    RESP_TYPE_EACH(100),
    RESP_TYPE_ALL_OK(101),
    RESP_TYPE_ALL_FAIL(102)
}

enum class ResponseResultType(val type: Int) {
    RESPONSE_EACH(100),
    RESPONSE_ALL_OK(1),
    RESPONSE_ALL_FAIL(2),
}
