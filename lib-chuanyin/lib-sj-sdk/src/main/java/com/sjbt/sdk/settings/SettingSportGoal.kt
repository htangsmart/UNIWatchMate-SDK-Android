package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.`interface`.setting.AbWmSetting
import io.reactivex.rxjava3.core.*

class SettingSportGoal : AbWmSetting<WmSportGoal>() {
    lateinit var observeEmitter: ObservableEmitter<WmSportGoal>
    lateinit var setEmitter: SingleEmitter<WmSportGoal>
    lateinit var getEmitter: SingleEmitter<WmSportGoal>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSportGoal> {
        return Observable.create(object : ObservableOnSubscribe<WmSportGoal> {
            override fun subscribe(emitter: ObservableEmitter<WmSportGoal>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmSportGoal): Single<WmSportGoal> {
        return Single.create(object : SingleOnSubscribe<WmSportGoal> {
            override fun subscribe(emitter: SingleEmitter<WmSportGoal>) {
                setEmitter = emitter
            }
        })
    }

    override fun get(): Single<WmSportGoal> {
        return Single.create(object : SingleOnSubscribe<WmSportGoal> {
            override fun subscribe(emitter: SingleEmitter<WmSportGoal>) {
                getEmitter = emitter
            }
        })
    }
}