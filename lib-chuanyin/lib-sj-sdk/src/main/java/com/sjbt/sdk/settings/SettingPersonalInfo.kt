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

class SettingPersonalInfo(sjUniWatch: SJUniWatch) : AbWmSetting<WmPersonalInfo>() {
    private var observeEmitter: ObservableEmitter<WmPersonalInfo>? = null
    private var setEmitter: SingleEmitter<WmPersonalInfo>? = null
    private var getEmitter: SingleEmitter<WmPersonalInfo>? = null

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
                val height = byteBuffer.getShort()
                val weight = byteBuffer.getShort()
                val gender = byteBuffer.get()

                val year = byteBuffer.getShort()
                val month = byteBuffer.get()
                val day = byteBuffer.get()

                val personalInfo = WmPersonalInfo(
                    height,
                    weight,
                    if (gender.toInt() == 1) {
                        WmPersonalInfo.Gender.MALE
                    } else {
                        WmPersonalInfo.Gender.FEMALE
                    },
                    WmPersonalInfo.BirthDate(year, month, day)
                )

                getEmitter?.onSuccess(personalInfo)
                observeEmitter?.onNext(personalInfo)
            }
        }
    }

}