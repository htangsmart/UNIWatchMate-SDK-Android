package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherRequest
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.settings.WmUnitInfo
import com.base.sdk.port.app.AbAppWeather
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_APP_WEATHER_PUSH_SIX_DAYS
import com.sjbt.sdk.spp.cmd.URN_APP_WEATHER_PUSH_TODAY
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.subjects.PublishSubject
import java.nio.charset.StandardCharsets

class AppWeather(val sjUniWatch: SJUniWatch) : AbAppWeather() {
    private var pushWeatherEmitter: SingleEmitter<Boolean>? = null
    private val TAG = "AppWeather"

    override fun isSupport(): Boolean {
        return true
    }

    override fun pushTodayWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean> {

        return Single.create {
            pushWeatherEmitter = it

            var cityLen = weather.location.city?.let { it.toByteArray().size }
            var countryLen = weather.location.country?.let { it.toByteArray().size }

            var totalLen = 7 + cityLen + countryLen + 2
            sjUniWatch.wmLog.logD(TAG, "today weather payload_len:" + totalLen)

            weather.todayWeather.forEach {
                totalLen += it.weatherDesc.toByteArray().size + 13
            }

            sjUniWatch.wmLog.logD(TAG, "today weather payload_len:" + totalLen)
            val payloadPackage = CmdHelper.getWriteTodayWeatherCmd(
                totalLen,
                temperatureUnit,
                weather
            )

            sjUniWatch.wmLog.logD(
                TAG,
                "today weather package count:" + payloadPackage.itemCount
            )

            sjUniWatch.sendWriteNodeCmdList(
                payloadPackage
            )
        }
    }

    override fun pushSevenDaysWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean> {
        return Single.create {
            pushWeatherEmitter = it

            sjUniWatch.wmLog.logD(TAG, "weather_len:" + weather)

            sjUniWatch.observableMtu.subscribe { mtu ->
                var cityLen = weather.location.city?.let { it.toByteArray().size }
                var countryLen = weather.location.country?.let { it.toByteArray().size }

                var sevenDayLen = 7 + cityLen + countryLen + 2
                sjUniWatch.wmLog.logD(TAG, "7 days weather payload_len:" + sevenDayLen)

                weather.weatherForecast.forEach {
                    sevenDayLen += it.dayDesc.toByteArray().size + it.nightDesc.toByteArray().size + 18
                }

                sjUniWatch.wmLog.logD(TAG, "7 days weather total bytes:" + sevenDayLen)

                val payloadPackage = CmdHelper.getWriteSevenDaysWeatherCmd(
                    sevenDayLen,
                    temperatureUnit,
                    weather
                )

                sjUniWatch.wmLog.logD(
                    TAG,
                    "7 days weather package count:" + payloadPackage.itemCount
                )

                sjUniWatch.sendWriteNodeCmdList(
                    payloadPackage
                )

            }
        }
    }

    private val requestWeather: PublishSubject<WmWeatherRequest> = PublishSubject.create()

    override val observeWeather: PublishSubject<WmWeatherRequest> = requestWeather

    fun weatherBusiness(it: NodeData) {
        when (it.urn[3]) {
            URN_APP_WEATHER_PUSH_TODAY -> {
                if (it.dataLen.toInt() == 1) {
                    val result = it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                    sjUniWatch.wmLog.logD(TAG, "weather push result:$result")
                    pushWeatherEmitter?.onSuccess(result)
                } else {
                    val bcp = String(it.data, StandardCharsets.UTF_8)
                    observeWeather?.onNext(WmWeatherRequest(bcp, WmWeatherTime.TODAY))
                }
            }

            URN_APP_WEATHER_PUSH_SIX_DAYS -> {
                if (it.dataLen.toInt() == 1) {
                    val result = it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                    sjUniWatch.wmLog.logD(TAG, "weather push result:$result")
                    pushWeatherEmitter?.onSuccess(result)
                } else {
                    val bcp = String(it.data, StandardCharsets.UTF_8)
                    observeWeather?.onNext(WmWeatherRequest(bcp, WmWeatherTime.SEVEN_DAYS))
                }
            }
        }
    }

}