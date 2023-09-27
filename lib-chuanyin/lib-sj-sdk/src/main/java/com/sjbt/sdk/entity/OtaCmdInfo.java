package com.sjbt.sdk.entity;

public class OtaCmdInfo {
    public int offSet;
    public byte[] payload;
    public int crc;

    @Override
    public String toString() {
        return "OtaDataInfoNew{" +
                "offSet=" + offSet +
                ", payload=" + payload.length +
                ", crc=" + crc +
                '}';
    }
}