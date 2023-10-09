package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.RequestType
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.LogUtils
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingSportGoal(sjUniWatch: SJUniWatch) : AbWmSetting<WmSportGoal>() {
    lateinit var observeEmitter: ObservableEmitter<WmSportGoal>
    lateinit var setEmitter: SingleEmitter<WmSportGoal>
    lateinit var getEmitter: SingleEmitter<WmSportGoal>

    val mSjUniWatch = sjUniWatch

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
                val payloadPackage = CmdHelper.getUpdateSportGoalAllCmd(obj)

                mSjUniWatch.sendNodeCmdList(RequestType.REQ_TYPE_WRITE, payloadPackage)
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