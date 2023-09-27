package com.sjbt.sdk.entity

import java.nio.ByteBuffer

data class PayLoad(
    val requestId: ByteArray,//2 bytes
    val packageSeq: Int,//4 bytes  分包序号
    val request_type: Byte,//1 byte
    val package_limit: ByteArray,//2 bytes 单包大小限制
    val item_count: Byte,//1 byte  节点个数
    val nodeItems: Array<NodeItem>//
) {
    var nodeLen = nodeItems.let {
        var mLen = 0
        it.forEach { node ->
            mLen += node.nodeId.size + node.reserved.size + 1 + node.data_len.size + node.data.size
        }
        mLen
    }

    var totalLen = requestId.size + 4 + 1 + package_limit.size + 1 + nodeItems.size + nodeLen
    override fun toString(): String {
        return "PayLoad(totalLen=$totalLen,requestId=${requestId.contentToString()}, packageSeq=$packageSeq, request_type=$request_type, package_limit=${package_limit.contentToString()}, item_count=$item_count, nodeItems=${nodeItems.contentToString()}, nodeLen=$nodeLen, )"
    }
}

data class NodeItem(
    val nodeId: ByteArray = byteArrayOf(2),//节点ID 4 bytes
    val reserved: ByteArray = byteArrayOf(2),//保留数据 2 bytes
    val data_fmt: Byte,//数据类型
    val data_len: ByteArray = byteArrayOf(2),//数据字节长度 2bytes
    val data: ByteArray//节点数据类型
)

class ErrCode {
    val ERRCODE_NOERR = 0
    val ERRCODE_NODATA = 1
    val ERRCODE_DENIED = 2
}

class DataFormat {
    val FMT_BINARY = 0
    val FMT_PLAINTEXT = 1
    val FMT_JSON = 2
    val FMT_NODATA = 3
    val FMT_ERRCODE = 4
}

class RequestType {
    val ACCESS_READ = 0
    val ACCESS_WRITE = 1
    val ACCESS_EXECUTE = 2
}

class ResponseType {
    val RESPONSE_EACH = 0
    val RESPONSE_ALL_OK = 1
    val RESPONSE_ALL_FAIL = 2
}

val REQUEST_TYPE: ByteArray = byteArrayOf(0x00, 0x00)

const val CONNECT_P = 0x01
const val CONNECT_C_BIND = 0x01
const val CONNECT_C_UNBIND = 0x02

fun buildPayLoadMsg(payLoad: PayLoad): ByteArray {
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


val NODE_ID_BIND = 0x0101
val NODE_ID_UNBIND = 0x0102

val NODE_SPORT_GOAL_SET_STEP = 0x020201
val NODE_SPORT_GOAL_SET_CALORIES = 0x020202
val NODE_SPORT_GOAL_SET_DIS = 0x020203
val NODE_SPORT_GOAL_SET_ACTIVITY_LEN = 0x020204

fun setSportGoal(step: Int, calories: Int, dis: Int, activityLen: Int) {


//    val nodeItemStep = NodeItem()
//    val payLoad = PayLoad()

}


val REQUEST_ID_ARR = arrayOf(
    1,
    2,
    3,
    4,
    5,
    6,
    7,
    8,
    9,
    10,
    11,
    12,
    13,
    14,
    15,
    16,
    17,
    18,
    19,
    20,
    21,
    22,
    23,
    24,
    25,
    26,
    27,
    28,
    29,
    30
)