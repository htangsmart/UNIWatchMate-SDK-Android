package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSedentaryReminder
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.*

class SettingSedentaryReminder(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSedentaryReminder>(){
    lateinit var observeEmitter: ObservableEmitter<WmSedentaryReminder>
    lateinit var setEmitter: SingleEmitter<WmSedentaryReminder>
    lateinit var getEmitter: SingleEmitter<WmSedentaryReminder>

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

}