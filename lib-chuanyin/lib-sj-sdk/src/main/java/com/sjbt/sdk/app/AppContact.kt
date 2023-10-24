package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.port.app.AbAppContact
import com.sjbt.sdk.CONTACT_NAME_LEN
import com.sjbt.sdk.CONTACT_NUM_LEN
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class AppContact(val sjUniWatch: SJUniWatch) : AbAppContact() {
    private var contactListEmitter: ObservableEmitter<List<WmContact>>? = null
    private var updateContactEmitter: SingleEmitter<Boolean>? = null
    private var contactCountSetEmitter: SingleEmitter<Boolean>? = null
    private var updateEmergencyEmitter: SingleEmitter<WmEmergencyCall>? = null
    private var emergencyNumberEmitter: ObservableEmitter<WmEmergencyCall>? = null

    private var mEmergencyCall: WmEmergencyCall = WmEmergencyCall(false, mutableListOf())
    private val mContacts = mutableListOf<WmContact>()

    private val TAG = "AppContact"
    override fun isSupport(): Boolean {
        return true
    }

    override fun setContactCount(count: Int): Single<Boolean> {
        return Single.create {
            contactCountSetEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getReadContactCountCmd(count.toByte()))
        }
    }

    override var observableContactList: Observable<List<WmContact>> = Observable.create {
        mContacts.clear()
        contactListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadContactListCmd())
    }

    override fun updateContactList(contactList: List<WmContact>): Single<Boolean> = Single.create {
        updateContactEmitter = it

        sjUniWatch.observableMtu.subscribe { mtu ->

            val payloadPackage = CmdHelper.getWriteContactListCmd(contactList)

            sendWriteSubpackageNodeCmdList(
                (contactList.size * (CONTACT_NAME_LEN + CONTACT_NUM_LEN) + 10).toShort(),//通讯录个数长度+payload头长度
                mtu,
                payloadPackage
            )
        }
    }

    private fun updateContactListBack(success: Boolean) {
        updateContactEmitter?.onSuccess(success)
    }

    override fun observableEmergencyContacts(): Observable<WmEmergencyCall> = Observable.create {
        emergencyNumberEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadEmergencyNumberCmd())
    }

    override fun updateEmergencyContact(emergencyCall: WmEmergencyCall): Single<WmEmergencyCall> =
        Single.create {
            mEmergencyCall = emergencyCall
            updateEmergencyEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteEmergencyNumberCmd(emergencyCall))
        }

    private fun getEmergencyContactBack(emergencyCall: WmEmergencyCall) {
        mEmergencyCall = emergencyCall
        emergencyNumberEmitter?.onNext(emergencyCall)
    }

    private fun updateEmergencyContactBack(success: Boolean) {
        updateEmergencyEmitter?.onSuccess(mEmergencyCall)
    }

    private val businessMap = LinkedHashMap<Int, LinkedHashMap<Int, MsgBean>>()

    //    private val msgPkMap = LinkedHashMap<Int, MsgBean>()
    private var contactIndex = 0
    private var packageIndex = 0

    /**
     * 分包发送写入类型Node节点消息
     */

    fun sendWriteSubpackageNodeCmdList(
        totalLen: Short, mtu: Int, payloadPackage: PayloadPackage
    ) {
        /**
         * 返回业务单元list
         */
        val packList = payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_WRITE)
        var divideType = DIVIDE_N_2
        businessMap.clear()

        for (k in 0 until packList.size) {

            val it = packList[k]

            sjUniWatch.wmLog.logE(TAG, "业务分包数据：" + BtUtils.bytesToHexString(it))

            //每一个单元再做数据分包
            var count = it.size / mtu
            if (it.size % mtu != 0) {
                count.plus(1)
            }

            val msgPkMap = LinkedHashMap<Int, MsgBean>()

            for (i in 0 until count) {
                //传输层分包
                var payload: ByteArray = it.copyOfRange(i * mtu, i * mtu + mtu)

                if (i == 0) {
                    divideType = DIVIDE_Y_F_2
                } else if (i == count - 1) {
                    divideType = DIVIDE_Y_E_2
                } else {
                    divideType = DIVIDE_Y_M_2
                }

                sjUniWatch.wmLog.logE(TAG, "DIVIDE TYPE:$divideType - i:$i")

                val cmdArray = CmdHelper.constructCmd(
                    HEAD_NODE_TYPE,
                    CMD_ID_8001,
                    divideType,
                    totalLen,
                    0,
                    BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                    payload
                )

                val msgBean = CmdHelper.getPayLoadJson(false, cmdArray)

                msgPkMap.put(i, msgBean)
            }

            businessMap.put(k, msgPkMap)
        }

        businessMap.get(0)?.let {
            it.get(0)?.let { it1 -> sjUniWatch.sendNormalMsg(it1.originData) }

            Thread.sleep(100)
            it.get(1)?.let { it1 -> sjUniWatch.sendNormalMsg(it1.originData) }

            Thread.sleep(100)
            it.get(2)?.let { it1 -> sjUniWatch.sendNormalMsg(it1.originData) }
        }
    }

    fun contactBusiness(
        payload: PayloadPackage,
        it: NodeData,
        msgBean: MsgBean?
    ) {
        when (it.urn[2]) {

            URN_APP_CONTACT_COUNT -> {
                contactCountSetEmitter?.onSuccess(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }

            URN_APP_CONTACT_LIST -> {

                if (it.data.size == 1) {
                    updateContactListBack(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)

                } else {
                    if (payload.packageSeq == 0) {
                        mContacts.clear()
                    }

                    val byteArray =
                        ByteBuffer.wrap(it.data).array()

                    val chunkSize = CONTACT_NAME_LEN + CONTACT_NUM_LEN

                    if (it.dataLen.toInt() > chunkSize) {
                        var i = 0
                        while (i < byteArray.size) {
                            val nameBytes = byteArray.copyOfRange(i, i + CONTACT_NAME_LEN)
                            val numBytes = byteArray.copyOfRange(i + CONTACT_NUM_LEN, i + chunkSize)
                            val name = String(nameBytes).trim()
                            val num = String(numBytes).trim()
                            val contact = WmContact.create(name, num)
                            sjUniWatch.wmLog.logE(TAG, "contact:" + contact)
                            contact?.let {
                                mContacts.add(it)
                            }
                            i += chunkSize
                        }
                    }

                    msgBean?.let {
                        if (!payload.hasNext()) {
                            contactListEmitter?.onNext(mContacts)
                            contactListEmitter?.onComplete()
                        }
                    }
                }

            }

            URN_APP_CONTACT_EMERGENCY -> {
                if (it.data.size == 1) {
                    updateEmergencyContactBack(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                } else {
                    if (it.dataLen >= CONTACT_NAME_LEN + CONTACT_NUM_LEN + 1) {
                        val emergencyByteArray = it.data
                        val enable = it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                        mEmergencyCall.isEnabled = enable
                        mEmergencyCall.emergencyContacts.clear()
                        val name = String(
                            emergencyByteArray.copyOf(CONTACT_NAME_LEN),
                            StandardCharsets.UTF_8
                        )

                        val num = String(
                            emergencyByteArray.copyOfRange(
                                CONTACT_NAME_LEN,
                                CONTACT_NAME_LEN + CONTACT_NUM_LEN + 1
                            ),
                            StandardCharsets.UTF_8
                        )

                        WmContact.create(name, num)?.let {
                            mEmergencyCall.emergencyContacts.add(it)
                        }

                        getEmergencyContactBack(mEmergencyCall)
                    } else {
                        getEmergencyContactBack(mEmergencyCall)
                    }
                }
            }
        }
    }
}