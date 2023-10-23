package com.sjbt.sdk.sample.ui.device.contacts

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmContact
import com.base.sdk.entity.settings.WmEmergencyCall
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncAction
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst

data class ContactsState(
    val requestContacts: Async<ArrayList<WmContact>> = Uninitialized,
)


sealed class ContactsEvent {
    class RequestFail(val throwable: Throwable) : ContactsEvent()
    class RequestEmergencyFail(val throwable: Throwable) : ContactsEvent()
    class Inserted(val position: Int) : ContactsEvent()
    class Removed(val position: Int) : ContactsEvent()
    class Moved(val fromPosition: Int, val toPosition: Int) : ContactsEvent()

    object NavigateUp : ContactsEvent()
}


class ContactsViewModel : StateEventViewModel<ContactsState, ContactsEvent>(ContactsState()) {

    private val deviceManager = Injector.getDeviceManager()

    init {
        requestContacts()
    }

    fun requestContacts() {
        viewModelScope.launch {
            state.copy(requestContacts = Loading()).newState()
            runCatchingWithLog {
                UNIWatchMate.wmApps.appContact.observableContactList.awaitFirst()
            }.onSuccess {
                state.copy(requestContacts = Success(ArrayList(it))).newState()
            }.onFailure {
                state.copy(requestContacts = Fail(it)).newState()
                ContactsEvent.RequestFail(it).newEvent()
            }
        }

    }

    fun addContacts(contacts: WmContact) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null) {
                var exist = false
                for (item in list) {
                    if (item.number == contacts.number) {
                        exist = true
                        break
                    }
                }
                if (!exist) {
                    list.add(contacts)
                    ContactsEvent.Inserted(list.size).newEvent()
                    setContactsAction.execute()
                }
            }
        }
    }

    /**
     * @param position 要删除的位置
     */
    fun deleteContacts(position: Int) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null && position < list.size) {
                list.removeAt(position)
                ContactsEvent.Removed(position).newEvent()
                setContactsAction.execute()
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            ContactsEvent.NavigateUp.newEvent()
        }
    }

    val setContactsAction = object : SingleAsyncAction<Unit>(
        viewModelScope,
        Uninitialized
    ) {
        override suspend fun action() {
            state.requestContacts()
                ?.let { UNIWatchMate.wmApps.appContact.updateContactList(it).await() }
        }
    }
}
