package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmMusicControlType
import com.base.sdk.port.app.AbAppMusicControl
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter

class AppMusicControl(val sjUniWatch: SJUniWatch) : AbAppMusicControl() {

    private var observableMusicControlEmitter: ObservableEmitter<WmMusicControlType>? = null

    private fun observeMusicControl(musicControl: WmMusicControlType) {
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

    fun musicControlBusiness(it: NodeData) {
        when (it.data[0]) {
            WmMusicControlType.PREV_SONG.type -> {
                observeMusicControl(WmMusicControlType.PREV_SONG)
            }

            WmMusicControlType.NEXT_SONG.type -> {
                observeMusicControl(WmMusicControlType.NEXT_SONG)
            }

            WmMusicControlType.PLAY.type -> {
                observeMusicControl(WmMusicControlType.PLAY)
            }

            WmMusicControlType.PAUSE.type -> {
                observeMusicControl(WmMusicControlType.PAUSE)
            }

            WmMusicControlType.VOLUME_UP.type -> {
                observeMusicControl(WmMusicControlType.VOLUME_UP)
            }

            WmMusicControlType.VOLUME_DOWN.type -> {
                observeMusicControl(WmMusicControlType.VOLUME_DOWN)
            }
        }
    }

}