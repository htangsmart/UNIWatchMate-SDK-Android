package com.base.sdk.port.setting

import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 所有设置模块父类
 */
abstract class AbWmSetting<T : Any> : IWmSupport {
    abstract fun observeChange(): Observable<T>
    abstract fun set(obj: T): Single<T>
    abstract fun get(): Single<T>
}