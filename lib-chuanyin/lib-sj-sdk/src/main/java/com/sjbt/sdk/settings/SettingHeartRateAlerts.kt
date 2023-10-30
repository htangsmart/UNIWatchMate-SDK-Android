package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingHeartRateAlerts(val sjUniWatch: SJUniWatch) : AbWmSetting<WmHeartRateAlerts>() {
    var observeEmitter: ObservableEmitter<WmHeartRateAlerts>? = null
    var setEmitter: SingleEmitter<WmHeartRateAlerts>? = null
    var getEmitter: SingleEmitter<WmHeartRateAlerts>? = null

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmHeartRateAlerts> {
        return Observable.create { emitter ->
            observeEmitter = emitter
        }
    }

    override fun set(obj: WmHeartRateAlerts): Single<WmHeartRateAlerts> {
        return Single.create { emitter ->
            setEmitter = emitter
            sjUniWatch.sendWriteNodeCmdList(getWriteRateSettingPayLoad(obj))
        }
    }

    override fun get(): Single<WmHeartRateAlerts> {
        return Single.create { emitter ->
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(getReadRateSettingPayload())
        }
    }

    /**
     * 获取体育列表
     */
    private fun getReadRateSettingPayload(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING, URN_APP_RATE
            ), ByteArray(0)
        )
        return payloadPackage
    }

    /**
     * 获取写入心率设置的命令
     */
    private fun getWriteRateSettingPayLoad(heartRateAlerts: WmHeartRateAlerts): PayloadPackage {
        val payloadPackage = PayloadPackage()

        val byteBuffer = ByteBuffer.allocate(6)
        byteBuffer.put(
            if (heartRateAlerts.isEnableHrAutoMeasure) {
                1
            } else {
                0
            }
        )
        byteBuffer.put(heartRateAlerts.maxHeartRate.toByte())

        byteBuffer.put(
            if (heartRateAlerts.exerciseHeartRateAlert.isEnable) {
                1
            } else {
                0
            }
        )
        byteBuffer.put(heartRateAlerts.exerciseHeartRateAlert.threshold.toByte())

        byteBuffer.put(
            if (heartRateAlerts.restingHeartRateAlert.isEnable) {
                1
            } else {
                0
            }
        )
        byteBuffer.put(heartRateAlerts.restingHeartRateAlert.threshold.toByte())

        payloadPackage.putData(
            CmdHelper.getUrnId(URN_APP_SETTING, URN_APP_RATE),
            byteBuffer.array()
        )

        return payloadPackage
    }


    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {

    }

    fun settingHeartRateBusiness(nodeData: NodeData){

    }
}