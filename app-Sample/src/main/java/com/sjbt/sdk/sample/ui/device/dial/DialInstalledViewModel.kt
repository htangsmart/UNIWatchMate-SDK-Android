package com.sjbt.sdk.sample.ui.device.dial

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmAlarm
import com.base.sdk.entity.apps.WmDial
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncAction
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.ui.device.alarm.AlarmEvent
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.topstep.fitcloud.sample2.ui.device.alarm.AlarmHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst

data class DialState(
    val requestDials: Async<MutableList<WmDial>> = Uninitialized,
)

sealed class DialEvent {
    class RequestFail(val throwable: Throwable) : DialEvent()

    class DialRemoved(val position: Int) : DialEvent()
}

class DialInstalledViewModel : StateEventViewModel<DialState, DialEvent>(DialState()) {

//    private val deviceManager = Injector.getDeviceManager()
    val helper = AlarmHelper()

    init {
        requestAlarms()
    }

    fun requestAlarms() {
        viewModelScope.launch {
            state.copy(requestDials = Loading()).newState()
            runCatchingWithLog {
                UNIWatchMate.wmApps.appDial.syncDialList().awaitFirst()
            }.onSuccess {
                if (it is MutableList) {
                    state.copy(requestDials = Success(it)).newState()
                }else{
                    state.copy(requestDials = Fail(Throwable("result is not a mutable list"))).newState()
                }
            }.onFailure {
                state.copy(requestDials = Fail(it)).newState()
                DialEvent.RequestFail(it).newEvent()
            }
        }
    }

    /**
     * @param position Delete position
     */
    fun deleteAlarm(position: Int) {
        viewModelScope.launch {
            state.copy(requestDials = Loading()).newState()
            val alarms = state.requestDials()
            if (alarms != null && position < alarms.size) {
                UNIWatchMate.wmApps.appDial.deleteDial(alarms[position]).await()
                alarms.removeAt(position)

                DialEvent.DialRemoved(position).newEvent()
            }
        }
    }

}