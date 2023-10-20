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
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst

data class EmergencyContactsState(
    val requestEmergencyCall: Async<WmEmergencyCall> = Uninitialized,
)

sealed class EmergencyCallEvent {
    class RequestFail(val throwable: Throwable) : EmergencyCallEvent()

    object NavigateUp : EmergencyCallEvent()
}


class EmergencyContactViewModel :
    StateEventViewModel<EmergencyContactsState, EmergencyCallEvent>(EmergencyContactsState()) {

    private val deviceManager = Injector.getDeviceManager()

    init {
        requestEmegencyCall()
    }

    fun requestEmegencyCall() {
        viewModelScope.launch {
            state.copy(requestEmergencyCall = Loading()).newState()
            runCatchingWithLog {
                UNIWatchMate.wmLog.logI("EmergencyContactViewModel","observableEmergencyContacts")
                UNIWatchMate.wmApps.appContact.observableEmergencyContacts().awaitFirst()
            }.onSuccess {
                UNIWatchMate.wmLog.logI("EmergencyContactViewModel","observableEmergencyContacts result$it")
                state.copy(requestEmergencyCall = Success(it)).newState()
            }.onFailure {
                state.copy(requestEmergencyCall = Fail(it)).newState()
                EmergencyCallEvent.RequestFail(it).newEvent()
            }
        }
    }

    fun setEmergencyEnbalbe(enable: Boolean) {
        viewModelScope.launch {
            val call = state.requestEmergencyCall()
            call?.let {
                it.isEnabled = enable
                setEmergencyCall(it)
            }
        }
    }

    fun setEmergencyContact(contact: WmContact) {
        viewModelScope.launch {
            val call = state.requestEmergencyCall()
            call?.let {
                it.emergencyContacts.clear()
                it.emergencyContacts.add(contact)
                setEmergencyCall(it)
            }
        }
    }

    suspend fun setEmergencyCall(call: WmEmergencyCall) {
        val result = UNIWatchMate.wmApps.appContact.updateEmergencyContact(call).await()
        UNIWatchMate.wmLog.logD(this.javaClass.simpleName, "result=$result")
    }

}