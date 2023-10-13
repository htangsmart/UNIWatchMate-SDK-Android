package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherRequest
import com.base.sdk.entity.apps.WmWeatherTime
import com.base.sdk.entity.settings.WmUnitInfo
import com.base.sdk.port.app.AbAppWeather
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_APP_WEATHER_PUSH_SIX_DAYS
import com.sjbt.sdk.spp.cmd.URN_APP_WEATHER_PUSH_TODAY
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.charset.StandardCharsets

class AppWeather(val sjUniWatch: SJUniWatch) : AbAppWeather() {
    private var weatherRequestEmitter: ObservableEmitter<WmWeatherRequest>? = null
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
            val payloadPackage = CmdHelper.getWriteTodayWeatherCmd(
                temperatureUnit,
                weather
            )

            sjUniWatch.wmLog.logD(TAG, "today weather payload_len:" + payloadPackage.itemCount)

            sjUniWatch.sendWriteNodeCmdList(
                payloadPackage
            )
        }
    }

    override fun pushSevenTodayWeather(
        weather: WmWeather,
        temperatureUnit: WmUnitInfo.TemperatureUnit
    ): Single<Boolean> {
        return Single.create {
            pushWeatherEmitter = it
            val payloadPackage = CmdHelper.getWriteSevenTodayWeatherCmd(
                temperatureUnit,
                weather
            )

            sjUniWatch.wmLog.logD(TAG, "7 days weather payload_len:" + payloadPackage.itemCount)

            sjUniWatch.sendWriteNodeCmdList(
                payloadPackage
            )
        }
    }

    override var observeWeather: Observable<WmWeatherRequest> = Observable.create {
        weatherRequestEmitter = it
    }

    fun weatherBusiness(it: NodeData) {
        when (it.urn[3]) {
            URN_APP_WEATHER_PUSH_TODAY -> {
                val bcp = String(it.data, StandardCharsets.UTF_8)
                weatherRequestEmitter?.onNext(WmWeatherRequest(bcp, WmWeatherTime.TODAY))
            }

            URN_APP_WEATHER_PUSH_SIX_DAYS -> {
                val bcp = String(it.data, StandardCharsets.UTF_8)
                weatherRequestEmitter?.onNext(WmWeatherRequest(bcp, WmWeatherTime.SEVEN_DAYS))
            }
        }
    }

}