package com.sjbt.sdk.app

import android.os.Handler
import android.os.HandlerThread
import com.base.sdk.entity.apps.WmCameraFrameInfo
import com.base.sdk.port.app.AbAppCamera
import com.base.sdk.port.app.WMCameraFlashMode
import com.base.sdk.port.app.WMCameraPosition
import com.sjbt.sdk.MSG_INTERVAL_FRAME
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.H264FrameMap
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.DIVIDE_N_2
import com.sjbt.sdk.utils.LogUtils
import io.reactivex.rxjava3.core.*

class AppCamera(sjUniWatch: SJUniWatch) : AbAppCamera() {

    val sjUniWatch = sjUniWatch
    lateinit var cameraStateEmitter: ObservableEmitter<Boolean>
    lateinit var cameraFlashEmitter: ObservableEmitter<WMCameraFlashMode>
    lateinit var cameraFrontBackEmitter: ObservableEmitter<WMCameraPosition>
    lateinit var cameraBackSwitchEmitter: ObservableEmitter<WMCameraPosition>
    lateinit var cameraFlashSwitchEmitter: ObservableEmitter<WMCameraFlashMode>
    lateinit var cameraReadyEmitter: SingleEmitter<Boolean>

    //相机预览相关
    var mCameraFrameInfo: WmCameraFrameInfo? = null
    var mH264FrameMap: H264FrameMap = H264FrameMap()
    var mLatestIframeId: Long = 0
    var mLatestPframeId: Long = 0
    private lateinit var mCameraThread: HandlerThread
    private lateinit var mCameraHandler: Handler
    var needNewH264Frame = false
    var continueUpdateFrame: Boolean = false
    private var mCellLength = 0
    private var mTransferring = false
    private var mDivide: Byte = 0
    private var mOtaProcess = 0
    private var mFramePackageCount = 0
    private var mFrameLastLen = 0

    init {
        mCameraThread = HandlerThread("camera_send_thread")
    }

    open fun startCameraThread() {
        if (!mCameraThread.isAlive) {
            mCameraThread.start()
            mCameraHandler = Handler(mCameraThread.looper)
        }
    }

    open fun stopCameraThread() {
        if (mCameraThread.isAlive) {
            mCameraThread.interrupt()
        }
    }

    override fun isSupport(): Boolean {
        return true
    }

    override var observeCameraState: Observable<Boolean> =
        Observable.create(object : ObservableOnSubscribe<Boolean> {
            override fun subscribe(emitter: ObservableEmitter<Boolean>) {
                cameraStateEmitter = emitter
            }
        })

    override var observeCameraFlash: Observable<WMCameraFlashMode> =
        Observable.create(object : ObservableOnSubscribe<WMCameraFlashMode> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraFlashMode>) {
                cameraFlashEmitter = emitter
            }
        })

    override fun cameraFlashSwitch(type: WMCameraFlashMode): Observable<WMCameraFlashMode> {
        return Observable.create(object : ObservableOnSubscribe<WMCameraFlashMode> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraFlashMode>) {
                cameraFlashSwitchEmitter = emitter
            }
        })
    }

    override var observeCameraFrontBack: Observable<WMCameraPosition> =
        Observable.create(object : ObservableOnSubscribe<WMCameraPosition> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraPosition>) {
                cameraFrontBackEmitter = emitter
            }
        })

    override fun cameraBackSwitch(isBack: WMCameraPosition): Observable<WMCameraPosition> {
        return Observable.create(object:ObservableOnSubscribe<WMCameraPosition>{
            override fun subscribe(emitter: ObservableEmitter<WMCameraPosition>) {
                cameraBackSwitchEmitter = emitter
            }
        })
    }

    override fun isCameraPreviewEnable(): Boolean {
        return true
    }

    override fun isCameraPreviewReady(): Single<Boolean> {
        return Single.create(object : SingleOnSubscribe<Boolean> {
            override fun subscribe(emitter: SingleEmitter<Boolean>) {
                cameraReadyEmitter = emitter
            }
        })
    }

    override fun updateCameraPreview(cameraFrameInfo: WmCameraFrameInfo) {
        LogUtils.logBlueTooth("更新frame continueUpdateFrame：$continueUpdateFrame")

        if (cameraFrameInfo != null) {
            if (cameraFrameInfo.frameType === 2) {
                mLatestIframeId = cameraFrameInfo.frameId
                //                LogUtils.logBlueTooth("最新的I帧：" + mLatestIframeId);
            } else {
                mLatestPframeId = cameraFrameInfo.frameId
                //                LogUtils.logBlueTooth("最新的P帧：" + mLatestIframeId);
            }
            mH264FrameMap.putFrame(cameraFrameInfo)

//            LogUtils.logBlueTooth("来新数据了:" + needNewH264Frame);
            if (needNewH264Frame) {
                mCameraFrameInfo = cameraFrameInfo
                sendFrameDataAsync(cameraFrameInfo)
                needNewH264Frame = false
            }
        }
    }

    private fun sendFrameDataAsync(frameInfo: WmCameraFrameInfo?) {
        if (frameInfo == null) {
            needNewH264Frame = true
            LogUtils.logBlueTooth("没数据了-》2")
            return
        }
        mCameraHandler.post { sendFrameData(frameInfo) }
    }

    /**
     * 继续发送帧数据,分包,需要在单独的线程中执行
     *
     * @param cameraFrameInfo
     */
    private fun sendFrameData(cameraFrameInfo: WmCameraFrameInfo) {
        val dataArray: ByteArray = cameraFrameInfo.frameData
        mFramePackageCount = dataArray.size / mCellLength
        mFrameLastLen = dataArray.size % mCellLength
        if (mFrameLastLen != 0) {
            mFramePackageCount = mFramePackageCount + 1
        }
        if (mFramePackageCount > 0) {
            for (i in 0 until mFramePackageCount) {
                this.mOtaProcess = i
                if (dataArray == null || !continueUpdateFrame) {
                    break
                }

                try {
                    mTransferring = true
                    val info: OtaCmdInfo = sjUniWatch.getCameraPreviewCmdInfo(mFramePackageCount,mFrameLastLen,cameraFrameInfo, i)
                    //                    LogUtils.logBlueTooth("执行发送：" + info + " 分包类型：" + mDivide);
                    sjUniWatch.sendNormalMsg(CmdHelper.getCameraPreviewDataCmd02(info.payload, mDivide))
                    if (i != mFramePackageCount - 1) {
                        Thread.sleep(MSG_INTERVAL_FRAME)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mTransferring = false
                }
            }
        } else {
            mDivide = DIVIDE_N_2
            sjUniWatch.sendNormalMsg(CmdHelper.getCameraPreviewDataCmd02(dataArray, mDivide))
        }
    }


    override fun stopCameraPreview() {
        TODO("Not yet implemented")
    }
}