package com.sjbt.sdk.sample.ui.device.alarm

import androidx.lifecycle.viewModelScope
import com.base.sdk.entity.apps.WmAlarm
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncAction
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.topstep.fitcloud.sample2.ui.device.alarm.AlarmHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await

data class AlarmState(
    val requestAlarms: Async<ArrayList<WmAlarm>> = Uninitialized,
)

sealed class AlarmEvent {
    class RequestFail(val throwable: Throwable) : AlarmEvent()

    class AlarmInserted(val position: Int) : AlarmEvent()
    class AlarmRemoved(val position: Int) : AlarmEvent()
    class AlarmMoved(val fromPosition: Int, val toPosition: Int) : AlarmEvent()

    object NavigateUp : AlarmEvent()
}

class AlarmViewModel : StateEventViewModel<AlarmState, AlarmEvent>(AlarmState()) {

//    private val deviceManager = Injector.getDeviceManager()
    val helper = AlarmHelper()

    init {
        requestAlarms()
    }

    fun requestAlarms() {
        viewModelScope.launch {
            state.copy(requestAlarms = Loading()).newState()
//            runCatchingWithLog {
//                deviceManager.settingsFeature.requestAlarms().await()
//            }.onSuccess {
//                state.copy(requestAlarms = Success(ArrayList(helper.sort(it)))).newState()
//            }.onFailure {
//                state.copy(requestAlarms = Fail(it)).newState()
//                AlarmEvent.RequestFail(it).newEvent()
//            }
        }
    }

    private fun findAlarmAddPosition(alarm: WmAlarm, list: List<WmAlarm>): Int {
        var addPosition: Int? = null
        for (i in list.indices) {
//            if (helper.comparator.compare(alarm, list[i]) < 0) {
//                addPosition = i
//                break
//            }
        }
        if (addPosition == null) {
            addPosition = list.size
        }
        return addPosition
    }

    fun addAlarm(alarm: WmAlarm) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null) {
                val addPosition = findAlarmAddPosition(alarm, alarms)
                alarms.add(addPosition, alarm)
                AlarmEvent.AlarmInserted(addPosition).newEvent()
                setAlarmsAction.execute()
            }
        }
    }

    /**
     * @param position Delete position
     */
    fun deleteAlarm(position: Int) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null && position < alarms.size) {
                alarms.removeAt(position)
                AlarmEvent.AlarmRemoved(position).newEvent()
                setAlarmsAction.execute()
            }
        }
    }

    /**
     * @param position Modify position
     * @param alarmModified Modified data
     */
    fun modifyAlarm(position: Int, alarmModified: WmAlarm) {
        viewModelScope.launch {
            val alarms = state.requestAlarms()
            if (alarms != null && position < alarms.size) {
                if (alarms.contains(alarmModified)) {
                    throw IllegalStateException()//不能直接改list里的数据
                }
                alarms.removeAt(position)
                val addPosition = findAlarmAddPosition(alarmModified, alarms)
                alarms.add(addPosition, alarmModified)
                AlarmEvent.AlarmMoved(position, addPosition).newEvent()
                setAlarmsAction.execute()
            }
        }
    }

    fun sendNavigateUpEvent() {
        viewModelScope.launch {
            delay(1000)
            AlarmEvent.NavigateUp.newEvent()
        }
    }

    val setAlarmsAction = object : SingleAsyncAction<Unit>(
        viewModelScope,
        Uninitialized
    ) {
        override suspend fun action() {
//            deviceManager.settingsFeature.setAlarms(state.requestAlarms()).await()
        }
    }
}