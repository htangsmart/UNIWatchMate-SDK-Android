package com.base.sdk.entity.common

/**
 * Time frequency(频次)
 */
enum class WmTimeFrequency {

    EVERY_30_MINUTES(30),
    EVERY_1_HOUR(60),
    EVERY_1_HOUR_30_MINUTES(90);

     var value = 0
    private constructor(value:Int) {
        this.value=value
    }

}