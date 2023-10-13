package com.sjbt.sdk.settings

import com.base.sdk.entity.common.WmNoDisturb
import com.base.sdk.entity.common.WmTimeFrequency
import com.base.sdk.entity.data.WmTimeRange
import com.base.sdk.entity.settings.WmSedentaryReminder
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_0
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingSedentaryReminder(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSedentaryReminder>(){
    private var observeEmitter: ObservableEmitter<WmSedentaryReminder>?=null
    private var setEmitter: SingleEmitter<WmSedentaryReminder>?=null
    private var getEmitter: SingleEmitter<WmSedentaryReminder>?=null

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSedentaryReminder> {
        return Observable.create(object : ObservableOnSubscribe<WmSedentaryReminder> {
            override fun subscribe(emitter: ObservableEmitter<WmSedentaryReminder>) {
                observeEmitter = emitter
            }
        })
    }

    override fun set(obj: WmSedentaryReminder): Single<WmSedentaryReminder> {
        return Single.create(object : SingleOnSubscribe<WmSedentaryReminder> {
            override fun subscribe(emitter: SingleEmitter<WmSedentaryReminder>) {
                setEmitter = emitter
                sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteSedentaryReminderCmd(obj))
            }
        })
    }

    override fun get(): Single<WmSedentaryReminder> {
        return Single.create(object : SingleOnSubscribe<WmSedentaryReminder> {
            override fun subscribe(emitter: SingleEmitter<WmSedentaryReminder>) {
                getEmitter = emitter
                sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadSedentaryReminderCmd())
            }
        })
    }

    fun sedentaryReminderBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {
                val byteBuffer =
                    ByteBuffer.wrap(it.data)

                val enabled = byteBuffer.get().toInt() == 1
                val startHour = byteBuffer.get()
                val startMinute = byteBuffer.get()
                val endHour = byteBuffer.get()
                val endMinute = byteBuffer.get()
                val frequency = byteBuffer.get()

                val timeFrequency = when (frequency.toInt()) {
                    0 -> {
                        WmTimeFrequency.EVERY_30_MINUTES
                    }
                    1 -> {
                        WmTimeFrequency.EVERY_1_HOUR
                    }
                    2 -> {
                        WmTimeFrequency.EVERY_1_HOUR_30_MINUTES
                    }
                    else -> {
                        WmTimeFrequency.EVERY_30_MINUTES
                    }
                }

                val timeRange = WmTimeRange(
                    startHour.toInt(),
                    startMinute.toInt(),
                    endHour.toInt(),
                    endMinute.toInt()
                )

                val noDisturbEnable = byteBuffer.get().toInt() == 1
                val noDisturbStartHour = byteBuffer.get()
                val noDisturbStartMinute = byteBuffer.get()
                val noDisturbEndHour = byteBuffer.get()
                val noDisturbEndMinute = byteBuffer.get()

                val noTimeRange = WmTimeRange(
                    noDisturbStartHour.toInt(),
                    noDisturbStartMinute.toInt(),
                    noDisturbEndHour.toInt(),
                    noDisturbEndMinute.toInt()
                )

                val noDisturb = WmNoDisturb(noDisturbEnable, noTimeRange)

                val sedentaryReminder = WmSedentaryReminder(enabled, timeRange, timeFrequency, noDisturb)

                getEmitter?.onSuccess(sedentaryReminder)
                observeEmitter?.onNext(sedentaryReminder)
            }
        }
    }

}