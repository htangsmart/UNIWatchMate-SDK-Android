package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmPersonalInfo
import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.RequestType
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_0
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

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

                sjUniWatch.sendWriteNodeCmdList(CmdHelper.getUpdatePersonalInfoAllCmd(obj))
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

     fun personalInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {

                val byteBuffer =
                    ByteBuffer.wrap(it.data)

                val personalInfo=WmPersonalInfo(180,80, WmPersonalInfo.Gender.FEMALE,WmPersonalInfo.BirthDate(20,0,0))
                getEmitter.onSuccess(personalInfo)

            }
        }
    }

}