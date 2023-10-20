package com.sjbt.sdk.entity;

import static com.sjbt.sdk.SJConfigKt.TAG_SJ;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8001;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8002;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8003;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8004;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_800D;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_800F;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_802E;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_CAMERA_PREVIEW;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_COMMON;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_FILE_SPP_A_2_D;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_NODE_TYPE;
import android.util.Log;
import java.nio.ByteBuffer;

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

    public Boolean isNotTimeOut() {
        return (head == HEAD_COMMON && cmdId == CMD_ID_800D)//绑定
                || (head == HEAD_COMMON && cmdId == CMD_ID_800F)//我的表盘列表
                || (head == HEAD_COMMON && cmdId == CMD_ID_802E)//绑定

                || (head == HEAD_FILE_SPP_A_2_D && cmdId == CMD_ID_8003)//传输文件的过程中，采用连续传输的方式

                || (head == HEAD_CAMERA_PREVIEW && cmdId == CMD_ID_8002)//相机预览

                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8001)//节点消息
                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8002)//节点消息
                || (head == HEAD_NODE_TYPE && cmdId == CMD_ID_8004)//节点消息
                ;
    }

    public Boolean isNodeMsg() {
        return head == HEAD_NODE_TYPE;
    }

    public String getTimeOutCode() {
        String timeOutCode;

        if (isNodeMsg()) {
            short requestId = 0;
            if (payload != null && payload.length > 2) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
                requestId = byteBuffer.getShort();
            }

            timeOutCode = "" + head + cmdOrder + cmdId + requestId;
        } else {
            timeOutCode = "" + head + cmdOrder + cmdId;
        }

        Log.e(TAG_SJ, "timeOutCode:" + timeOutCode);

        return timeOutCode;
    }

}
