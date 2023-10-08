package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmPersonalInfo
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.RequestType
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingPersonalInfo(sjUniWatch: SJUniWatch): AbWmSetting<WmPersonalInfo>(){
    lateinit var observeEmitter: ObservableEmitter<WmPersonalInfo>
    lateinit var setEmitter: SingleEmitter<WmPersonalInfo>
    lateinit var getEmitter: SingleEmitter<WmPersonalInfo>

    private val sjUniWatch = sjUniWatch

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmPersonalInfo> {
        return Observable.create(object : ObservableOnSubscribe<WmPersonalInfo> {
            override fun subscribe(emitter: ObservableEmitter<WmPersonalInfo>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmPersonalInfo): Single<WmPersonalInfo> {
        return Single.create(object : SingleOnSubscribe<WmPersonalInfo> {
            override fun subscribe(emitter: SingleEmitter<WmPersonalInfo>) {
                setEmitter = emitter

                sjUniWatch.sendNodeCmdList(RequestType.REQ_TYPE_WRITE,CmdHelper.getUpdatePersonalInfoAllCmd(obj))
            }
        })
    }

    override fun get(): Single<WmPersonalInfo> {
        return Single.create(object : SingleOnSubscribe<WmPersonalInfo> {
            override fun subscribe(emitter: SingleEmitter<WmPersonalInfo>) {
                getEmitter = emitter
            }
        })
    }

}