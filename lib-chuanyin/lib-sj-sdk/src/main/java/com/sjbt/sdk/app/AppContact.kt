package com.sjbt.sdk.app

import android.text.TextUtils
import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.apps.WmContact.Companion.NAME_BYTES_LIMIT
import com.base.sdk.entity.apps.WmContact.Companion.NUMBER_BYTES_LIMIT
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.port.app.AbAppContact
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.spp.cmd.CmdHelper.MAX_ORDER_ID
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.Disposable
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class AppContact(val sjUniWatch: SJUniWatch) : AbAppContact() {
    private var contactListEmitter: ObservableEmitter<List<WmContact>>? = null
    private var updateContactEmitter: SingleEmitter<Boolean>? = null

    //    private var contactCountSetEmitter: SingleEmitter<Boolean>? = null
    private var updateEmergencyEmitter: SingleEmitter<WmEmergencyCall>? = null
    private var emergencyNumberEmitter: ObservableEmitter<WmEmergencyCall>? = null

    private var mEmergencyCall: WmEmergencyCall = WmEmergencyCall(false, mutableListOf())
    private val mContacts = mutableListOf<WmContact>()

    private val msgList = mutableSetOf<MsgBean>()

    private var hasNext = false

    private val TAG = "AppContact"
    override fun isSupport(): Boolean {
        return true
    }

    fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    fun getHasNext(): Boolean {
        return hasNext
    }

//    override fun setContactCount(count: Int): Single<Boolean> {
//        return Single.create {
//            contactCountSetEmitter = it
//            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getReadContactCountCmd(count.toByte()))
//        }
//    }

    override var getContactList: Observable<List<WmContact>> = Observable.create {
        mContacts.clear()
        contactListEmitter = it
        msgList.clear()

        sjUniWatch.sendReadSubPkObserveNode(CmdHelper.getReadContactListCmd())
            .subscribe(object : Observer<MsgBean> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: MsgBean) {
                    msgList.add(t)
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {
                    var byteBuffer = ByteBuffer.allocate(MAX_BUSINESS_BUFFER_SIZE * 10)

                    msgList.forEachIndexed { index, msgBean ->
                        byteBuffer.put(msgBean.payload)
                    }

                    val chunkSize = NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT

                    var i = 17
                    while ((i + chunkSize) < byteBuffer.array().size) {
                        val nameBytes = byteBuffer.array().copyOfRange(i, i + NAME_BYTES_LIMIT)
                            .takeWhile { it.toInt() != 0 }.toByteArray()
                        val numBytes =
                            byteBuffer.array().copyOfRange(i + NUMBER_BYTES_LIMIT, i + chunkSize)

                        val name = String(nameBytes, StandardCharsets.UTF_8)
                        val num = String(numBytes, StandardCharsets.UTF_8)

                        sjUniWatch.wmLog.logE(TAG, "name:" + name + " num:" + num)

                        if (!TextUtils.isEmpty(name)) {
                            val contact = WmContact.create(name, num)

                            mContacts.add(contact!!)
                            i += chunkSize
                        } else {
                            break
                        }
                    }

                    contactListEmitter?.onNext(mContacts)
                    contactListEmitter?.onComplete()
                }

            })
    }

    override fun updateContactList(contactList: List<WmContact>): Single<Boolean> = Single.create {
        updateContactEmitter = it

        sjUniWatch.observableMtu.subscribe { mtu ->

            val payloadPackage = CmdHelper.getWriteContactListCmd(contactList)

            sendWriteSubpackageNodeCmdList(
                mtu,
                payloadPackage
            )
        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {

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

    private fun updateEmergencyContactBack(success: Boolean) {
        updateEmergencyEmitter?.onSuccess(mEmergencyCall)
    }

    //    private val businessMap: LinkedHashMap<Int, LinkedHashMap<Int, MsgBean>> =
//        linkedMapOf<Int, LinkedHashMap<Int, MsgBean>>()
    private val msgPkMap = LinkedHashMap<Int, MsgBean>()

    /**
     * 分包发送写入类型Node节点消息
     */

    private var firstPkOrder = 0
    fun sendWriteSubpackageNodeCmdList(
        mtu: Int, payloadPackage: PayloadPackage
    ) {
        /**
         * 返回业务单元list
         */
        val businessList = payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_WRITE)
        var divideType = DIVIDE_N_2
//        businessMap.clear()
        msgPkMap.clear()

        for (k in 0 until businessList.size) {

            val businessArray = businessList[k]

            //每一个单元再做数据分包
            var count = businessArray.size / mtu
            var lastCount = businessArray.size % mtu
            if (lastCount != 0) {
                count += 1
            }

            for (i in 0 until count) {
                //传输层分包
                var payload: ByteArray? = null

                if(count == 1){
                    payload = businessArray.copyOfRange(i * mtu, i * mtu + lastCount)
                    divideType = DIVIDE_N_2
                } else if (i == count - 1) {
                    payload = businessArray.copyOfRange(i * mtu, i * mtu + lastCount)
                    divideType = DIVIDE_Y_E_2
                } else {
                    payload = businessArray.copyOfRange(i * mtu, i * mtu + mtu)
                    if (i == 0) {
                        divideType = DIVIDE_Y_F_2
                    } else {
                        divideType = DIVIDE_Y_M_2
                    }
                }

                val cmdArray = CmdHelper.constructCmd(
                    HEAD_NODE_TYPE,
                    CMD_ID_8001,
                    divideType,
                    businessArray.size.toShort(),
                    0,
                    BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                    payload
                )

                val msgBean = CmdHelper.getPayLoadJson(false, cmdArray)

                val order = msgBean.cmdOrder
                msgPkMap[order] = msgBean

                if (k == 0 && i == 0) {
                    firstPkOrder = order
                    sjUniWatch.wmLog.logE(TAG, "first Order Id：$firstPkOrder")
                }
            }
        }

        sendObserveNode(firstPkOrder)
    }

    private fun sendObserveNode(order: Int) {
        sjUniWatch.wmLog.logE(TAG, "total left ${msgPkMap.keys} send next order:$order")
        msgPkMap[order]?.let { msgBean ->
            sjUniWatch.wmLog.logE(TAG, "divideType： ${msgBean.divideType}  order:$order")
            sjUniWatch.sendAndObserveNode04(msgBean.originData).subscribe { order ->
                sjUniWatch.wmLog.logE(TAG, "success order id：$order")
                msgPkMap.remove(order)

                if (order != 0) {
                    if (order == MAX_ORDER_ID - 1) {
                        sendObserveNode(0)
                    } else {
                        sendObserveNode(order % MAX_ORDER_ID + 1)
                    }
                } else {
                    sendObserveNode(order % MAX_ORDER_ID + 1)
                }

            }
        }
    }

    fun contactBusiness(
        payload: PayloadPackage,
        it: NodeData,
        msgBean: MsgBean
    ) {
        when (it.urn[2]) {

            URN_APP_CONTACT_COUNT -> {
//                contactCountSetEmitter?.onSuccess(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
            }

            URN_APP_CONTACT_LIST -> {

                if (it.data.size == 1) {
                    updateContactListBack(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)

                } else {
                    if (payload.packageSeq == 0) {
                        mContacts.clear()
                    }

                    if (msgBean.divideType == DIVIDE_N_2) {
                        val byteArray =
                            ByteBuffer.wrap(it.data).array()

                        val chunkSize = NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT

                        if (it.dataLen.toInt() > chunkSize) {
                            var i = 0
                            while (i < byteArray.size) {

                                val nameBytes = byteArray.copyOfRange(i, i + NAME_BYTES_LIMIT)
                                    .takeWhile { it.toInt() != 0 }.toByteArray()
                                val numBytes =
                                    byteArray.copyOfRange(i + NUMBER_BYTES_LIMIT, i + chunkSize)

                                val name = String(nameBytes, StandardCharsets.UTF_8)
                                val num = String(numBytes, StandardCharsets.UTF_8)

                                if (!TextUtils.isEmpty(name)) {
                                    val contact = WmContact.create(name, num)
                                    mContacts.add(contact!!)
                                }

                                i += chunkSize
                            }
                        }

                        contactListEmitter?.onNext(mContacts)
                        contactListEmitter?.onComplete()
                    }
                }
            }

            URN_APP_CONTACT_EMERGENCY -> {

                sjUniWatch.wmLog.logD(TAG, "emergency contact msg!")

                if (it.data.size == 1) {
                    updateEmergencyContactBack(it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal)
                } else {
                    if (it.dataLen >= NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT + 1) {
                        val emergencyByteArray = it.data
                        val enable = it.data[0].toInt() == 1
                        mEmergencyCall.isEnabled = enable
                        mEmergencyCall.emergencyContacts.clear()
                        val name = String(
                            emergencyByteArray.copyOfRange(1, NAME_BYTES_LIMIT + 1),
                            StandardCharsets.UTF_8
                        )

                        val num = String(
                            emergencyByteArray.copyOfRange(
                                NAME_BYTES_LIMIT,
                                NAME_BYTES_LIMIT + NUMBER_BYTES_LIMIT + 1
                            ),
                            StandardCharsets.UTF_8
                        )

                        if (!TextUtils.isEmpty(name)) {
                            WmContact.create(name, num)?.let {
                                mEmergencyCall.emergencyContacts.add(it)
                            }
                            sjUniWatch.wmLog.logD(TAG, "emergency contact:$mEmergencyCall")
                        } else {
                            mEmergencyCall.isEnabled = false
                        }
                    }

                    emergencyNumberEmitter?.onNext(mEmergencyCall)
                }
            }
        }
    }
}