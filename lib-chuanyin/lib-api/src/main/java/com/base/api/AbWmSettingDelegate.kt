package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.exception.WmDisconnectedException
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlin.reflect.KProperty1

internal class AbWmSettingDelegate<T : Any>(
    private val watchObservable: BehaviorObservable<AbUniWatch>,
    private val property: KProperty1<AbWmSettings, AbWmSetting<T>>,
) : AbWmSetting<T>(
) {

    override fun isSupport(): Boolean {
        return watchObservable.value?.wmSettings?.let(property)?.isSupport() ?: false
    }

    override fun observeChange(): Observable<T> {
        return watchObservable.switchMap {
            property.get(it.wmSettings).observeChange()
        }
    }

    override fun set(obj: T): Single<T> {
        val watch = watchObservable.value
        return if (watch == null) {
            Single.error(WmDisconnectedException())
        } else {
            property.get(watch.wmSettings).set(obj)
        }
    }

    override fun get(): Single<T> {
        val watch = watchObservable.value
        return if (watch == null) {
            Single.error(WmDisconnectedException())
        } else {
            property.get(watch.wmSettings).get()
        }
    }
}