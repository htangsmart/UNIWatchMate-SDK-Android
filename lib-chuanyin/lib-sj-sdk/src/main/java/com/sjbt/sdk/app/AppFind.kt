package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmFind
import com.base.sdk.port.app.AbAppFind
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.subjects.PublishSubject
import java.nio.ByteBuffer

class AppFind(val sjUniWatch: SJUniWatch) : AbAppFind() {

    private var startFindWatchEmitter: SingleEmitter<Boolean>? = null
    private var stopFindWatchEmitter: SingleEmitter<Boolean>? = null
    private val TAG = "AppFind"

    override fun isSupport(): Boolean {
        return true
    }

    private val findMobile = PublishSubject.create<WmFind>()
    private val stopFindMobile = PublishSubject.create<Boolean>()
    private val mObserveStopFindMobile = PublishSubject.create<Any>()
    private val mObserveStopFindWatch = PublishSubject.create<Any>()
    override val observeFindMobile: PublishSubject<WmFind> = findMobile

    override fun stopFindMobile(): Observable<Boolean> {
        return stopFindMobile
    }

    override fun findWatch(wmFind: WmFind): Single<Boolean> {
        return Single.create {
            startFindWatchEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteStartFindDevice(wmFind))
        }
    }

    override val observeStopFindMobile: Observable<Any>
        get() = mObserveStopFindMobile

    override val observeStopFindWatch: Observable<Any>
        get() = mObserveStopFindWatch

    override fun stopFindWatch(): Single<Boolean> {
        return Single.create {
            stopFindWatchEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteStopFindDevice())
        }
    }

    fun appFindBusiness(it: NodeData) {

        when (it.urn[1]) {
            URN_APP_FIND_DEVICE -> {
                when (it.urn[2]) {

                    URN_APP_FIND_DEVICE_START -> {
                        val startResult = it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                        startFindWatchEmitter?.onSuccess(startResult)
                    }

                    URN_APP_FIND_DEVICE_STOP -> {
                        val stopResult = it.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                        stopFindWatchEmitter?.onSuccess(stopResult)
                    }
                }
            }

            URN_APP_FIND_PHONE -> {
                when (it.urn[2]) {

                    URN_APP_FIND_PHONE_START -> {
//                        sjUniWatch.sendResponseNodeCmdList(CmdHelper.getResponseStartFindPhone())
                        val byteBuffer =
                            ByteBuffer.wrap(it.data)
                        val count = byteBuffer.get().toInt()
                        val timeSeconds = BtUtils.byte2short(it.data.copyOfRange(1, 3)).toInt()
                        val wmFind = WmFind(count, timeSeconds)
                        sjUniWatch.wmLog.logD(TAG, "findMobile: $wmFind")
                        observeFindMobile.onNext(wmFind)
                    }

                    URN_APP_FIND_PHONE_STOP -> {
//                        sjUniWatch.sendResponseNodeCmdList(CmdHelper.getResponseStopFindPhone())
                        stopFindMobile.onNext(true)
                    }
                }
            }
        }

    }
}

