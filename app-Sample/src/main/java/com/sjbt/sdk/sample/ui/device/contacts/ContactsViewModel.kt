package com.sjbt.sdk.sample.ui.device.contacts

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmContact
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import timber.log.Timber

data class ContactsState(
    val requestContacts: Async<ArrayList<WmContact>> = Uninitialized,
)


sealed class ContactsEvent {
    class RequestFail(val throwable: Throwable) : ContactsEvent()
    class RequestEmergencyFail(val throwable: Throwable) : ContactsEvent()
    class Inserted(val pos: Int) : ContactsEvent()
    class Update100Success() : ContactsEvent()
    class Removed(val position: Int) : ContactsEvent()
    class UpdateFail() : ContactsEvent()
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
                UNIWatchMate.wmApps.appContact.getContactList.awaitFirst()
            }.onSuccess {
                state.copy(requestContacts = Success(ArrayList(it))).newState()
            }.onFailure {
                state.copy(requestContacts = Fail(it)).newState()
                ContactsEvent.RequestFail(it).newEvent()
            }
        }

    }

    fun addContacts(contact: WmContact) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null) {
                var exist = false
                for (item in list) {
                    if (item.number == contact.number && item.name == contact.name) {
                        exist = true
                        break
                    }
                }
                if (!exist) {
                    list.add(contact)
                    runCatchingWithLog {
                        action(list)
                    }.onSuccess {
                        ContactsEvent.Inserted(list.size).newEvent()
                    }.onFailure {
                        ContactsEvent.UpdateFail().newEvent()
                    }
                }
            }
        }
    }

    fun add100Contacts(contacts: MutableList<WmContact>) {
        viewModelScope.launch {
            val list = state.requestContacts()
            if (list != null) {
                list.clear()
                list.addAll(contacts)
                runCatchingWithLog {
                    action(list)
                }.onSuccess {
                    ContactsEvent.Update100Success().newEvent()
                }.onFailure {
                    ContactsEvent.UpdateFail().newEvent()
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
                runCatchingWithLog {
                    action(list)
                }.onSuccess {
                    ContactsEvent.Removed(position).newEvent()
                }.onFailure {
                    ContactsEvent.UpdateFail().newEvent()
                }
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            ContactsEvent.NavigateUp.newEvent()
        }
    }

    suspend fun action(list: ArrayList<WmContact>) {
        val result = UNIWatchMate.wmApps.appContact.updateContactList(list).await()
        Timber.i("setContactsAction result=$result ")
    }
}
