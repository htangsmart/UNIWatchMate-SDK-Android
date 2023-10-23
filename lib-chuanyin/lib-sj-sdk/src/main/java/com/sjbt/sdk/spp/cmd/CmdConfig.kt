package com.sjbt.sdk.spp.cmd

const val RANDOM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
const val HEX_FFFF = 0xFFFF

/**
 * 以下是BiuApp 协议
 * Type 4-bytes | Length 4-bytes | Offset 4-bytes | CRC 4-bytes | Payload 4-bytes
 * 蓝牙命令HEAD
 */
const val HEAD_VERIFY = 0X0A.toByte()
const val HEAD_COMMON = 0X0B.toByte()
const val HEAD_SPORT_HEALTH = 0X0C.toByte()
const val HEAD_FILE_OPP = 0X0D.toByte() //OPP传输方式
const val HEAD_FILE_SPP_A_2_D = 0x0E.toByte() //App到设备端发送文件
const val HEAD_FILE_SPP_D_2_A = 0xFF.toByte() //从设备端到App端传文件
const val HEAD_DEVICE_ERROR = 0xEF.toByte() //从设备向App端报告错误
const val HEAD_COLLECT_DEBUG_DATA = 0xDF.toByte() //收集调试数据
const val HEAD_CAMERA_PREVIEW = 0x1A.toByte() //相机预览头
const val HEAD_NODE_TYPE = 0x30.toByte() //节点数据头
const val TRANSFER_KEY: Short = 0X7FFF

/**
 * COMMAND_ID 因为方向原因，发送的时候需要Command_Id 与运算 0x7FFF 携带方向 0X8001 & 0x7FFF = 0X0001
 */
const val CMD_ID_8001: Short = 0X01
const val CMD_ID_8002: Short = 0X02
const val CMD_ID_8003: Short = 0X03
const val CMD_ID_8004: Short = 0X04
const val CMD_ID_8005: Short = 0X05
const val CMD_ID_8006: Short = 0X06
const val CMD_ID_8007: Short = 0X07
const val CMD_ID_8008: Short = 0X08
const val CMD_ID_8009: Short = 0X09
const val CMD_ID_800A: Short = 0X0A
const val CMD_ID_800B: Short = 0X0B
const val CMD_ID_800C: Short = 0X0C
const val CMD_ID_800D: Short = 0X0D
const val CMD_ID_800E: Short = 0X0E
const val CMD_ID_800F: Short = 0X0F
const val CMD_ID_8010: Short = 0X10
const val CMD_ID_8011: Short = 0X11
const val CMD_ID_8012: Short = 0X12
const val CMD_ID_8014: Short = 0X14
const val CMD_ID_8017: Short = 0X17
const val CMD_ID_8018: Short = 0X18
const val CMD_ID_8019: Short = 0X19
const val CMD_ID_801A: Short = 0X1A
const val CMD_ID_801B: Short = 0X1B
const val CMD_ID_801C: Short = 0X1C
const val CMD_ID_801D: Short = 0X1D
const val CMD_ID_801E: Short = 0X1E
const val CMD_ID_8020: Short = 0X20
const val CMD_ID_8021: Short = 0X21
const val CMD_ID_8022: Short = 0X22
const val CMD_ID_8023: Short = 0X23
const val CMD_ID_8024: Short = 0X24
const val CMD_ID_8025: Short = 0X25
const val CMD_ID_8026: Short = 0X26
const val CMD_ID_8027: Short = 0x27
const val CMD_ID_8028: Short = 0x28
const val CMD_ID_8029: Short = 0x29
const val CMD_ID_802A: Short = 0x2a
const val CMD_ID_802B: Short = 0x2b
const val CMD_ID_802C: Short = 0x2C
const val CMD_ID_802D: Short = 0x2D
const val CMD_ID_802E: Short = 0x2E
const val CMD_ID_802F: Short = 0x2F

const val CMD_STR_8001 = "0180"
const val CMD_STR_8002 = "0280"
const val CMD_STR_8003 = "0380"
const val CMD_STR_8004 = "0480"
const val CMD_STR_8005 = "0580"
const val CMD_STR_8006 = "0680"
const val CMD_STR_8007 = "0780"
const val CMD_STR_8008 = "0880"
const val CMD_STR_8009 = "0980"
const val CMD_STR_800A = "0A80"
const val CMD_STR_800B = "0B80"
const val CMD_STR_800C = "0C80"
const val CMD_STR_800D = "0D80"
const val CMD_STR_800E = "0E80"
const val CMD_STR_800F = "0F80"
const val CMD_STR_8010 = "1080"
const val CMD_STR_8011 = "1180"
const val CMD_STR_8012 = "1280"
const val CMD_STR_8014 = "1480"
const val CMD_STR_8015 = "1580"
const val CMD_STR_8017 = "1780"
const val CMD_STR_8018 = "1880"
const val CMD_STR_8019 = "1980"
const val CMD_STR_801A = "1A80"
const val CMD_STR_801B = "1B80"
const val CMD_STR_801C = "1C80"
const val CMD_STR_801D = "1D80"
const val CMD_STR_801E = "1E80"
const val CMD_STR_8020 = "2080"
const val CMD_STR_8021 = "2180"
const val CMD_STR_8022 = "2280"
const val CMD_STR_8023 = "2380"
const val CMD_STR_8024 = "2480"
const val CMD_STR_8025 = "2580"
const val CMD_STR_8026 = "2680"
const val CMD_STR_8027 = "2780"
const val CMD_STR_8028 = "2880"
const val CMD_STR_8029 = "2980"
const val CMD_STR_802A = "2A80"
const val CMD_STR_802B = "2B80"
const val CMD_STR_802C = "2C80"
const val CMD_STR_802D = "2D80"
const val CMD_STR_8001_TIME_OUT = "0100"
const val CMD_STR_8002_TIME_OUT = "0200"
const val CMD_STR_8003_TIME_OUT = "0300"
const val CMD_STR_8004_TIME_OUT = "0400"
const val CMD_STR_8005_TIME_OUT = "0500"
const val CMD_STR_8006_TIME_OUT = "0600"
const val CMD_STR_8007_TIME_OUT = "0700"
const val CMD_STR_8008_TIME_OUT = "0800"
const val CMD_STR_8009_TIME_OUT = "0900"
const val CMD_STR_800A_TIME_OUT = "0A00"
const val CMD_STR_800B_TIME_OUT = "0B00"
const val CMD_STR_800C_TIME_OUT = "0C00"
const val CMD_STR_800D_TIME_OUT = "0D00"
const val CMD_STR_800E_TIME_OUT = "0E00"
const val CMD_STR_800F_TIME_OUT = "0F00"
const val CMD_STR_8010_TIME_OUT = "1000"
const val CMD_STR_8011_TIME_OUT = "1100"
const val CMD_STR_8012_TIME_OUT = "1200"
const val CMD_STR_8013_TIME_OUT = "1300"
const val CMD_STR_8014_TIME_OUT = "1400"
const val CMD_STR_8017_TIME_OUT = "1700"
const val CMD_STR_8018_TIME_OUT = "1800"
const val CMD_STR_8019_TIME_OUT = "1900"
const val CMD_STR_801A_TIME_OUT = "1A00"
const val CMD_STR_801B_TIME_OUT = "1B00"
const val CMD_STR_801C_TIME_OUT = "1C00"
const val CMD_STR_801E_TIME_OUT = "1E00"
const val CMD_STR_8020_TIME_OUT = "2000"
const val CMD_STR_8021_TIME_OUT = "2100"
const val CMD_STR_8022_TIME_OUT = "2200"
const val CMD_STR_8023_TIME_OUT = "2300"
const val CMD_STR_8024_TIME_OUT = "2400"
const val CMD_STR_8025_TIME_OUT = "2500"
const val CMD_STR_8026_TIME_OUT = "2600"
const val CMD_STR_8027_TIME_OUT = "2700"
const val CMD_STR_8028_TIME_OUT = "2800"
const val CMD_STR_8029_TIME_OUT = "2900"
const val CMD_STR_802A_TIME_OUT = "2A00"
const val CMD_STR_802B_TIME_OUT = "2B00"
const val CMD_STR_802C_TIME_OUT = "2C00"
const val CMD_STR_802D_TIME_OUT = "2D00"

/**
 * 循环使用order_id
 */
val CMD_ORDER_ARRAY = byteArrayOf(
    0X01, 0X02, 0X03, 0X04, 0X05, 0X06, 0X07, 0X08, 0X09, 0X0A,
    0X0B, 0X0C, 0X0D, 0X0E, 0X0F
)

/**
 * 蓝牙命令组装通用方法
 *
 *
 * bit0~bit1:
 * [00] 不分片
 * [01] 分片，首包
 * [10] 分片，中间包
 * [11] 分片，尾包
 * bit3: [0]二进制数据；[1]json数据
 * bit4~bit7: 保留
 *
 *
 * 枚举所有命令对应十进制整数:
 * 不分片   二进制 对应二进制：00000000 = 0
 * 不分片    json 对应二进制：00000100 = 4
 *
 *
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
const val DIVIDE_N_2: Byte = 0
const val DIVIDE_N_JSON: Byte = 4
const val DIVIDE_Y_F_2: Byte = 1
const val DIVIDE_Y_M_2: Byte = 2
const val DIVIDE_Y_E_2: Byte = 3
const val DIVIDE_Y_F_JSON: Byte = 5
const val DIVIDE_Y_M_JSON: Byte = 6
const val DIVIDE_Y_E_JSON: Byte = 7

const val DIAL_MSG_LEN = 17
const val CONTACT_MSG_LEN = 80
const val CONTACT_NAME_LEN = 60
const val CONTACT_PHONE_LEN = 20
const val BT_MSG_BASE_LEN = 16
const val TIME_SYNC_SET = 1 //设置自动同步时间
const val TIME_SYNC_SEARCH = 2 //查询是否打开时间同步
const val CONTACT_ACTION_LIST = 1
const val CONTACT_ACTION_ADD = 2
const val CONTACT_ACTION_DELETE = 3

/**
 * 节点类型数据配置
 **/
const val URN_0: Byte = '0'.code.toByte()
const val URN_1: Byte = '1'.code.toByte()
const val URN_2: Byte = '2'.code.toByte()
const val URN_3: Byte = '3'.code.toByte()
const val URN_4: Byte = '4'.code.toByte()
const val URN_5: Byte = '5'.code.toByte()
const val URN_6: Byte = '6'.code.toByte()
const val URN_7: Byte = '7'.code.toByte()
const val URN_8: Byte = '8'.code.toByte()
const val URN_9: Byte = '9'.code.toByte()
const val URN_A: Byte = 'A'.code.toByte()
const val URN_B: Byte = 'B'.code.toByte()
const val URN_C: Byte = 'C'.code.toByte()
const val URN_D: Byte = 'D'.code.toByte()
const val URN_E: Byte = 'E'.code.toByte()
const val URN_F: Byte = 'F'.code.toByte()
const val URN_G: Byte = 'G'.code.toByte()
const val URN_H: Byte = 'H'.code.toByte()

const val URN_CONNECT: Byte = URN_1
const val URN_SETTING: Byte = URN_2
const val URN_SETTING_SPORT: Byte = URN_1
const val URN_SETTING_SPORT_STEP: Byte = URN_1
const val URN_SETTING_PERSONAL: Byte = URN_2
const val URN_SETTING_UNIT: Byte = URN_3
const val URN_SETTING_LANGUAGE: Byte = URN_4
const val URN_SETTING_LANGUAGE_LIST: Byte = URN_1
const val URN_SETTING_LANGUAGE_SET: Byte = URN_2

const val URN_SETTING_SEDENTARY: Byte = URN_5
const val URN_SETTING_DRINK: Byte = URN_6
const val URN_SETTING_DATE_TIME: Byte = URN_7
const val URN_SETTING_SOUND: Byte = URN_8
const val URN_SETTING_ARM: Byte = URN_9
const val URN_SETTING_APP_VIEW: Byte = URN_A
const val URN_SETTING_DEVICE_INFO: Byte = URN_B

const val URN_APP: Byte = URN_4
const val URN_APP_ALARM: Byte = URN_1
const val URN_APP_ALARM_ADD: Byte = URN_2
const val URN_APP_ALARM_LIST: Byte = URN_1
const val URN_APP_ALARM_UPDATE: Byte = URN_3
const val URN_APP_ALARM_DELETE: Byte = URN_4

const val URN_APP_SPORT: Byte = URN_2

const val URN_APP_CONTACT: Byte = URN_3
const val URN_APP_CONTACT_COUNT: Byte = URN_1
const val URN_APP_CONTACT_LIST: Byte = URN_2
const val URN_APP_CONTACT_UPDATE: Byte = URN_3
const val URN_APP_CONTACT_SET_EMERGENCY: Byte = URN_4
const val URN_APP_CONTACT_GET_EMERGENCY: Byte = URN_5

const val URN_APP_WEATHER: Byte = URN_4
const val URN_APP_WEATHER_PUSH_TODAY: Byte = URN_1
const val URN_APP_WEATHER_PUSH_SIX_DAYS: Byte = URN_2
const val URN_APP_RATE: Byte = URN_5


const val URN_APP_CONTROL: Byte = URN_5
const val URN_APP_FIND_PHONE: Byte = URN_1
const val URN_APP_FIND_PHONE_START: Byte = URN_1
const val URN_APP_FIND_PHONE_STOP: Byte = URN_2

const val URN_APP_FIND_DEVICE: Byte = URN_2
const val URN_APP_FIND_DEVICE_START: Byte = URN_1
const val URN_APP_FIND_DEVICE_STOP: Byte = URN_2

const val URN_APP_MUSIC_CONTROL: Byte = URN_4

const val URN_SPORT: Byte = URN_6

const val CHANGE_CAMERA = 0.toByte()
const val CHANGE_FLASH = 1.toByte()

const val DEFAULT_ITEM_MAX_LEN = 600