package com.base.sdk.entity.apps

/**
 * 天气请求返回
 */
data class WmWeatherRequest(val bcp: String,val wmWeatherTime: WmWeatherTime)

/**
 * 请求当天，还是未来7天
 */
enum class WmWeatherTime{
    TODAY,
    SEVEN_DAYS
}