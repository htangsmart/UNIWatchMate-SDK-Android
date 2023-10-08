package com.sjbt.sdk.spp.cmd

import android.text.TextUtils
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.entity.settings.WmUnitInfo
import com.google.gson.Gson
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.log.SJLog.logSendMsg
import com.sjbt.sdk.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object CmdHelper {
    //对CMD_ORDER.length 取余后作为 CMD_ORDER 的索引下标
    var command_index = 0
    private var key1 = 0
    private var mKey1: String? = null
    private var mKey2: String? = null
    private var mKeyData1: String? = null
    private var mKeyData2: String? = null
    private val gson = Gson()
    fun constructCmd(
        head: Byte,
        cmd_id: Short,
        divideType: Byte,
        offset: Int,
        crc: Int,
        payload: ByteArray?
    ): ByteArray {
        val payLoadLength = payload?.size ?: 0
        val byteBuffer = ByteBuffer.allocate(16 + payLoadLength)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN) //采用小端

        //TYPE
        byteBuffer.put(head)
        byteBuffer.put(CMD_ORDER_ARRAY[command_index % CMD_ORDER_ARRAY.size])
        byteBuffer.putShort((cmd_id.toInt() and TRANSFER_KEY.toInt()).toShort()) //携带方向

        //Length
        byteBuffer.put(divideType)
        byteBuffer.put(0x00.toByte()) //Resolved
        byteBuffer.putShort(payLoadLength.toShort())

        //Offset
        byteBuffer.putInt(offset)

        //CRC
        byteBuffer.putInt(crc)

        //Payload
        if (payload != null) {
            byteBuffer.put(payload)
        }
        byteBuffer.flip()
        command_index++
        return byteBuffer.array()
    }

    fun getRandomNumber(length: Int): String? {
        val chars = RANDOM.toCharArray()
        val l = System.currentTimeMillis()
        val random = Random(l)
        val stringBuffer = StringBuffer()
        for (i in 0 until length) {
            stringBuffer.append(chars[random.nextInt(chars.size)])
        }
        return BtUtils.stringToHexString(stringBuffer.toString())
    }

    /**
     * 获取JSON格式的 Payload数据
     *
     * @param msg_result
     * @return
     */
    @JvmStatic
    fun getPayLoadJson(msg_result: ByteArray): MsgBean {
        val msgBean = MsgBean()
        try {
            val byteBuffer = ByteBuffer.wrap(msg_result)
            msgBean.head = byteBuffer.get()
            msgBean.cmdOrder = byteBuffer.get()
            val cmdId = ByteArray(2)
            System.arraycopy(msg_result, 2, cmdId, 0, cmdId.size)
            msgBean.cmdIdStr = BtUtils.bytesToHexString(cmdId)
            val temp = cmdId[0]
            cmdId[0] = cmdId[1]
            cmdId[1] = temp
            cmdId[0] = 0x00
            msgBean.cmdId = BtUtils.byte2short(cmdId).toInt()
            //            LogUtils.logBlueTooth("返回命令cmdId:" + msgBean.cmdId);
            val divideType = byteBuffer[4]
            msgBean.divideType = divideType

//            byte[] len = new byte[2];
//            System.arraycopy(msg_result, 6, len, 0, len.length);
//
//            byte a = len[0];
//            byte b = len[1];
//
//            len[0] = b;
//            len[1] = a;

//            String lenHex = BtUtils.bytesToHexString(len);
//            int length = BtUtils.hexToInt(lenHex);
            val payLoadLength = msg_result.size - 16
            //            LogUtils.logBlueTooth("lenHex:" + lenHex);
//            LogUtils.logBlueTooth("length:" + length);
//            LogUtils.logBlueTooth("divideType:" + divideType);
            msgBean.payloadLen = payLoadLength
            val offsetArray = ByteArray(4)
            System.arraycopy(msg_result, 8, offsetArray, 0, offsetArray.size)
            msgBean.offset = ByteUtil.bytesToInt(offsetArray, ByteOrder.LITTLE_ENDIAN)
            val crcArray = ByteArray(4)
            System.arraycopy(msg_result, 12, crcArray, 0, crcArray.size)
            msgBean.crc = ByteUtil.bytesToInt(crcArray, ByteOrder.LITTLE_ENDIAN)

//            LogUtils.logBlueTooth("offset:" + msgBean.offset);
//            LogUtils.logBlueTooth("crc:" + msgBean.crc);
//            LogUtils.logBlueTooth("length:" + length);
            if (msgBean.divideType == DIVIDE_N_2 || msgBean.divideType == DIVIDE_N_JSON) {
                if (payLoadLength > 0) {
                    val payload = ByteArray(payLoadLength)
                    System.arraycopy(msg_result, 16, payload, 0, payLoadLength)
                    msgBean.payload = payload
                    if (divideType == DIVIDE_N_JSON) {
                        val payloadJson = String(payload, StandardCharsets.UTF_8)
                        msgBean.payloadJson = payloadJson
                    }
                }
            } else {
                val divideIndexArray = ByteArray(4)
                System.arraycopy(msg_result, 16, divideIndexArray, 0, divideIndexArray.size)
                msgBean.divideIndex = ByteUtil.bytesToInt(divideIndexArray)

//                LogUtils.logBlueTooth("分包序号HEX：" + BtUtils.bytesToHexString(divideIndexArray));
//                LogUtils.logBlueTooth("分包序号INT：" + msgBean.divideIndex);
                val payload = ByteArray(payLoadLength - 4)
                System.arraycopy(msg_result, 20, payload, 0, payload.size)
                msgBean.payload = payload
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return msgBean
    }

    fun packageFileToCmdList(file: File, cell_length: Int): List<ByteArray> {
        var divide: Byte
        var otaPckCrc = 0
        val dataArray = FileUtils.readFileBytes(file)
        val otaCmdArrayList: MutableList<ByteArray> = ArrayList()
        if (!file.exists()) {
            return otaCmdArrayList
        }
        var count = dataArray.size / cell_length
        val lastDataLength = dataArray.size % cell_length
        if (lastDataLength != 0) {
            count = count + 1
        }
        for (i in 0 until count) {
            val info = OtaCmdInfo()
            if (i != count - 1) {
                info.offSet = i * cell_length
                info.payload = ByteArray(cell_length)
                System.arraycopy(dataArray, i * cell_length, info.payload, 0, cell_length)
            } else {
                if (lastDataLength == 0) {
                    info.offSet = i * cell_length
                    info.payload = ByteArray(cell_length)
                    System.arraycopy(dataArray, i * cell_length, info.payload, 0, cell_length)
                } else {
                    info.offSet = i * cell_length
                    info.payload = ByteArray(lastDataLength)
                    System.arraycopy(dataArray, i * cell_length, info.payload, 0, lastDataLength)
                }
            }
            if (i == 0) {
                otaPckCrc = BtUtils.getCrc(HEX_FFFF, info.payload, info.payload.size)
                divide = DIVIDE_Y_F_2
            } else {
                divide = if (i == count - 1) {
                    DIVIDE_Y_E_2
                } else {
                    DIVIDE_Y_M_2
                }
                otaPckCrc = BtUtils.getCrc(otaPckCrc, info.payload, info.payload.size)
            }
            info.crc = otaPckCrc
            otaCmdArrayList.add(getTransfer03Cmd(i, info, divide))
        }
        return otaCmdArrayList
    }

    /**
     * 计算出key2
     *
     * @param data
     */
    fun getTheAccumulatedValueAnd(data: String): String {
        if (TextUtils.isEmpty(data)) {
            return ""
        }
        var total = 0
        val len = data.length
        var num = 0
        while (num < len) {
            val s = data.substring(num, num + 2)
            total += s.toInt(16)
            num = num + 2
        }
        return Integer.toHexString(total)
    }//密钥
    //异或原参
    //        LogUtils.logBlueTooth("APP加密的Key1:" + mKey1);
    //未加密数据

//        LogUtils.logBlueTooth("APP未加密的数据:" + verificationArray[4]);

//        LogUtils.logBlueTooth("APP加密后的数据:" + verificationArray[2]);
    //加密后累加

//        LogUtils.logBlueTooth("APP未加密的数据_mKeyData2 :" + mKeyData2);
//        LogUtils.logBlueTooth("APP解密累加后 KEY2:" + mKey2);
    /**
     * 新协议 获取校验命令
     *
     * @return
     */
    val verifyPayload: ByteArray
        get() {
            val verificationArray = arrayOfNulls<String>(5)
            verificationArray[0] = getRandomNumber(14)
            verificationArray[1] = getRandomNumber(2) //密钥
            verificationArray[2] = getRandomNumber(32)
            verificationArray[3] = getRandomNumber(16) //异或原参

            val bytes = BtUtils.hexStringToByteArray(verificationArray[2])
            key1 = verificationArray[1]!!.toInt(16)
            mKey1 = BtUtils.intToHex(key1)
            //        LogUtils.logBlueTooth("APP加密的Key1:" + mKey1);
            verificationArray[4] = BtUtils.bytesToHexString(bytes) //未加密数据

//        LogUtils.logBlueTooth("APP未加密的数据:" + verificationArray[4]);
            verificationArray[2] =
                BtUtils.bytesToHexString(BtUtils.encryptData(key1, bytes, bytes.size))

//        LogUtils.logBlueTooth("APP加密后的数据:" + verificationArray[2]);
            mKeyData1 = verificationArray[3]
            mKeyData2 = verificationArray[4]
            mKey2 = BtUtils.getTheAccumulatedValueAnd(verificationArray[2]) //加密后累加

//        LogUtils.logBlueTooth("APP未加密的数据_mKeyData2 :" + mKeyData2);
//        LogUtils.logBlueTooth("APP解密累加后 KEY2:" + mKey2);
            val sbVerify = StringBuilder()
            sbVerify.append(verificationArray[0])
            sbVerify.append(verificationArray[1])
            sbVerify.append(verificationArray[2])
            sbVerify.append(verificationArray[3])
            return BtUtils.hexStringToByteArray(sbVerify.toString())
        }

    /**
     * 解密并验证数据
     *
     * @param data 从设备返回的数据
     * @return
     */
    fun verificationCmd(data: String): Boolean {

//        LogUtils.logBlueTooth("verificationCmd 设备返回payload：" + data);
        val substring = data.substring(32, 96)
        //        LogUtils.logBlueTooth("verificationCmd: new 10-2F： " + substring);
        val bytes = BtUtils.hexStringToByteArray(substring)
        val key = mKey2!!.toInt(16)
        //解密数据
        val s2 = BtUtils.bytesToHexString(BtUtils.encryptData(key, bytes, bytes.size))
        //        LogUtils.logBlueTooth("verificationCmd:解密后的10-2F： " + s2);
        //异或
        val substring1 = s2.substring(0, 32)
        val substring2 = s2.substring(32)
        val bytes1 = BtUtils.hexStringToByteArray(substring1)
        val bytes2 = BtUtils.hexStringToByteArray(substring2)
        val bytes3 = BtUtils.hexStringToByteArray(mKeyData1)
        val bytes4 = ByteArray(bytes1.size + bytes2.size)
        for (j in bytes1.indices) {
            bytes4[j] = (bytes1[j].toInt() xor bytes3[j].toInt()).toByte()
        }
        for (j in bytes2.indices) {
            bytes4[j + bytes2.size] = (bytes2[j].toInt() xor bytes3[j].toInt()).toByte()
        }

//        LogUtils.logBlueTooth("解密的key1：" + key + " | - key2:" + key);

        //解密
        val s3 = BtUtils.bytesToHexString(BtUtils.encryptData(key1, bytes4, bytes4.size))
        //        LogUtils.logBlueTooth("verificationCmd:异或后的数据： " + BtUtils.bytesToHexString(bytes4));
//        LogUtils.logBlueTooth("verificationCmd:加密后的数据： " + s3);
        return BtUtils.bytesToHexString(bytes4).equals(mKeyData2, ignoreCase = true)
    }

    /**
     * 新协议 握手命令
     *
     * @return 组装好的握手命令
     */
    val biuShakeHandsCmd: ByteArray
        get() {
            val payload = BtUtils.hexStringToByteArray(getRandomNumber(61))
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            LogUtils.logBlueTooth("发送握手消息:")
            return constructCmd(
                HEAD_VERIFY,
                CMD_ID_8001.toShort(),
                DIVIDE_N_2,
                0,
                crc,
                payload
            )
        }

    /**
     * 获取绑定命令
     * @return
     */
    fun getBindCmd(bindInfo: WmBindInfo): ByteArray {
        LogUtils.logBlueTooth("绑定命令")
        val byteBuffer = ByteBuffer.allocate(17)
        byteBuffer.put(bindInfo.bindType.ordinal.toByte())

        bindInfo.randomCode?.let {
            LogUtils.logBlueTooth("随机码:$it")
            byteBuffer.put(it.toByteArray())
        }

        val payload = byteBuffer.array()
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)

        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802E,
            DIVIDE_N_2,
            0,
            crc,
            payload
        )
    }

    /**
     * 获取绑定命令
     * @return
     */
    fun getUnBindCmd(): ByteArray {
        LogUtils.logBlueTooth("解绑命令")

        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802F,
            DIVIDE_N_2,
            0,
            0,
            null
        )
    }

    /**
     * 新协议 校验命令
     *
     * @return 组装好的校验命令
     */
    val biuVerifyCmd: ByteArray
        get() {
            val payload = verifyPayload
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            LogUtils.logBlueTooth("2.发送校验信息:")
            return constructCmd(
                HEAD_VERIFY,
                CMD_ID_8002.toShort(),
                DIVIDE_N_2,
                0,
                crc,
                payload
            )
        }

    /**
     * 获取同步时间命令
     *
     * @return
     */
    val syncTimeCmd: ByteArray
        get() {
            logSendMsg("4.5 同步时间信息:")
            val currDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
            val timeZone = SimpleTimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
            val currTime =
                TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd HH:mm:ss"))
            val timeStamp = System.currentTimeMillis() / 1000
            val timeSyncBean = WmDateTime(
                timeZone,
                WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR,
                WmUnitInfo.DateFormat.YYYY_MM_DD,
                timeStamp,
                currDate,
                currTime
            )
            val payload = gson.toJson(timeSyncBean).toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8007.toShort(),
                DIVIDE_N_JSON,
                0,
                crc,
                payload
            )
        }

    /**
     * 获取基本信息
     *
     * @return
     */
    val baseInfoCmd: ByteArray
        get() {
            logSendMsg("3.发送基本信息:")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8001.toShort(),
                DIVIDE_N_JSON,
                0,
                0,
                null
            )
        }

    /**
     * 获取APPView list
     *
     * @return
     */
    val appViewList: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8008.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )//1抬腕数据

    /**
     * 预备收集抬腕数据
     *
     * @return
     */
    val debugDataPreState: ByteArray
        get() {
            val payload = ByteArray(1)
            payload[0] = 1 //1抬腕数据
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(
                HEAD_COLLECT_DEBUG_DATA,
                CMD_ID_8001.toShort(),
                DIVIDE_N_2,
                0,
                crc,
                payload
            )
        }

    /**
     * 预备收集抬腕数据
     *
     * @return
     */
    fun getCollectDebugData(page: Byte): ByteArray {
        val payload = ByteArray(1)
        payload[0] = page //1抬腕数据
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
        return constructCmd(
            HEAD_COLLECT_DEBUG_DATA,
            CMD_ID_8002.toShort(),
            DIVIDE_N_2,
            0,
            crc,
            payload
        )
    }

    /**
     * 生成设置Appview的命令
     *
     * @param id
     * @return
     */
    fun setAppViewCmd(id: Byte): ByteArray {
        val payload = ByteArray(1)
        payload[0] = id
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8009.toShort(),
            DIVIDE_N_2,
            0,
            crc,
            payload
        )
    }

    /**
     * 获取电池信息
     *
     * @return
     */
    val batteryInfo: ByteArray
        get() {
            logSendMsg("发送电量信息:")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8003.toShort(),
                DIVIDE_N_JSON,
                0,
                0,
                null
            )
        }

    /**
     * 获取状态信息CMD
     *
     * @return
     */
    val statusCmd: ByteArray
        get() {
            logSendMsg("2.发送状态信息:")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8002.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取同步通知消息
     *
     * @param notifyMsgBean
     * @return
     */
    fun getNotificationCmd(notifyMsgBean: WmNotification?): ByteArray {
        val payload = gson.toJson(notifyMsgBean).toByteArray(StandardCharsets.UTF_8)
        logSendMsg("发送通知消息:" + gson.toJson(notifyMsgBean))
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8004,
            DIVIDE_N_JSON,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 设置闹钟
     *
     * @param alarmBean
     * @return
     */
    fun getSetAlarmCmd(alarmBean: WmAlarm): ByteArray {
        val payload = ByteArray(4)
        //        payload[0] = (byte) alarmBean.open;
//        payload[1] = (byte) alarmBean.hour;
//        payload[2] = (byte) alarmBean.min;
//        payload[3] = (byte) alarmBean.repeat;
        val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
        logSendMsg("闹钟：$alarmBean")
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_801C.toShort(),
            DIVIDE_N_2,
            0,
            crc,
            payload
        )
    }

    /**
     * 获取闹钟
     *
     * @return
     */
    val currAlarmCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_801E.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 获取密码设置状态
     *
     * @return
     */
    val pwdSetCmd: ByteArray
        get() {
            logSendMsg("发送查询密码设置")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_800A.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取时间同步设置状态
     *
     * @return
     */
    fun getTimeSetCmd(mode: Int, open: Int): ByteArray? {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("mode", mode)
            jsonObject.put("open", open)
            val json = jsonObject.toString()
            logSendMsg("4.5发送查询时间同步设置：$json")
            val payload =
                json.toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_800C.toShort(),
                DIVIDE_N_JSON,
                0,
                crc,
                payload
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取一次设置多个初始化信息的命令
     *
     * @return
     */
    fun getSetAllSportInitCmd(initInfo: WmSportGoal): ByteArray {
        logSendMsg("一次设置多个初始化信息:")
        val jsonObject = JSONObject()
        return try {
            jsonObject.put("heat_goal", initInfo.calories)
            jsonObject.put("step_goal", initInfo.steps)
            jsonObject.put("dis_goal", initInfo.distance)
            val json = jsonObject.toString()
            val payload =
                json.toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_8005.toShort(),
                DIVIDE_N_JSON,
                0,
                crc,
                payload
            )
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 获取设置密码
     *
     * @param msgSetType
     * @param password
     * @return
     */
    fun getSetPwdCmd(password: String?, msgSetType: Int): ByteArray {
        return try {
            val jsonObject = JSONObject()
            jsonObject.put("type", msgSetType)
            jsonObject.put("pwd", password)
            val json = jsonObject.toString()
            LogUtils.logCommon("设置密码：$json")
            val payload =
                json.toByteArray(StandardCharsets.UTF_8)
            val crc = BtUtils.getCrc(HEX_FFFF, payload, payload.size)
            constructCmd(
                HEAD_COMMON,
                CMD_ID_800B.toShort(),
                DIVIDE_N_JSON,
                0,
                crc,
                payload
            )
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 同步绑定状态
     *
     * @return
     */
    fun getBindStateCmd(state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_800D.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    val createBondCmd: ByteArray
        get() {
            val byteBuffer = ByteBuffer.allocate(1)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.put(1.toByte())
            byteBuffer.flip()
            val payload = byteBuffer.array()
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8012.toShort(),
                DIVIDE_N_2,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )
        }

    /**
     * 获取取消绑定协议CMD
     *
     * @return
     */
    val cancelBindRequestCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8011.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 获取解绑协议CMD
     *
     * @return
     */
    val unBindRequestCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_800E.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 获取DialList CMD
     *
     * @return
     */
    fun getDialListCmd(order: Byte): ByteArray {
        logSendMsg("9.从设备端获取我的表盘数据")
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(order)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_800F.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取DialList CMD
     *
     * @return
     */
    fun getDialStateCmd(id: String): ByteArray {
        val idArray = id.toByteArray()
        val byteBuffer = ByteBuffer.allocate(idArray.size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(idArray)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8014.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 发送文件请求
     *
     * @return
     */
    fun getOppFileTransferCmd(file_type: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        //        byteBuffer.put(trans_type);
        byteBuffer.put(file_type)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_OPP,
            CMD_ID_8001.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 是否维持OPP
     *
     * @return
     */
    fun getOppContinueCmd(opp: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(opp)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_OPP,
            CMD_ID_8003.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取停止传输OPP
     *
     * @return
     */
    fun getOppFinishFileTransferCmd(type: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_OPP,
            CMD_ID_8002.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 操作表盘 0x0 type:1设定 2删除
     *
     * @param type
     * @param dialId
     * @return
     */
    fun getDialActionCmd(type: Byte, dialId: String): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1 + dialId.toByteArray().size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.put(dialId.toByteArray())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8010.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取81命令
     *
     * @param type 1:音频文件 2:OTA（根据实际需求定义） 3:BIN
     * @return
     */
    fun getTransferFile01Cmd(type: Byte, fileLen: Int, fileCount: Int): ByteArray {
        logSendMsg("文件长度:$fileLen")
        val byteBuffer = ByteBuffer.allocate(1 + 4 + 1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.putInt(fileLen)
        byteBuffer.put(fileCount.toByte())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_A_2_D,
            CMD_ID_8001.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 文件长度和名称上报
     *
     * @param len  文件长度
     * @param name 文件名称
     * @return
     */
    fun getTransferFile02Cmd(len: Int, name: String): ByteArray {
        val nameByte = name.toByteArray(Charset.defaultCharset())
        val byteBuffer = ByteBuffer.allocate(4 + nameByte.size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(len)
        byteBuffer.put(nameByte)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_A_2_D,
            CMD_ID_8002.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取83命令
     *
     * @param otaCmdInfo
     * @return
     */
    fun getTransfer03Cmd(
        process: Int,
        otaCmdInfo: OtaCmdInfo,
        divideType: Byte
    ): ByteArray {
        val byteBuffer =
            ByteBuffer.allocate(otaCmdInfo.payload.size + 4)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(process)
        byteBuffer.put(otaCmdInfo.payload)
        byteBuffer.flip()
        otaCmdInfo.payload = byteBuffer.array()
        otaCmdInfo.crc =
            BtUtils.getCrc(HEX_FFFF, otaCmdInfo.payload, otaCmdInfo.payload.size)
        logSendMsg("发送消息序号：" + process + " 包长:" + otaCmdInfo.payload.size)
        return constructCmd(
            HEAD_FILE_SPP_A_2_D, CMD_ID_8003.toShort(),
            divideType, otaCmdInfo.offSet, otaCmdInfo.crc, otaCmdInfo.payload
        )
    }

    /**
     * 获取83命令 确认是否成功
     *
     * @return
     */
    val transfer04Cmd: ByteArray
        get() = constructCmd(
            HEAD_FILE_SPP_A_2_D, CMD_ID_8004.toShort(),
            DIVIDE_N_2, 0, 0, null
        )

    /**
     * App取消文件传输
     *
     * @return
     */
    val transferCancelCmd: ByteArray
        get() = constructCmd(
            HEAD_FILE_SPP_A_2_D, CMD_ID_8005.toShort(),
            DIVIDE_N_2, 0, 0, null
        )

    /**
     * 从设备端向App端传数据
     *
     * @param type
     * @return
     */
    fun getTransferD2A01Cmd(type: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1 + 4 + 1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_D_2_A,
            CMD_ID_8001.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 文件长度和名称上报
     *
     * @param index 文件序号
     * @return
     */
    fun getTransferD2A02Cmd(index: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.putInt(index.toInt())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_D_2_A,
            CMD_ID_8002.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 83命令
     *
     * @param state   状态
     * @param process 序号
     * @return
     */
    fun getTransferD2A03Cmd(state: Byte, process: Int): ByteArray {
        val byteBuffer = ByteBuffer.allocate(5)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.putInt(process)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        logSendMsg("发送消息序号：$process 成功:$state")
        return constructCmd(
            HEAD_FILE_SPP_D_2_A,
            CMD_ID_8003.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 返回传输结果
     *
     * @return
     */
    fun getTransferD2A04Cmd(result: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(result)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_FILE_SPP_D_2_A,
            CMD_ID_8004.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * App端取消接收数据
     *
     * @return
     */
    fun getTransferD2A05Cmd(result: Byte): ByteArray {
        return constructCmd(
            HEAD_FILE_SPP_D_2_A,
            CMD_ID_8005.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )
    }

    /**
     * 获取添加初始化信息的命令
     *
     * @param type  1. 热量(千卡)
     * 2. 距离(千米)
     * 3. 步数(步)
     * 4. 生日(19960605)
     * 5. 身高（厘米）
     * 6. 体重(千克)
     * 7. 性别：1.男 2.女
     * @param value 整数值
     * @return
     */
    fun initSportHealthCmd(type: Byte, value: Int): ByteArray {
        logSendMsg("type:$type value:$value")
        val byteBuffer = ByteBuffer.allocate(5)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.putInt(value)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_SPORT_HEALTH,
            CMD_ID_8004.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取初始化信息
     *
     * @return
     */
    val sportHealthInitInfoCmd: ByteArray
        get() = constructCmd(
            HEAD_SPORT_HEALTH,
            CMD_ID_8001.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 获取运动记录
     *
     * @return
     */
    val stepRecordCmd: ByteArray
        get() {
            logSendMsg("7.获取运动步数：")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_8002.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取心率记录
     *
     * @return
     */
    val rateRecordCmd: ByteArray
        get() {
            logSendMsg("8.获取心率：")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_8003.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取睡眠记录
     *
     * @return
     */
    fun getSleepRecordCmd(index: Byte): ByteArray {
        logSendMsg("12.获取睡眠")
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(index)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_SPORT_HEALTH,
            CMD_ID_800F.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取睡眠记录
     *
     * @return
     */
    val getSleepSetCmd: ByteArray
        get() {
            logSendMsg("获取睡眠区间设置")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_800C,
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取睡眠记录
     *
     * @return
     */
    fun setSleepSetCmd(open: Byte, startH: Byte, startM: Byte, endH: Byte, endM: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(5)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(open)
        byteBuffer.put(startH)
        byteBuffer.put(startM)
        byteBuffer.put(endH)
        byteBuffer.put(endM)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_SPORT_HEALTH,
            CMD_ID_800E.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 获取血氧记录
     *
     * @return
     */
    val bloodOxRecordCmd: ByteArray
        get() {
            logSendMsg("9.获取血氧")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_8009.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取血糖记录
     *
     * @return
     */
    val bloodSugarRecordCmd: ByteArray
        get() {
            logSendMsg("10.获取血糖")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_800A.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取血压记录
     *
     * @return
     */
    val bloodPressRecordCmd: ByteArray
        get() {
            logSendMsg("11.获取血压")
            return constructCmd(
                HEAD_SPORT_HEALTH,
                CMD_ID_800B.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取当前支持的功能
     *
     * @return
     */
    val actionSupportCmd: ByteArray
        get() {
            logSendMsg("获取支持功能列表")
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_802D.toShort(),
                DIVIDE_N_2,
                0,
                0,
                null
            )
        }

    /**
     * 获取运动健康有数据的日期
     *
     * @return
     */
    fun getRecordDates(type: Int): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type.toByte())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_SPORT_HEALTH,
            CMD_ID_8006.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * App修改来电响铃
     *
     *
     * 0: 响铃
     * 1: 震动反馈
     * 2: 抬腕亮屏
     *
     * @param state
     * @return
     */
    fun getSetDeviceRingStateCmd(type: Byte, state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(type)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8018.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 设备端修改来电响铃
     *
     * @return
     */
    val deviceRingStateCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8017.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 设备端修改来电响铃
     *
     * @return
     */
    val deviceRingStateRespondCmd: ByteArray
        get() {
            val byteBuffer = ByteBuffer.allocate(1)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.put(1.toByte())
            byteBuffer.flip()
            val payload = byteBuffer.array()
            return constructCmd(
                HEAD_COMMON,
                CMD_ID_8019.toShort(),
                DIVIDE_N_2,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )
        }

    /**
     * App发起寻找手表
     *
     * @return
     */
    val searchDeviceCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8021.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 当设备添加通讯录的时候需要给回应，固定是成功
     * CMD_ID_8024/CMD_ID_8026/CMD_ID_8020
     *
     * @return
     */
    fun getRespondSuccessCmd(cmdId: Short): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(1.toByte())
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            cmdId,
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 读取通讯录准备命令
     *
     * @return
     */
    val contactPreCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_8027.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 获取通讯录
     *
     * @param index
     * @return
     */
    fun getContactListCmd(index: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(index)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8022.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 添加联系人通讯录
     *
     * @return
     */
    fun getAddContactCmd(name: String, phoneNum: String): ByteArray {
        val nameArray = ByteArray(60)
        val phoneArray = ByteArray(20)
        val byteBuffer = ByteBuffer.allocate(nameArray.size + phoneArray.size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val srcNameArray = name.toByteArray(Charset.defaultCharset())
        System.arraycopy(
            srcNameArray,
            0,
            nameArray,
            0,
            if (srcNameArray.size > nameArray.size) nameArray.size else srcNameArray.size
        )
        val srcPhoneArray =
            phoneNum.replace("\\s".toRegex(), "").toByteArray(Charset.defaultCharset())
        System.arraycopy(
            srcPhoneArray,
            0,
            phoneArray,
            0,
            if (srcPhoneArray.size > phoneArray.size) phoneArray.size else srcPhoneArray.size
        )

//        SJLog.INSTANCE.logSendMsg("添加的联系人：" + new String(nameArray));
//        SJLog.INSTANCE.logSendMsg("添加的联系人长度：" + nameArray.length);
//        SJLog.INSTANCE.logSendMsg("添加的电话号：" + new String(phoneArray));
//        SJLog.INSTANCE.logSendMsg("添加的电话号长度：" + phoneArray.length);
        byteBuffer.put(nameArray)
        byteBuffer.put(phoneArray)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8023.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 删除通讯录命令
     *
     * @return
     */
    fun getDeleteContactCmd(name: String, phoneNum: String): ByteArray {
        val nameArray = ByteArray(60)
        val phoneArray = ByteArray(20)
        val byteBuffer = ByteBuffer.allocate(nameArray.size + phoneArray.size)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val srcNameArray = name.toByteArray(Charset.defaultCharset())
        System.arraycopy(
            srcNameArray,
            0,
            nameArray,
            0,
            if (srcNameArray.size > nameArray.size) nameArray.size else srcNameArray.size
        )
        val srcPhoneArray = phoneNum.toByteArray(Charset.defaultCharset())
        System.arraycopy(
            srcPhoneArray,
            0,
            phoneArray,
            0,
            if (srcPhoneArray.size > phoneArray.size) phoneArray.size else srcPhoneArray.size
        )
        byteBuffer.put(nameArray)
        byteBuffer.put(phoneArray)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_8025.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 读取通讯录准备命令
     *
     * @return
     */
    val stopRingCmd: ByteArray
        get() = constructCmd(
            HEAD_COMMON,
            CMD_ID_801A.toShort(),
            DIVIDE_N_2,
            0,
            0,
            null
        )

    /**
     * 摄氏度 = (华氏度 - 32°F) ÷ 1.8；华氏度 = 32°F+ 摄氏度 × 1.8
     *
     * @param weatherBean
     * @return
     */
    fun getWeatherListCmd(weatherBean: WmWeather): ByteArray {
        val cityArray = ByteArray(20)
        val byteBuffer =
            ByteBuffer.allocate(cityArray.size + 4 + weatherBean.weatherForecast.size * 5)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val cityName = weatherBean.location.city
        val srcNameArray = cityName.toByteArray(Charset.defaultCharset())
        System.arraycopy(
            srcNameArray,
            0,
            cityArray,
            0,
            if (srcNameArray.size > cityArray.size) cityArray.size else srcNameArray.size
        )
        val date = getFormatTime(weatherBean.pubDate)
        byteBuffer.put(cityArray)
        byteBuffer.putInt(date)
        for ((lowTemp, highTemp, _, _, _, _, dayCode, _, _, _, date1) in weatherBean.weatherForecast) {
            //week  周几1~7
//            byte week = getWeek(forecastsBean.getDay());
            val week = getWeekByTimeStamp(date1)
            //            SJLog.INSTANCE.logSendMsg("week2:" + week2 + " - week:" + week);
            byteBuffer.put(week)
            //天气大类型 1：晴； 2：多云；3：雨；4：雪；5：沙尘
            val weatherBigCode = getWeatherBigCode(dayCode)
            byteBuffer.put(weatherBigCode)
            //天气名称暂时不用
            //天气代码
            byteBuffer.put(dayCode.toByte())
            //高温
//            byteBuffer.put((byte) ((forecastsBean.getHigh() - 32) / 1.8));
            byteBuffer.put(highTemp.toByte())
            //低温
            byteBuffer.put(lowTemp.toByte())
            //            byteBuffer.put((byte) ((forecastsBean.getLow() - 32) / 1.8));
        }
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_801B.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 回应相机
     *
     * @param state
     * @return
     */
    fun getCameraRespondCmd(cmdId: Short, state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            cmdId,
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 相机设置
     *
     * @param state
     * @return
     */
    fun getCameraStateActionCmd(action: Byte, state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(action)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802C.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * App拉起设备相机
     *
     * @param state
     * @return
     */
    fun getAppCallDeviceCmd(state: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(state)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_COMMON,
            CMD_ID_802A.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    private fun getFormatTime(timeMillions: Long): Int {
        val simpleDateFormat = SimpleDateFormat("yyMMdd")
        val date = Date(timeMillions)
        val res = simpleDateFormat.format(date)
        logSendMsg("转换后的时间：$res")
        return res.toInt()
    }

    private val weatherCodeClear = intArrayOf(0, 1, 2, 3, 38)
    private val weatherCodeCloud = intArrayOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)
    private val weatherCodeRain = intArrayOf(20, 21, 22, 23, 24, 25, 37)
    private val weatherCodeSnow = intArrayOf(26, 27, 28, 29, 30, 31)
    private val weatherCodeSand = intArrayOf(4, 5, 6, 7, 8, 9, 32, 33, 34, 35, 36)

    /**
     * 1：晴； 2：多云；3：雨；4：雪；5：沙尘  6未知
     *
     * @param weatherCode
     * @return
     */
    private fun getWeatherBigCode(weatherCode: Int): Byte {
        var weatherBigCode: Byte = 99
        for (code in weatherCodeClear) {
            if (code == weatherCode) {
                weatherBigCode = 1
                return weatherBigCode
            }
        }
        for (code in weatherCodeCloud) {
            if (code == weatherCode) {
                weatherBigCode = 2
                return weatherBigCode
            }
        }
        for (code in weatherCodeRain) {
            if (code == weatherCode) {
                weatherBigCode = 3
                return weatherBigCode
            }
        }
        for (code in weatherCodeSnow) {
            if (code == weatherCode) {
                weatherBigCode = 4
                return weatherBigCode
            }
        }
        for (code in weatherCodeSand) {
            if (code == weatherCode) {
                weatherBigCode = 5
                return weatherBigCode
            }
        }
        return weatherBigCode
    }

    private fun getWeekByTimeStamp(weekTime: Long): Byte {
        val week = TimeUtils.getUSWeek(weekTime)

//        SJLog.INSTANCE.logSendMsg("星期：" + week);
        if (week.contains("Sun")) {
            return 7
        } else if (week.contains("Mon")) {
            return 1
        } else if (week.contains("Tue")) {
            return 2
        } else if (week.contains("Wed")) {
            return 3
        } else if (week.contains("Thu")) {
            return 4
        } else if (week.contains("Fri")) {
            return 5
        } else if (week.contains("Sat")) {
            return 6
        } else if (week.contains("Sun")) {
            return 7
        }
        return 0
    }

    /**
     * 相机预览界面开始
     *
     * @return
     */
    fun getCameraPreviewCmd01(dataType: Byte): ByteArray {
        val byteBuffer = ByteBuffer.allocate(1)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        byteBuffer.put(dataType)
        byteBuffer.flip()
        val payload = byteBuffer.array()
        return constructCmd(
            HEAD_CAMERA_PREVIEW,
            CMD_ID_8001.toShort(),
            DIVIDE_N_2,
            0,
            BtUtils.getCrc(HEX_FFFF, payload, payload.size),
            payload
        )
    }

    /**
     * 相机预览界面数据发送
     *
     * @return
     */
    fun getCameraPreviewDataCmd02(data: ByteArray, divideType: Byte): ByteArray {
        return constructCmd(
            HEAD_CAMERA_PREVIEW,
            CMD_ID_8002.toShort(),
            divideType,
            0,
            BtUtils.getCrc(
                HEX_FFFF,
                data,
                data.size
            ),
            data
        )
    }

    fun sizeOfNumber(variable: Any): Int {
        // Long.SIZE_BYTES = 8
        return ByteBuffer.allocate(Long.SIZE_BYTES)
            .let { buffer ->
                when (variable) {
                    is Byte -> buffer.put(variable)
                    is Short -> buffer.putShort(variable)
                    is Int -> buffer.putInt(variable)
                    is Long -> buffer.putLong(variable)
                    is Float -> buffer.putFloat(variable)
                    is Double -> buffer.putDouble(variable)
                    else -> throw IllegalArgumentException("unsupported data type: ${variable::class.simpleName}")
                }
                buffer.position(0).limit()
            }
    }

    fun getUrnId(
        parentUrn: Byte,
        childUrn: Byte = URN_0,
        grandSon: Byte = URN_0,
        grandgrandSon: Byte = URN_0
    ): ByteArray {
        return byteArrayOf(
            parentUrn,
            childUrn,
            grandSon,
            grandgrandSon,
        )
    }

    /**
     * 获取设置体育目标的命令
     */
    fun getUpdateSportGoalAllCmd(
        sportGoal: WmSportGoal
    ): PayloadPackage {

        val payloadPackage = PayloadPackage()

        if (sportGoal.steps != 0 || sportGoal.calories != 0 || sportGoal.distance != 0 || sportGoal.activityDuration.toInt() != 0) {
            val bbSport: ByteBuffer = ByteBuffer.allocate(4 + 4 + 4 + 2)
            bbSport.putInt(sportGoal.steps)
            bbSport.putInt(sportGoal.calories)
            bbSport.putInt(sportGoal.distance)
            bbSport.putShort(sportGoal.activityDuration)
            payloadPackage.putData(getUrnId(URN_2, URN_1), bbSport.array())
        } else {
            if (sportGoal.steps != 0) {
                payloadPackage.putData(
                    getUrnId(URN_2, URN_1, URN_1),
                    ByteBuffer.allocate(sizeOfNumber(sportGoal.steps)).putInt(sportGoal.steps)
                        .array()
                )
            } else if (sportGoal.calories != 0) {
                payloadPackage.putData(
                    getUrnId(URN_2, URN_1, URN_2),
                    ByteBuffer.allocate(sizeOfNumber(sportGoal.calories)).putInt(sportGoal.calories)
                        .array()
                )
            } else if (sportGoal.distance != 0) {
                payloadPackage.putData(
                    getUrnId(URN_2, URN_1, URN_3),
                    ByteBuffer.allocate(sizeOfNumber(sportGoal.distance)).putInt(sportGoal.distance)
                        .array()
                )
            } else if (sportGoal.activityDuration.toInt() != 0) {
                payloadPackage.putData(
                    getUrnId(URN_2, URN_1, URN_4),
                    ByteBuffer.allocate(sizeOfNumber(sportGoal.activityDuration))
                        .putShort(sportGoal.activityDuration).array()
                )
            }
        }

        return payloadPackage
    }

}