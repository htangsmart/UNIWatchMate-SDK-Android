package com.base.sdk.`interface`.app

import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.base.sdk.`interface`.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-通讯录
 */
abstract class AbAppContact : IWmSupport {

    /**
     * syncContactList 同步通讯录列表
     */
    abstract var observableContactList: Observable<List<WmContact>>

    abstract fun syncContactList(contactList: List<WmContact>): Single<Boolean>

//    /**
//     * addContact 添加通讯录
//     */
//    abstract fun addContact(contact: WmContact): Observable<WmContact>
//
//    /**
//     * deleteContact 删除通讯录
//     */
//    abstract fun deleteContact(contact: WmContact): Observable<WmContact>

    /**
     * syncEmergencyContacts 获取紧急联系人
     */
    abstract fun observableEmergencyContacts(): Observable<WmEmergencyCall>

    /**
     * updateEmergencyContact 设置紧急联系人 null 为删除
     */
    abstract fun updateEmergencyContact(contacts: WmEmergencyCall): Single<WmEmergencyCall>

}