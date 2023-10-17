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

class SettingPersonalInfo(val sjUniWatch: SJUniWatch) : AbWmSetting<WmPersonalInfo>() {
    private var observeEmitter: ObservableEmitter<WmPersonalInfo>? = null
    private var setEmitter: SingleEmitter<WmPersonalInfo>? = null
    private var getEmitter: SingleEmitter<WmPersonalInfo>? = null

    private var personalInfo: WmPersonalInfo? = null
    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmPersonalInfo> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmPersonalInfo): Single<WmPersonalInfo> {
        personalInfo = obj
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getUpdatePersonalInfoAllCmd(obj))
        }
    }

    override fun get(): Single<WmPersonalInfo> {
        return Single.create { emitter ->
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(CmdHelper.getDevicePersonalInfoCmd())
        }
    }

    fun personalInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {

                if (it.data.size <= 1) {
                    personalInfo?.let {
                        setEmitter?.onSuccess(it)
                    }

                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data)
                    val height = byteBuffer.getShort()
                    val weight = byteBuffer.getShort()
                    val gender = byteBuffer.get()

                    val year = byteBuffer.getShort()
                    val month = byteBuffer.get()
                    val day = byteBuffer.get()

                    personalInfo = WmPersonalInfo(
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

}