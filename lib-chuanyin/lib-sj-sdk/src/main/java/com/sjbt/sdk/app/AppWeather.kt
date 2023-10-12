package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmWeather
import com.base.sdk.entity.apps.WmWeatherForecast
import com.base.sdk.port.app.AbAppWeather
import com.sjbt.sdk.SJUniWatch
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AppWeather(val sjUniWatch: SJUniWatch): AbAppWeather() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override fun pushWeather(weather: WmWeather): Single<Boolean> {
        TODO("Not yet implemented")
    }

    override var observeWeather: Observable<Boolean>
        get() = TODO("Not yet implemented")
        set(value) {}
}