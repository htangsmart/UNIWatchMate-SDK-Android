package com.base.sdk.entity.settings

/**
 * The date and time information of the watch
 * (日期与时间同步信息)
 */
data class WmDateTime(
    /**
     * The time zone ID string, such as "Asia/Shanghai", "America/Los_Angeles", "Europe/London", etc.
     * @see <a href="https://en.wikipedia.org/wiki/List_of_tz_database_time_zones">List of tz database time zones</a>
     */
    val timeZone: String?,
    /**
     * The time format, 12-hour or 24-hour
     */
    val timeFormat: WmUnitInfo.TimeFormat?,
    /**
     * The date format
     */
    val dateFormat: WmUnitInfo.DateFormat?,
    /**
     * The timestamp, the milliseconds since January 1, 1970, 00:00:00 GMT. Optional.
     */
    val timestamp: Long?,
    /**
     * The current time, such as "12:00:00"
     */
    val currentTime: String?,
    /**
     * The current date, such as "2020-01-20"
     */
    val currentDate: String?
) {
    override fun toString(): String {
        return "WmDateTime(timeZone='$timeZone', timeFormat=$timeFormat, dateFormat=$dateFormat, timestamp=$timestamp, currentTime='$currentTime', currentDate='$currentDate')"
    }
}
