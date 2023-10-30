package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingSportGoal(val sjUniWatch: SJUniWatch) : AbWmSetting<WmSportGoal>() {
    private var observeEmitter: ObservableEmitter<WmSportGoal>? = null
    private var setEmitter: SingleEmitter<WmSportGoal>? = null
    private var getEmitter: SingleEmitter<WmSportGoal>? = null

    private var wmSportGoal: WmSportGoal? = null
    private val TAG = "SettingSportGoal"

    private var isGet = false

    override fun isSupport(): Boolean {
        return true
    }

    override fun observeChange(): Observable<WmSportGoal> {
        return Observable.create { emitter -> observeEmitter = emitter }
    }

    override fun set(obj: WmSportGoal): Single<WmSportGoal> {
        wmSportGoal = obj
        return Single.create { emitter ->
            setEmitter = emitter
            val payloadPackage = CmdHelper.getUpdateSportGoalAllCmd(obj)

            sjUniWatch.sendWriteNodeCmdList(payloadPackage)
        }
    }

    override fun get(): Single<WmSportGoal> {

        return Single.create { emitter ->
            isGet = true
            getEmitter = emitter
            sjUniWatch.sendReadNodeCmdList(CmdHelper.getDeviceSportGoalCmd())
        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {
    }

    fun sportInfoBusiness(it: NodeData) {
        when (it.urn[2]) {
            URN_0 -> {
                if (it.dataLen.toInt() == 1) {

                    if (it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal) {
                        setEmitter?.onSuccess(wmSportGoal)
                    } else {
                        setEmitter?.onError(RuntimeException("set fail"))
                    }

                } else {
                    val byteBuffer =
                        ByteBuffer.wrap(it.data)
                    val step = byteBuffer.getInt()
                    val distance = byteBuffer.getInt()
                    val calories = byteBuffer.getInt()
                    val activityDuration =
                        byteBuffer.getShort()

                    wmSportGoal = WmSportGoal(
                        step,
                        distance,
                        calories,
                        activityDuration
                    )

                    sjUniWatch.wmLog.logD(TAG, "sport goal：$wmSportGoal")

                    if (isGet) {
                        isGet = !isGet
                        getEmitter?.onSuccess(
                            wmSportGoal
                        )
                    }else{
                        observeEmitter?.onNext(wmSportGoal)
                    }

                }
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