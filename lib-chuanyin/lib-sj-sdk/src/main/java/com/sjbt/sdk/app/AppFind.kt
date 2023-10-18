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

    private var startFindPhoneEmitter: ObservableEmitter<WmFind>? = null
    private var stopFindPhoneEmitter: ObservableEmitter<Boolean>? = null
    private var startFindWatchEmitter: SingleEmitter<Boolean>? = null
    private var stopFindWatchEmitter: SingleEmitter<Boolean>? = null

    private var observableFindPhone: Observable<WmFind>? = null
    private var observableStopFindPhone: Observable<Boolean>? = null

    override fun isSupport(): Boolean {
        return true
    }

    private fun getObservableFindPhone(): Observable<WmFind> {
        if (observableFindPhone == null || startFindPhoneEmitter == null || startFindPhoneEmitter!!.isDisposed) {
            observableFindPhone = Observable.create {
                startFindPhoneEmitter = it
            }
        } else {
            observableFindPhone
        }

        return observableFindPhone!!
    }

    private fun getObservableStopFindPhone(): Observable<Boolean> {
        if (observableStopFindPhone == null || stopFindPhoneEmitter == null || stopFindPhoneEmitter!!.isDisposed) {
            observableStopFindPhone = Observable.create {
                stopFindPhoneEmitter = it
            }
        } else {
            observableStopFindPhone
        }

        return observableStopFindPhone!!
    }

    override var observeFindMobile: Observable<WmFind> = getObservableFindPhone()
    override fun stopFindMobile(): Observable<Boolean> = getObservableStopFindPhone()

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
                stopFindWatchEmitter?.onSuccess(true)
            }
        }
    }
}