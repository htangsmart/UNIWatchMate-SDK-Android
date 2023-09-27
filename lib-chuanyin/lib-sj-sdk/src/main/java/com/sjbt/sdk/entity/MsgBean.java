package com.sjbt.sdk.entity;

public class MsgBean {
//    public String head;
    public byte head;
    public byte cmdOrder;
    public String cmdIdStr;
    public int cmdId;

    public byte divideType;
    public int payloadLen;

    public int offset;
    public int crc;
    public int divideIndex;

    public byte[] payload;
    public String payloadJson;

    @Override
    public String toString() {
        return "BiuMsgBean{" +
                "head=" + head +
                ", cmdOrder=" + cmdOrder +
                ", cmdStr='" + cmdIdStr + '\'' +
                ", divideType=" + divideType +
                ", payloadLen=" + payloadLen +
                ", offset=" + offset +
                ", crc=" + crc +
                '}';
    }
}
