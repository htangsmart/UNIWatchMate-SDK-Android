package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.port.app.AbAppContact
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
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
    private var mEmergencyCall: WmEmergencyCall? = null
    private val mContacts = mutableListOf<WmContact>()

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
        contactListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadContactListCmd())
    }

    override fun updateContactList(contactList: List<WmContact>): Single<Boolean> = Single.create {
        updateContactEmitter = it
        val payloadPackage = CmdHelper.getWriteContactListCmd(contactList)

        sjUniWatch.observableMtu.subscribe { mtu ->
            sjUniWatch.sendWriteSubpackageNodeCmdList(
                (contactList.size * 52 + 10).toShort(),//通讯录个数长度+payload头长度
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
        emergencyNumberEmitter?.onNext(emergencyCall)
    }

    private fun updateEmergencyContactBack(success: Boolean) {
        updateEmergencyEmitter?.onSuccess(
            if (success) {
                mEmergencyCall
            } else {
                null
            }
        )
    }

    fun contactBusiness(
        payload: PayloadPackage,
        it: NodeData,
        msgBean: MsgBean?
    ) {
        when (it.urn[2]) {

            URN_APP_CONTACT_COUNT -> {
                contactCountSetEmitter?.onSuccess(it.data[0].toInt() == 1)
            }

            URN_APP_CONTACT_LIST -> {

                if (payload.packageSeq == 0) {
                    mContacts.clear()
                }

                val byteArray =
                    ByteBuffer.wrap(it.data).array()

                val chunkSize = 52

                if (it.dataLen.toInt() > chunkSize) {
                    var i = 0
                    while (i < byteArray.size) {
                        val nameBytes = byteArray.copyOfRange(i, i + 32)
                        val numBytes = byteArray.copyOfRange(i + 20, i + chunkSize)
                        val name = String(nameBytes).trim()
                        val num = String(numBytes).trim()
                        val contact = WmContact.create(name, num)
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

            URN_APP_CONTACT_UPDATE -> {
                updateContactListBack(it.data[0].toInt() == 1)
            }

            URN_APP_CONTACT_SET_EMERGENCY -> {
                updateEmergencyContactBack(it.data[0].toInt() == 1)
            }

            URN_APP_CONTACT_GET_EMERGENCY -> {

                val emergencyByteArray = it.data
                val enable = it.data[0].toInt() == 1
                val name = String(
                    emergencyByteArray.copyOf(32),
                    StandardCharsets.UTF_8
                )

                val num = String(
                    emergencyByteArray.copyOf(20),
                    StandardCharsets.UTF_8
                )

                val emergencyContacts = mutableListOf<WmContact>()
                WmContact.create(name, num)?.let {
                    emergencyContacts.add(it)
                }

                val emergencyCall = WmEmergencyCall(enable, emergencyContacts)
                getEmergencyContactBack(emergencyCall)
            }
        }
    }
}