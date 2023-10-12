package com.base.sdk.entity.apps

/**
 * AlarmData structure 闹钟数据结构
 */
class WmAlarm(
    var alarmId: Int,//闹钟Id
    var alarmName: String,//限制最长20
    var hour: Int,
    var minute: Int,
    var repeatOptions: AlarmRepeatOption//重复模式
) {
    var isOn: Boolean = false

    override fun toString(): String {
        return "WmAlarm(alarmId=$alarmId, alarmName='$alarmName', hour=$hour, minute=$minute, repeatOptions=$repeatOptions, isOn=$isOn)"
    }
}

/**
 * 重复模式
 */
enum class AlarmRepeatOption(val value: Int) {
    NONE(0),
    SUNDAY(1 shl 0),
    MONDAY(1 shl 1),
    TUESDAY(1 shl 2),
    WEDNESDAY(1 shl 3),
    THURSDAY(1 shl 4),
    FRIDAY(1 shl 5),
    SATURDAY(1 shl 6);

    companion object {
        fun fromValue(value: Int): Set<AlarmRepeatOption> {
            return AlarmRepeatOption.values().filter { option -> option.value and value != 0 }
                .toSet()
        }
    }
}
