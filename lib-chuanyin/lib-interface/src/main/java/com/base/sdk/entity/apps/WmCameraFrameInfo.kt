package com.base.sdk.entity.apps

data class WmCameraFrameInfo(
    var frameData: ByteArray,
    var frameType : Int = 0,// I帧==2 和 P帧==0
    var frameId : Long = 0) //时间戳
{
    override fun toString(): String {
        return "WmCameraFrameInfo(frameData=${frameData.contentToString()}, frameType=$frameType, frameId=$frameId)"
    }
}