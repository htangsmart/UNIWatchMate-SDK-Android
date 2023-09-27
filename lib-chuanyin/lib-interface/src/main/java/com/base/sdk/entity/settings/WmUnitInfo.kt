package com.base.sdk.entity.settings

/**
 * Unit info(单位同步数据)
 */
data class WmUnitInfo(
    /**
     * Length unit
     * CM, INCH
     */
    var lengthUnit: LengthUnit,
    /**
     * Weight unit
     * KG, LB
     */
    val weightUnit: WeightUnit,
    /**
     * Temperature unit
     * CELSIUS, FAHRENHEIT
     */
    var temperatureUnit: TemperatureUnit,
    /**
     * Time format
     * TWELVE_HOUR, TWENTY_FOUR_HOUR
     */
    var timeFormat: TimeFormat,

    /**
     * Distance unit
     * KM, MILE
     */
    val distanceUnit: DistanceUnit
) {
    enum class LengthUnit {
        CM,
        INCH
    }

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
        return "WmUnitInfo(lengthUnit=$lengthUnit, weightUnit=$weightUnit, temperatureUnit=$temperatureUnit, timeFormat=$timeFormat)"
    }
}

