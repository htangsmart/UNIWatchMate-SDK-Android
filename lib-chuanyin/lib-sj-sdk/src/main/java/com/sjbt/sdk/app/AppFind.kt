package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmFind
import com.base.sdk.port.app.AbAppFind
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.entity.ResponseResultType
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.URN_APP_FIND_DEVICE_START
import com.sjbt.sdk.spp.cmd.URN_APP_FIND_DEVICE_STOP
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class AppFind(val sjUniWatch: SJUniWatch) : AbAppFind() {

    var startFindPhoneEmitter: ObservableEmitter<WmFind>? = null
    var stopFindPhoneEmitter: ObservableEmitter<Boolean>? = null
    var startFindWatchEmitter: SingleEmitter<Boolean>? = null
    var stopFindWatchEmitter: SingleEmitter<Boolean>? = null

    override fun isSupport(): Boolean {
        return true
    }

    override var observeFindMobile: Observable<WmFind> = Observable.create{
        startFindPhoneEmitter = it
    }


    override fun stopFindMobile(): Observable<Boolean> {
        return Observable.create {
            stopFindPhoneEmitter = it
        }

    }

    override fun findWatch(wmFind: WmFind): Single<Boolean> {
        return Single.create {
            startFindWatchEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteStartFindDevice(wmFind))

        }
    }

    override fun stopFindWatch(): Single<Boolean> {
        return Single.create {
            stopFindWatchEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteStopFindDevice())
        }
    }

    fun appFindBusiness(it: NodeData) {
        when (it.urn[2]) {

            URN_APP_FIND_DEVICE_START -> {
                when (it.data[0].toInt()) {
                    ResponseResultType.RESPONSE_EACH.type -> {
                        startFindWatchEmitter?.onSuccess(true)
                    }

                    ResponseResultType.RESPONSE_ALL_OK.type -> {
                        startFindWatchEmitter?.onSuccess(true)
                    }

                    ResponseResultType.RESPONSE_ALL_FAIL.type -> {
                        startFindWatchEmitter?.onSuccess(false)
                    }
                }
            }

            URN_APP_FIND_DEVICE_STOP -> {

            }
        }
    }
}