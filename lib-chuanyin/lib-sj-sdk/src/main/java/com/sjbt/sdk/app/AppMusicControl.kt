package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmMusicControlType
import com.base.sdk.port.app.AbAppMusicControl
import com.sjbt.sdk.SJUniWatch
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

class AppMusicControl(val sjUniWatch: SJUniWatch) : AbAppMusicControl() {

    lateinit var observableMusicControlEmitter: ObservableEmitter<WmMusicControlType>
    override fun isSupport(): Boolean {
        return true
    }

    override var observableMusicControl: Observable<WmMusicControlType> = Observable.create {
        observableMusicControlEmitter = it
    }

}