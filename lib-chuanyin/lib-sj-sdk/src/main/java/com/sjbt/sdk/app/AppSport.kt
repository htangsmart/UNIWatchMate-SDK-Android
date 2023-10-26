package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmSport
import com.base.sdk.port.app.AbAppSport
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

/**
 * 应用 - 运动 列表获取和更新
 */
class AppSport(val sjUniWatch: SJUniWatch) : AbAppSport() {
    private var getSportListEmitter: SingleEmitter<List<WmSport>>? = null

    override fun isSupport(): Boolean {
        return true
    }

    override var getSportList: Single<List<WmSport>> = Single.create {
        getSportListEmitter = it
        sjUniWatch.sendReadNodeCmdList(getSportListPayloadPackage())
    }

    /**
     * 获取体育列表
     */
    private fun getSportListPayloadPackage(): PayloadPackage {
        val payloadPackage = PayloadPackage()
        payloadPackage.putData(
            CmdHelper.getUrnId(
                URN_APP_SETTING,
                URN_APP_SPORT,
                URN_APP_SPORT_LIST
            ), ByteArray(0)
        )
        return payloadPackage
    }

    override fun updateSportList(list: List<WmSport>): Single<Boolean> {
        return Single.create {

        }
    }

    fun onTimeOut(msgBean: MsgBean, nodeData: NodeData) {

    }


}