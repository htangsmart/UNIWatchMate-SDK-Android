package com.base.sdk.entity.settings

/**
 * Unit info(单位同步数据)
 */
data class WmUnitInfo(
    /**
     * Weight unit
     * KG, LB
     */
    val weightUnit: WeightUnit,

    /**
     * Temperature unit
     * CELSIUS, FAHRENHEIT
     */
    val temperatureUnit: TemperatureUnit,

    /**
     * Time format
     * TWELVE_HOUR, TWENTY_FOUR_HOUR
     */
    val timeFormat: TimeFormat,

    /**
     * Distance unit
     * KM, MILE
     */
    val distanceUnit: DistanceUnit
) {

    enum class WeightUnit {
        KG,
        LB
    }

    enum class TemperatureUnit {
        CELSIUS,
        FAHRENHEIT
    }

    enum class TimeFormat {
        TWELVE_HOUR,
        TWENTY_FOUR_HOUR
    }

    enum class DateFormat {
        YYYY_MM_DD, //  2020-01-20
        DD_MM_YYYY, //  20-01-2020
        MM_DD_YYYY,  //  01-20-2020
        DDMMYYYY //  20/01/2020
    }

    enum class DistanceUnit {
        KM,
        MILE
    }

    override fun toString(): String {
        return "WmUnitInfo(weightUnit=$weightUnit, temperatureUnit=$temperatureUnit, timeFormat=$timeFormat)"
    }
}

