package com.base.sdk.entity.settings

import com.base.sdk.entity.apps.WmContact

/**
 * Emergency call(紧急联系人)
 */
class WmEmergencyCall(
    /**
     * Whether to enable emergency contact(是否启用紧急联系人)
     */
    var isEnabled: Boolean,
    /**
     * Emergency contacts(紧急联系人列表)
     */
    val emergencyContacts: MutableList<WmContact>
) {
    fun addContact(contact: WmContact) {
        emergencyContacts.add(contact)
    }

    fun removeContact(contact: WmContact) {
        emergencyContacts.remove(contact)
    }

    override fun toString(): String {
        return "WmEmergencyCall(isEnabled=$isEnabled, emergencyContacts=$emergencyContacts)"
    }


}
