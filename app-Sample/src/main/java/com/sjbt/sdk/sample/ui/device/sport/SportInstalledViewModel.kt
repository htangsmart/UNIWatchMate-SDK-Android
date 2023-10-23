package com.sjbt.sdk.sample.ui.device.sport

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmSport
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
import kotlinx.coroutines.rx3.awaitFirst

data class SportState(
    val requestSports: Async<MutableList<WmSport>> = Uninitialized,
)

sealed class SportEvent {
    class RequestFail(val throwable: Throwable) : SportEvent()

    class DialRemoved(val position: Int) : SportEvent()
}

class SportInstalledViewModel : StateEventViewModel<SportState, SportEvent>(SportState()) {

    init {
        requestInstallSports()
    }

    fun requestInstallSports() {
        viewModelScope.launch {
            state.copy(requestSports = Loading()).newState()
            runCatchingWithLog {
//                UNIWatchMate.wmApps.appSport.syncSportList.awaitFirst()
                mutableListOf<WmSport>()
            }.onSuccess {
                if (it is MutableList) {
                    state.copy(requestSports = Success(it)).newState()
                }else{
                    state.copy(requestSports = Fail(Throwable("result is not a mutable list"))).newState()
                }
            }.onFailure {
                state.copy(requestSports = Fail(it)).newState()
                SportEvent.RequestFail(it).newEvent()
            }
        }
    }

    /**
     * @param position Delete position
     */
    fun deleteAlarm(position: Int) {
        viewModelScope.launch {
            val sports = state.requestSports()
            if (sports != null && position < sports.size) {
                try {
                    UNIWatchMate.wmApps.appSport.deleteSport(sports[position]).await()
                    sports.removeAt(position)
                    SportEvent.DialRemoved(position).newEvent()
                } catch (e: Exception) {
                    ToastUtil.showToast(e.message)
                }
            }
        }
    }

}