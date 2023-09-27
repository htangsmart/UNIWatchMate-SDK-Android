package com.sjbt.sdk.spp.cmd;

import com.sjbt.sdk.utils.BtUtils;

import java.util.Random;

public class CmdConfig {
    public static final String RANDOM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final int HEX_FFFF = 0xFFFF;

    public static String getRandomNumber(int length) {
        char[] chars = RANDOM.toCharArray();
        long l = System.currentTimeMillis();
        Random random = new Random(l);
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            stringBuffer.append(chars[random.nextInt(chars.length)]);
        }
        return BtUtils.stringToHexString(stringBuffer.toString());
    }

    /**
     * 以下是BiuApp 协议
     * Type 4-bytes | Length 4-bytes | Offset 4-bytes | CRC 4-bytes | Payload 4-bytes
     * 蓝牙命令HEAD
     */
    public static final byte HEAD_VERIFY = (byte) 0X0A;
    public static final byte HEAD_COMMON = (byte) 0X0B;
    public static final byte HEAD_SPORT_HEALTH = (byte) 0X0C;
    public static final byte HEAD_FILE_OPP = (byte) 0X0D;//OPP传输方式
    public static final byte HEAD_FILE_SPP_A_2_D = (byte) 0x0E;//App到设备端发送文件
    public static final byte HEAD_FILE_SPP_D_2_A = (byte) 0xFF;//从设备端到App端传文件
    public static final byte HEAD_DEVICE_ERROR = (byte) 0xEF;//从设备向App端报告错误
    public static final byte HEAD_COLLECT_DEBUG_DATA = (byte) 0xDF;//收集调试数据
    public static final byte HEAD_CAMERA_PREVIEW = (byte) 0x1A;//收集调试数据
    public static final byte HEAD_NODE_TYPE = (byte) 0x1B;//收集调试数据
    public static final byte HEAD_HL_OTA_STEP = (byte) 0x0F;//App到设备端发送文件

    public static final short TRANSFER_KEY = 0X7FFF;

    /**
     * COMMAND_ID 因为方向原因，发送的时候需要Command_Id 与运算 0x7FFF 携带方向 0X8001 & 0x7FFF = 0X0001
     */
    public static final short CMD_ID_8001 =  0X01;
    public static final short CMD_ID_8002 =  0X02;
    public static final short CMD_ID_8003 =  0X03;
    public static final byte CMD_ID_8004 =  0X04;
    public static final byte CMD_ID_8005 =  0X05;
    public static final byte CMD_ID_8006 =  0X06;
    public static final byte CMD_ID_8007 =  0X07;
    public static final byte CMD_ID_8008 =  0X08;
    public static final byte CMD_ID_8009 =  0X09;
    public static final byte CMD_ID_800A =  0X0A;
    public static final byte CMD_ID_800B =  0X0B;
    public static final byte CMD_ID_800C =  0X0C;
    public static final byte CMD_ID_800D =  0X0D;
    public static final byte CMD_ID_800E =  0X0E;
    public static final byte CMD_ID_800F =  0X0F;
    public static final byte CMD_ID_8010 =  0X10;
    public static final byte CMD_ID_8011 =  0X11;
    public static final byte CMD_ID_8012 =  0X12;
    public static final byte CMD_ID_8014 =  0X14;
    public static final byte CMD_ID_8017 =  0X17;
    public static final byte CMD_ID_8018 =  0X18;
    public static final byte CMD_ID_8019 =  0X19;
    public static final byte CMD_ID_801A =  0X1A;
    public static final byte CMD_ID_801B =  0X1B;
    public static final byte CMD_ID_801C =  0X1C;
    public static final byte CMD_ID_801D =  0X1D;
    public static final byte CMD_ID_801E =  0X1E;
    public static final byte CMD_ID_8020 =  0X20;
    public static final byte CMD_ID_8021 =  0X21;
    public static final byte CMD_ID_8022 =  0X22;
    public static final byte CMD_ID_8023 =  0X23;
    public static final byte CMD_ID_8024 =  0X24;
    public static final byte CMD_ID_8025 =  0X25;
    public static final byte CMD_ID_8026 =  0X26;
    public static final byte CMD_ID_8027 =  0x27;
    public static final byte CMD_ID_8028 =  0x28;
    public static final byte CMD_ID_8029 =  0x29;
    public static final byte CMD_ID_802A =  0x2a;
    public static final byte CMD_ID_802B =  0x2b;
    public static final byte CMD_ID_802C =  0x2C;
    public static final byte CMD_ID_802D =  0x2D;
    public static final short CMD_ID_802E =  0x2E;
    public static final short CMD_ID_802F =  0x2F;

    public static final String CMD_STR_8001 = "0180";
    public static final String CMD_STR_8002 = "0280";
    public static final String CMD_STR_8003 = "0380";
    public static final String CMD_STR_8004 = "0480";
    public static final String CMD_STR_8005 = "0580";
    public static final String CMD_STR_8006 = "0680";
    public static final String CMD_STR_8007 = "0780";
    public static final String CMD_STR_8008 = "0880";
    public static final String CMD_STR_8009 = "0980";
    public static final String CMD_STR_800A = "0A80";
    public static final String CMD_STR_800B = "0B80";
    public static final String CMD_STR_800C = "0C80";
    public static final String CMD_STR_800D = "0D80";
    public static final String CMD_STR_800E = "0E80";
    public static final String CMD_STR_800F = "0F80";
    public static final String CMD_STR_8010 = "1080";
    public static final String CMD_STR_8011 = "1180";
    public static final String CMD_STR_8012 = "1280";
    public static final String CMD_STR_8014 = "1480";
    public static final String CMD_STR_8015 = "1580";
    public static final String CMD_STR_8017 = "1780";
    public static final String CMD_STR_8018 = "1880";
    public static final String CMD_STR_8019 = "1980";
    public static final String CMD_STR_801A = "1A80";
    public static final String CMD_STR_801B = "1B80";
    public static final String CMD_STR_801C = "1C80";
    public static final String CMD_STR_801D = "1D80";
    public static final String CMD_STR_801E = "1E80";
    public static final String CMD_STR_8020 = "2080";
    public static final String CMD_STR_8021 = "2180";
    public static final String CMD_STR_8022 = "2280";
    public static final String CMD_STR_8023 = "2380";
    public static final String CMD_STR_8024 = "2480";
    public static final String CMD_STR_8025 = "2580";
    public static final String CMD_STR_8026 = "2680";
    public static final String CMD_STR_8027 = "2780";
    public static final String CMD_STR_8028 = "2880";
    public static final String CMD_STR_8029 = "2980";
    public static final String CMD_STR_802A = "2A80";
    public static final String CMD_STR_802B = "2B80";
    public static final String CMD_STR_802C = "2C80";
    public static final String CMD_STR_802D = "2D80";

    public static final String CMD_STR_8001_TIME_OUT = "0100";
    public static final String CMD_STR_8002_TIME_OUT = "0200";
    public static final String CMD_STR_8003_TIME_OUT = "0300";
    public static final String CMD_STR_8004_TIME_OUT = "0400";
    public static final String CMD_STR_8005_TIME_OUT = "0500";
    public static final String CMD_STR_8006_TIME_OUT = "0600";
    public static final String CMD_STR_8007_TIME_OUT = "0700";
    public static final String CMD_STR_8008_TIME_OUT = "0800";
    public static final String CMD_STR_8009_TIME_OUT = "0900";
    public static final String CMD_STR_800A_TIME_OUT = "0A00";
    public static final String CMD_STR_800B_TIME_OUT = "0B00";
    public static final String CMD_STR_800C_TIME_OUT = "0C00";
    public static final String CMD_STR_800D_TIME_OUT = "0D00";
    public static final String CMD_STR_800E_TIME_OUT = "0E00";
    public static final String CMD_STR_800F_TIME_OUT = "0F00";
    public static final String CMD_STR_8010_TIME_OUT = "1000";
    public static final String CMD_STR_8011_TIME_OUT = "1100";
    public static final String CMD_STR_8012_TIME_OUT = "1200";
    public static final String CMD_STR_8013_TIME_OUT = "1300";
    public static final String CMD_STR_8014_TIME_OUT = "1400";
    public static final String CMD_STR_8017_TIME_OUT = "1700";
    public static final String CMD_STR_8018_TIME_OUT = "1800";
    public static final String CMD_STR_8019_TIME_OUT = "1900";
    public static final String CMD_STR_801A_TIME_OUT = "1A00";
    public static final String CMD_STR_801B_TIME_OUT = "1B00";
    public static final String CMD_STR_801C_TIME_OUT = "1C00";
    public static final String CMD_STR_801E_TIME_OUT = "1E00";
    public static final String CMD_STR_8020_TIME_OUT = "2000";
    public static final String CMD_STR_8021_TIME_OUT = "2100";
    public static final String CMD_STR_8022_TIME_OUT = "2200";
    public static final String CMD_STR_8023_TIME_OUT = "2300";
    public static final String CMD_STR_8024_TIME_OUT = "2400";
    public static final String CMD_STR_8025_TIME_OUT = "2500";
    public static final String CMD_STR_8026_TIME_OUT = "2600";
    public static final String CMD_STR_8027_TIME_OUT = "2700";
    public static final String CMD_STR_8028_TIME_OUT = "2800";
    public static final String CMD_STR_8029_TIME_OUT = "2900";
    public static final String CMD_STR_802A_TIME_OUT = "2A00";
    public static final String CMD_STR_802B_TIME_OUT = "2B00";
    public static final String CMD_STR_802C_TIME_OUT = "2C00";
    public static final String CMD_STR_802D_TIME_OUT = "2D00";

    /**
     * 循环使用order_id
     */
    public static final byte[] CMD_ORDER_ARRAY = {
            0X01, 0X02, 0X03, 0X04, 0X05, 0X06, 0X07, 0X08, 0X09, 0X0A,
            0X0B, 0X0C, 0X0D, 0X0E, 0X0F
    };

    /**
     * 蓝牙命令组装通用方法
     * <p>
     * bit0~bit1:
     * [00] 不分片
     * [01] 分片，首包
     * [10] 分片，中间包
     * [11] 分片，尾包
     * bit3: [0]二进制数据；[1]json数据
     * bit4~bit7: 保留
     * <p>
     * 枚举所有命令对应十进制整数:
     * 不分片   二进制 对应二进制：00000000 = 0
     * 不分片    json 对应二进制：00000100 = 4
     * <p>
     * 分片首包 二进制 对应二进制：00000010 = 2
     * 分片中包 二进制 对应二进制：00000001 = 1
     * 分片尾包 二进制 对应二进制：00000011 = 3
     * 分片首包  json 对应二进制：00000110 = 6
     * 分片中包  json 对应二进制：00000101 = 5
     * 分片尾包  json 对应二进制：00000111 = 7
     *
     * @param offset     偏移量
     * @param crc
     * @param payload
     * @return
     */
    public static final byte DIVIDE_N_2 = 0b000;
    public static final byte DIVIDE_N_JSON = 0b100;
    public static final byte DIVIDE_Y_F_2 = 0b001;
    public static final byte DIVIDE_Y_M_2 = 0b010;
    public static final byte DIVIDE_Y_E_2 = 0b011;
    public static final byte DIVIDE_Y_F_JSON = 0b101;
    public static final byte DIVIDE_Y_M_JSON = 0b110;
    public static final byte DIVIDE_Y_E_JSON = 0b111;

    public static final int DIAL_MSG_LEN = 17;
    public static final int CONTACT_MSG_LEN = 80;
    public static final int CONTACT_NAME_LEN = 60;
    public static final int CONTACT_PHONE_LEN = 20;

    public static final int BT_MSG_BASE_LEN = 16;

    public static final int TIME_SYNC_SET = 1;//设置自动同步时间
    public static final int TIME_SYNC_SEARCH = 2;//查询是否打开时间同步
    public static final int CONTACT_ACTION_LIST = 1, CONTACT_ACTION_ADD = 2, CONTACT_ACTION_DELETE = 3;

}
