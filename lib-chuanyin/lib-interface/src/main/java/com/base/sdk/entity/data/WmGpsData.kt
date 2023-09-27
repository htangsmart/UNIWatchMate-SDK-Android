package com.base.sdk.entity.data

class WmGpsData(
    timestamp: Long,
    /**
     * Which [FcSportData] belongs to
     */
    val sportId: String,
    val items: List<WmGpsItem>
) : WmBaseSyncData(timestamp)

class WmGpsItem(
    /**
     * The duration(unit seconds) of sport at which this item is generated
     */
    val duration: Int,
    val lng: Double,
    val lat: Double,
    val altitude: Float,

    /**
     * The number of satellites represents the strength of the signal at this time
     */
    val satellites: Int,
    /**
     * Is it the first point after resuming sport?
     * True for yes, false for not.
     */
    val isRestart: Boolean,
)