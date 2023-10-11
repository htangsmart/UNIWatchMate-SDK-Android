package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmUnitInfo
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingUnitInfo(val sjUniWatch: SJUniWatch) : AbWmSetting<WmUnitInfo>() {
    lateinit var observeEmitter: ObservableEmitter<WmUnitInfo>
    lateinit var setEmitter: SingleEmitter<WmUnitInfo>
    lateinit var getEmitter: SingleEmitter<WmUnitInfo>

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmUnitInfo> {
        return Observable.create(object : ObservableOnSubscribe<WmUnitInfo> {
            override fun subscribe(emitter: ObservableEmitter<WmUnitInfo>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmUnitInfo): Single<WmUnitInfo> {
        return Single.create(object : SingleOnSubscribe<WmUnitInfo> {
            override fun subscribe(emitter: SingleEmitter<WmUnitInfo>) {
                setEmitter = emitter
                sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteUnitSettingCmd(obj))
            }
        })
    }

    override fun get(): Single<WmUnitInfo> {
        return Single.create(object : SingleOnSubscribe<WmUnitInfo> {
            override fun subscribe(emitter: SingleEmitter<WmUnitInfo>) {
                getEmitter = emitter
                sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadUnitSettingCmd())
            }
        })
    }

}