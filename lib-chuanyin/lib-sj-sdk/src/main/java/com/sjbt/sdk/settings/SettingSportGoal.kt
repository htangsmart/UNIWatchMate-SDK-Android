package com.sjbt.sdk.settings

import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.port.setting.AbWmSetting
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.LogUtils
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer

class SettingSportGoal(sjUniWatch: SJUniWatch) : AbWmSetting<WmSportGoal>() {
    lateinit var observeEmitter: ObservableEmitter<WmSportGoal>
    lateinit var setEmitter: SingleEmitter<WmSportGoal>
    lateinit var getEmitter: SingleEmitter<WmSportGoal>

    val mSjUniWatch = sjUniWatch

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

                payloadPackage.itemList.forEach {
                    when (it.urn[0]) {
                        URN_1 -> {//蓝牙连接 暂用旧协议格式

                        }

                        URN_2 -> {//设置同步
                            when (it.urn[1]) {
                                URN_1 -> {//运动目标

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

                                            LogUtils.logBlueTooth("体育运动消息："+wmSportGoal)

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

                                URN_2 -> {//健康信息

                                }

                                URN_3 -> {//单位同步

                                }

                                URN_4 -> {//语言设置

                                }

                                URN_4 -> {//语言设置

                                }

                            }

                        }

                        URN_3 -> {//表盘

                        }

                        URN_4 -> {//应用

                        }

                        URN_5 -> {//运动同步

                        }
                    }
                }

                mSjUniWatch.sendNodeCmdList(payloadPackage)
            }
        })
    }

    override fun get(): Single<WmSportGoal> {
        return Single.create(object : SingleOnSubscribe<WmSportGoal> {
            override fun subscribe(emitter: SingleEmitter<WmSportGoal>) {
                getEmitter = emitter
            }
        })
    }
}