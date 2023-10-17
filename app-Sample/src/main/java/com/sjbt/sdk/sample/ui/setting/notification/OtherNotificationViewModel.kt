package com.sjbt.sdk.sample.ui.setting.notification

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmDial
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitSingle

data class DialState(
    val requestDials: Async<MutableList<WmDial>> = Uninitialized,
)

sealed class DialEvent {
    class RequestFail(val throwable: Throwable) : DialEvent()

    class DialRemoved(val position: Int) : DialEvent()
}

class OtherNotificationViewModel : StateEventViewModel<DialState, DialEvent>(DialState()) {

    init {
        requestInstallDials()
    }

    fun requestInstallDials() {
        viewModelScope.launch {
            state.copy(requestDials = Loading()).newState()
            runCatchingWithLog {
                UNIWatchMate.wmApps.appDial.syncDialList().awaitSingle()
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
            val alarms = state.requestDials()
            if (alarms != null && position < alarms.size) {
                try {
                    UNIWatchMate.wmApps.appDial.deleteDial(alarms[position]).await()
                    alarms.removeAt(position)
                    DialEvent.DialRemoved(position).newEvent()
                } catch (e: Exception) {
                    ToastUtil.showToast(e.message)
                }
            }
        }
    }

}