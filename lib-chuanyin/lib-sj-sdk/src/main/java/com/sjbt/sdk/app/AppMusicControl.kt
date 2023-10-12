package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmMusicControlType
import com.base.sdk.port.app.AbAppMusicControl
import com.sjbt.sdk.SJUniWatch
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

class AppMusicControl(val sjUniWatch: SJUniWatch) : AbAppMusicControl() {

    private var observableMusicControlEmitter: ObservableEmitter<WmMusicControlType>? = null

    fun observeMusicControl(musicControl: WmMusicControlType) {
        observableMusicControlEmitter?.let {
            it.onNext(musicControl)
        }
    }

    override fun isSupport(): Boolean {
        return true
    }

    override var observableMusicControl: Observable<WmMusicControlType> = Observable.create {
        observableMusicControlEmitter = it
    }

}