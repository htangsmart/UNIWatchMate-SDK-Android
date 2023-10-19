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

    override fun stopFindWatch(): Single<Boolean> {
        return Single.create {
            stopFindWatchEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteStopFindDevice())
        }
    }

    fun appFindBusiness(it: NodeData) {
        when (it.urn[2]) {

            URN_APP_FIND_DEVICE_START -> {
                val startResult = it.data[0].toInt()== ErrorCode.ERR_CODE_OK.ordinal
                startFindWatchEmitter?.onSuccess(startResult)
            }

            URN_APP_FIND_DEVICE_STOP -> {
                val stopResult = it.data[0].toInt()== ErrorCode.ERR_CODE_OK.ordinal
                stopFindWatchEmitter?.onSuccess(stopResult)
            }

            URN_APP_FIND_PHONE_START -> {
                sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getResponseStartFindPhone())

                val byteBuffer =
                    ByteBuffer.wrap(it.data)
                val count = byteBuffer.get().toInt()
                val timeSeconds = BtUtils.byte2short(it.data.copyOfRange(1, 3)).toInt()
                val wmFind = WmFind(count, timeSeconds)
                sjUniWatch.wmLog.logD(TAG, "findMobile: $wmFind")
                observeFindMobile.onNext(wmFind)
            }

            URN_APP_FIND_PHONE_STOP -> {
                sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getResponseStopFindPhone())

                val stopResult = it.data[0].toInt()== ErrorCode.ERR_CODE_OK.ordinal
                stopFindMobile.onNext(stopResult)
            }
        }
    }
}

