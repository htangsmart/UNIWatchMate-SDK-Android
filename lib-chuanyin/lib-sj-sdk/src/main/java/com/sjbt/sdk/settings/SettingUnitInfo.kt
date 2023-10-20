package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmUnitInfo
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_0
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingUnitInfo(val sjUniWatch: SJUniWatch) : AbWmSetting<WmUnitInfo>() {
    private var observeEmitter: ObservableEmitter<WmUnitInfo>? = null
    private var setEmitter: SingleEmitter<WmUnitInfo>? = null
    private var getEmitter: SingleEmitter<WmUnitInfo>? = null

    private var wmUnitInfo: WmUnitInfo? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmUnitInfo> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmUnitInfo): Single<WmUnitInfo> {
        wmUnitInfo = obj
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteUnitSettingCmd(obj))
        }
    }

    override fun get(): Single<WmUnitInfo> {
        return Single.create { emitter ->
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadUnitSettingCmd())
        }
    }

    fun unitInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {

                if (it.dataLen.toInt() == 1) {
                    setEmitter?.onSuccess(
                        if (it.data[0] == ErrorCode.ERR_CODE_OK.ordinal.toByte()) {
                            wmUnitInfo
                        } else {
                            null
                        }
                    )
                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data)

                    val timeUnit = byteBuffer.get()
                    val distanceUnit = byteBuffer.get()
                    val temperatureUnit = byteBuffer.get()
                    val weightUnit = byteBuffer.get()

                    wmUnitInfo = WmUnitInfo(
                        weightUnit = if (weightUnit.toInt() == 0) {
                            WmUnitInfo.WeightUnit.KG
                        } else {
                            WmUnitInfo.WeightUnit.LB
                        },
                        temperatureUnit = if (temperatureUnit.toInt() == 0) {
                            WmUnitInfo.TemperatureUnit.CELSIUS
                        } else {
                            WmUnitInfo.TemperatureUnit.FAHRENHEIT
                        },
                        timeFormat = if (timeUnit.toInt() == 0) {
                            WmUnitInfo.TimeFormat.TWELVE_HOUR
                        } else {
                            WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR
                        },
                        distanceUnit = if (distanceUnit.toInt() == 0) {
                            WmUnitInfo.DistanceUnit.KM
                        } else {
                            WmUnitInfo.DistanceUnit.MILE
                        }
                    )

                    getEmitter?.onSuccess(wmUnitInfo)
                    observeEmitter?.onNext(wmUnitInfo)
                }
            }
        }
    }

}