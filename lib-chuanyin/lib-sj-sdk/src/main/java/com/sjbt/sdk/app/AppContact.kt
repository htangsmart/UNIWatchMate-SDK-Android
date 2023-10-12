package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.port.app.AbAppContact
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class AppContact(val sjUniWatch: SJUniWatch) : AbAppContact() {
    var contactListEmitter: ObservableEmitter<List<WmContact>>? = null
    var contactSetEmitter: SingleEmitter<Boolean>? = null
    var contactCountSetEmitter: SingleEmitter<Boolean>? = null
    var updateEmergencyEmitter: SingleEmitter<WmEmergencyCall>? = null
    var emergencyNumberEmitter: ObservableEmitter<WmEmergencyCall>? = null

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

    override fun syncContactList(contactList: List<WmContact>): Single<Boolean> = Single.create {
        contactSetEmitter = it
        val payloadPackage = CmdHelper.getWriteContactListCmd(contactList)
        sjUniWatch.sendWriteSubpackageNodeCmdList((contactList.size * 52).toShort(), payloadPackage)
    }

    override fun observableEmergencyContacts(): Observable<WmEmergencyCall> = Observable.create {
        emergencyNumberEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadEmergencyNumberCmd())
    }

    override fun updateEmergencyContact(emergencyCall: WmEmergencyCall): Single<WmEmergencyCall> =
        Single.create {
            updateEmergencyEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteEmergencyNumberCmd(emergencyCall))
        }
}