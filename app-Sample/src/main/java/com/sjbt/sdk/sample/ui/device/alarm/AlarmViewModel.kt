package com.sjbt.sdk.sample.ui.device.alarm

import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmAlarm
import com.sjbt.sdk.sample.base.Async
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncAction
import com.sjbt.sdk.sample.base.StateEventViewModel
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.base.Uninitialized
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitFirst
import kotlinx.coroutines.rx3.awaitSingle

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

enum class AlarmAction {
    ADD,
    UPDATE,
    DELETE
}

class AlarmViewModel : StateEventViewModel<AlarmState, AlarmEvent>(AlarmState()) {
    var setAlarm: WmAlarm? = null
    var alarmAction: AlarmAction? = null

    //    private val deviceManager = Injector.getDeviceManager()
    init {
        requestAlarms()
    }

    fun requestAlarms() {
        viewModelScope.launch {
            state.copy(requestAlarms = Loading()).newState()
            runCatchingWithLog {
                UNIWatchMate.wmApps.appAlarm.syncAlarmList.awaitFirst()
//                mutableListOf<WmAlarm>()
            }.onSuccess {
                state.copy(requestAlarms = Success(ArrayList(AlarmHelper.sort(it)))).newState()
            }.onFailure {
                state.copy(requestAlarms = Fail(it)).newState()
                AlarmEvent.RequestFail(it).newEvent()
            }
        }
    }

    private fun findAlarmAddPosition(alarm: WmAlarm, list: List<WmAlarm>): Int {
        var addPosition: Int? = null
        for (i in list.indices) {
            if (AlarmHelper.comparator.compare(alarm, list[i]) < 0) {
                addPosition = i
                break
            }
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
                setAlarm = alarm
                alarmAction = AlarmAction.ADD
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
                val alarm = alarms.removeAt(position)
                setAlarm = alarm
                alarmAction = AlarmAction.DELETE
                setAlarmsAction.execute()
                AlarmEvent.AlarmRemoved(position).newEvent()
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
                setAlarm = alarmModified
                alarmAction = AlarmAction.UPDATE
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
            setAlarm?.let {
                when (alarmAction) {
                    AlarmAction.ADD -> {
                        UNIWatchMate.wmApps.appAlarm.addAlarm(it).await()
                    }

                    AlarmAction.UPDATE -> {
                        UNIWatchMate.wmApps.appAlarm.updateAlarm(it).await()
                    }

                    AlarmAction.DELETE -> {
                        UNIWatchMate.wmApps.appAlarm.deleteAlarm(it).await()
                    }

                    else -> {

                    }
                }
            }
        }
    }

}