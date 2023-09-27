package com.sjbt.sdk.entity;

public class CameraFrameInfo {
    public int state;//0 新鲜的 1 跳过的 2发送了的
    public byte[] frameData;
    public byte frameType;// I帧==2 和 P帧==0
    public long frameId;//时间戳

    @Override
    public String toString() {
        return "CameraFrameInfo{" +
                "state=" + state +
                ", frameType=" + frameType +
                ", frameId=" + frameId +
                ", frameData 长度 = " + frameData.length +
                '}';
    }
}