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
import com.sjbt.sdk.sample.model.LocalSportLibrary
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

data class SportState(
    val requestSports: Async<MutableList<WmSport>> = Uninitialized,
)

sealed class SportEvent {
    class RequestFail(val throwable: Throwable) : SportEvent()

    class DialRemoved(val position: Int) : SportEvent()

    class SportInstallSuccess(val position: Int) : SportEvent()
    class SportInstallFail(val msg: String) : SportEvent()
}

class SportInstalledViewModel : StateEventViewModel<SportState, SportEvent>(SportState()) {

    init {
        requestInstallSports()
    }

    fun requestInstallSports() {
        viewModelScope.launch {
            state.copy(requestSports = Loading()).newState()
//            UNIWatchMate.wmApps.appSport.syncSportList.asFlow().catch {
//                state.copy(requestSports = Fail(it)).newState()
//                SportEvent.RequestFail(it).newEvent()
//            }.collect{
//
//            }
            runCatchingWithLog {
                UNIWatchMate.wmApps.appSport.getSportList.await()
//                val mutableList = mutableListOf<WmSport>()
//                for (index in 0..19) {
//                    val sport = WmSport(index, 1, index < 8)
//                    mutableList.add(sport)
//                }
//                mutableList
            }.onSuccess {
                if (it is MutableList) {
                    state.copy(requestSports = Success(it)).newState()
                } else {
                    state.copy(requestSports = Fail(Throwable("result is not a mutable list")))
                        .newState()
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
    fun deleteSport(position: Int) {
        viewModelScope.launch {
            val sports = state.requestSports()

            if (sports != null && position+8 < sports.size) {
                sports.removeAt(position+8)
                runCatchingWithLog {
                    UNIWatchMate.wmApps.appSport.updateSportList(sports).await()
                }.onSuccess {
                    SportEvent.DialRemoved(position).newEvent()
                }.onFailure {
                    ToastUtil.showToast(it.message)

                }
            }
        }
    }

    fun sortFixedSportList() {
        viewModelScope.launch {
            val sports = state.requestSports()
            if (sports != null) {
                runCatchingWithLog {
                    UNIWatchMate.wmApps.appSport.updateSportList(sports).await()
                }.onSuccess {

                }.onFailure {
                    ToastUtil.showToast(it.message)
                }
            }
        }
    }

    fun installContactContain(position:Int,localSport: LocalSportLibrary.LocalSport) {
        viewModelScope.launch {
            val wmSports = state.requestSports()
            wmSports?.let {
                val wmSport = WmSport(localSport.id, localSport.type, localSport.buildIn)
                wmSports.add(wmSport)
                runCatchingWithLog {
                    val result = UNIWatchMate.wmApps.appSport.updateSportList(wmSports).await()
                }.onSuccess {
                    localSport.installed = true
                    SportEvent.SportInstallSuccess(position).newEvent()
                }.onFailure {
                    wmSports.removeAt(wmSports.size-1)
                    SportEvent.SportInstallFail(it.toString()).newEvent()
                }
            }
        }
    }
}