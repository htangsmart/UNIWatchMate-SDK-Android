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

            sjUniWatch.observableMtu.subscribe { mtu ->
                var totalLen = 0

                weather.todayWeather.forEach {
                    totalLen += it.weatherDesc.length + 14
                }

                sjUniWatch.wmLog.logD(TAG, "today weather payload_len:" + totalLen)

                if (totalLen == 0) {
                    pushWeatherEmitter?.onError(RuntimeException("error weather data!"))
                } else {
                    val payloadPackage = CmdHelper.getWriteTodayWeatherCmd(
                        totalLen,
                        temperatureUnit,
                        weather
                    )

                    sjUniWatch.wmLog.logD(
                        TAG,
                        "today weather package count:" + payloadPackage.itemCount
                    )

                    sjUniWatch.sendWriteSubpackageNodeCmdList(
                        (totalLen + 10).toShort(),//当天天气总长度
                        mtu,
                        payloadPackage
                    )
                }
            }
        }
    }

    override fun pushSevenDaysWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean> {
        return Single.create {
            pushWeatherEmitter = it

            sjUniWatch.observableMtu.subscribe { mtu ->
                var sevenDayLen = 0

                weather.weatherForecast.forEach {
                    sevenDayLen += it.dayDesc.length + it.nightDesc.length + 18
                }

                sjUniWatch.wmLog.logD(TAG, "7 days weather total bytes:" + sevenDayLen)

                if (sevenDayLen == 0) {
                    pushWeatherEmitter?.onError(RuntimeException("error weather data!"))
                } else {
                    val payloadPackage = CmdHelper.getWriteSevenTodayWeatherCmd(
                        sevenDayLen,
                        temperatureUnit,
                        weather
                    )

                    sjUniWatch.wmLog.logD(
                        TAG,
                        "7 days weather package count:" + payloadPackage.itemCount
                    )

                    sjUniWatch.sendWriteSubpackageNodeCmdList(
                        (sevenDayLen + 10).toShort(),//7天天气总长度
                        mtu,
                        payloadPackage
                    )
                }

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