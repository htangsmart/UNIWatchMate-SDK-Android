package com.base.api

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * 包装一个BehaviorSubject，但不允许修改它
 */
internal class BehaviorObservable<T : Any>(
    private val subject: BehaviorSubject<T>
) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {
        subject.subscribe(observer)
    }

    val value: T?
        get() = subject.value

}