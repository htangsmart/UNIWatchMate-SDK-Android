package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.RequestType
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingSportGoal(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSportGoal>() {
    private var observeEmitter: ObservableEmitter<WmSportGoal>? = null
    private var setEmitter: SingleEmitter<WmSportGoal>? = null
    private var getEmitter: SingleEmitter<WmSportGoal>? = null

    private val TAG = "SettingSportGoal"

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

                sjUniWatch.sendWriteNodeCmdList(payloadPackage)
            }
        })
    }

    override fun get(): Single<WmSportGoal> {
        return Single.create(object : SingleOnSubscribe<WmSportGoal> {
            override fun subscribe(emitter: SingleEmitter<WmSportGoal>) {
                getEmitter = emitter

                sjUniWatch.sendReadNodeCmdList(CmdHelper.getDeviceSportGoalCmd())
            }
        })
    }

    fun sportInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {
                val byteBuffer =
                    ByteBuffer.wrap(it.data)
                val step = byteBuffer.getInt()
                val distance = byteBuffer.getInt()
                val calories = byteBuffer.getInt()
                val activityDuration =
                    byteBuffer.getShort()

                val wmSportGoal = WmSportGoal(
                    step,
                    distance,
                    calories,
                    activityDuration
                )

                sjUniWatch.wmLog.logI(TAG, "体育运动消息：" + wmSportGoal)

                setEmitter?.onSuccess(
                    wmSportGoal
                )
            }

            URN_1 -> {//步数

            }
            URN_2 -> {//热量（卡）

            }
            URN_3 -> {//距离（米）

            }
            URN_4 -> {//活动时长（分钟）

            }
        }
    }
}