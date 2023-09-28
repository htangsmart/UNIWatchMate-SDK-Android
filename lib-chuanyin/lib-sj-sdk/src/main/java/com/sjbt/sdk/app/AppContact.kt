package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.port.app.AbAppContact
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AppContact : AbAppContact() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override var observableContactList: Observable<List<WmContact>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun syncContactList(contactList: List<WmContact>): Single<Boolean> {
        TODO("Not yet implemented")
    }

    override fun observableEmergencyContacts(): Observable<WmEmergencyCall> {
        TODO("Not yet implemented")
    }

    override fun updateEmergencyContact(contacts: WmEmergencyCall): Single<WmEmergencyCall> {
        TODO("Not yet implemented")
    }
}