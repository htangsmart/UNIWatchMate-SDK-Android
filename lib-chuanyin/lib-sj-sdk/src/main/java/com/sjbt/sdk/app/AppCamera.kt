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
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.BtUtils
import io.reactivex.rxjava3.core.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AppCamera(val sjUniWatch: SJUniWatch) : AbAppCamera() {

    var cameraObserveOpenEmitter: ObservableEmitter<Boolean>? = null
    var cameraSingleOpenEmitter: SingleEmitter<Boolean>? = null
    var cameraObserveTakePhotoEmitter: ObservableEmitter<Boolean>? = null
    var cameraObserveFlashEmitter: ObservableEmitter<WMCameraFlashMode>? = null
    var cameraObserveFrontBackEmitter: ObservableEmitter<WMCameraPosition>? = null
    var cameraBackSwitchEmitter: ObservableEmitter<WMCameraPosition>? = null
    var cameraFlashSwitchEmitter: ObservableEmitter<WMCameraFlashMode>? = null
    var cameraPreviewReadyEmitter: SingleEmitter<Boolean>? = null

    private var cameraStateObserver: Observable<Boolean>? = null

    private val TAG = "AppCamera"//相机预览相关
    private var mCameraFrameInfo: WmCameraFrameInfo? = null
    val mH264FrameMap: H264FrameMap = H264FrameMap()
    var continueUpdateFrame: Boolean = false

    private var mLatestIframeId: Long = 0
    private var mLatestPframeId: Long = 0
    private var mCameraThread: HandlerThread = HandlerThread("camera_send_thread")
    private lateinit var mCameraHandler: Handler
    private var needNewH264Frame = false
    private var mCellLength = 0
    private var mDivide: Byte = 0
    private var mOtaProcess = 0
    private var mFramePackageCount = 0
    private var mFrameLastLen = 0

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

    override fun openCloseCamera(open: Boolean): Single<Boolean> {

        return Single.create { emitter ->
            cameraSingleOpenEmitter = emitter
            sjUniWatch.sendNormalMsg(
                CmdHelper.getAppCallDeviceCmd(
                    if (open) {
                        1.toByte()
                    } else {
                        0.toByte()
                    }
                )
            )
        }
    }

    private fun getObservableCameraState(): Observable<Boolean> {
        if (cameraStateObserver == null || cameraObserveOpenEmitter == null || cameraObserveOpenEmitter!!.isDisposed) {
            cameraStateObserver =
                Observable.create { emitter -> cameraObserveOpenEmitter = emitter }
        }
        return cameraStateObserver!!
    }

    override var observeCameraOpenState: Observable<Boolean> = getObservableCameraState()

    override var observeCameraTakePhoto: Observable<Boolean> =
        Observable.create { emitter -> cameraObserveTakePhotoEmitter = emitter }

    override var observeCameraFlash: Observable<WMCameraFlashMode> =
        Observable.create { emitter -> cameraObserveFlashEmitter = emitter }

    override var observeCameraFrontBack: Observable<WMCameraPosition> =
        Observable.create { emitter -> cameraObserveFrontBackEmitter = emitter }

    override fun cameraFlashSwitch(wmCameraFlashMode: WMCameraFlashMode): Observable<WMCameraFlashMode> {
        return Observable.create { emitter ->
            cameraFlashSwitchEmitter = emitter

            sjUniWatch.sendNormalMsg(
                CmdHelper.getCameraStateActionCmd(
                    1,
                    wmCameraFlashMode.ordinal.toByte()
                )
            )
        }
    }

    override fun cameraBackSwitch(wmCameraPosition: WMCameraPosition): Observable<WMCameraPosition> {
        return Observable.create { emitter ->
            cameraBackSwitchEmitter = emitter
            sjUniWatch.sendNormalMsg(
                CmdHelper.getCameraStateActionCmd(
                    0,
                    wmCameraPosition.ordinal.toByte()
                )
            )
        }
    }

    override fun isCameraPreviewEnable(): Boolean {
        return continueUpdateFrame
    }

    override fun startCameraPreview(): Single<Boolean> {
        return Single.create { emitter ->
            cameraPreviewReadyEmitter = emitter

            sjUniWatch.sendNormalMsg(
                CmdHelper.getCameraPreviewCmd01()
            )
        }
    }

    override fun updateCameraPreview(cameraFrameInfo: WmCameraFrameInfo) {
        sjUniWatch.wmLog.logI(TAG, "更新frame continueUpdateFrame：$continueUpdateFrame")

        cameraFrameInfo?.let {
            if (it.frameType === 2) {
                mLatestIframeId = it.frameId
                sjUniWatch.wmLog.logI(TAG, "最新的I帧：" + mLatestIframeId);
            } else {
                mLatestPframeId = it.frameId
                sjUniWatch.wmLog.logI(TAG, "最新的P帧：" + mLatestIframeId)
            }
            mH264FrameMap.putFrame(it)

            sjUniWatch.wmLog.logI(TAG, "来新数据了:" + needNewH264Frame);
            if (needNewH264Frame) {
                mCameraFrameInfo = it
                sendFrameDataAsync(it)
                needNewH264Frame = false
            }
        }
    }

    fun sendFrameDataAsync(frameInfo: WmCameraFrameInfo?) {
        if (frameInfo == null) {
            needNewH264Frame = true
            sjUniWatch.wmLog.logI(TAG, "没数据了-》2")
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
        val dataArray: ByteArray? = cameraFrameInfo.frameData
        dataArray?.let {
            mFramePackageCount = it.size / mCellLength
            mFrameLastLen = it.size % mCellLength
            if (mFrameLastLen != 0) {
                mFramePackageCount = mFramePackageCount + 1
            }
            if (mFramePackageCount > 0) {
                for (i in 0 until mFramePackageCount) {
                    this.mOtaProcess = i
                    if (it == null || !continueUpdateFrame) {
                        break
                    }

                    try {
                        val info: OtaCmdInfo = getCameraPreviewCmdInfo(
                            mFramePackageCount,
                            mFrameLastLen,
                            cameraFrameInfo,
                            i
                        )
                        //                    sjUniWatch.wmLog.logI(TAG,"执行发送：" + info + " 分包类型：" + mDivide);
                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getCameraPreviewDataCmd02(
                                info.payload,
                                mDivide
                            )
                        )
                        if (i != mFramePackageCount - 1) {
                            Thread.sleep(MSG_INTERVAL_FRAME)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                mDivide = DIVIDE_N_2
                sjUniWatch.sendNormalMsg(CmdHelper.getCameraPreviewDataCmd02(it, mDivide))
            }
        }

    }

    fun cameraPreviewBuz(msg: ByteArray) {
        val byteBuffer =
            ByteBuffer.wrap(msg).order(ByteOrder.LITTLE_ENDIAN)

        val camera_pre_allow = byteBuffer[16] //是否容许同步画面 0允许 1不允许

        val reason = byteBuffer[17]
        val lenArray = ByteArray(4)

        System.arraycopy(msg, 18, lenArray, 0, lenArray.size)

        mOtaProcess = 0
        mCellLength =
            ByteBuffer.wrap(lenArray).order(ByteOrder.LITTLE_ENDIAN).int

        mCellLength = mCellLength - 5
        sjUniWatch.wmLog.logI(TAG, "相机预览传输包大小：${mCellLength}")

        continueUpdateFrame = camera_pre_allow.toInt() == 1

        cameraPreviewReadyEmitter?.onSuccess(continueUpdateFrame)

        sjUniWatch.wmLog.logI(TAG, "是否支持相机预览 continueUpdateFrame：$continueUpdateFrame")

        if (camera_pre_allow.toInt() == 1) {
            sjUniWatch.wmLog.logI(TAG, "预发送数据：" + mH264FrameMap.frameCount)
            if (!mH264FrameMap.isEmpty()) {
                sjUniWatch.wmLog.logI(TAG, "发送的帧ID：${mLatestIframeId}")
                mCameraFrameInfo =
                    mH264FrameMap.getFrame(mLatestIframeId)
                sjUniWatch.wmLog.logI(TAG, "发送的帧信息：${mCameraFrameInfo}")
                sendFrameDataAsync(mCameraFrameInfo)
            } else {
                needNewH264Frame = true
            }
        }
    }

    fun sendFrameData03(frameSuccess: Byte) {
        mCameraFrameInfo?.let {
            if (continueUpdateFrame) {
                if (mH264FrameMap.isEmpty()) {
                    needNewH264Frame = true
                    sjUniWatch.wmLog.logI(TAG, "没数据了-》1")
                    return
                }

                if (frameSuccess.toInt() == 1) { //发送成功
                    //删除掉已经发送成功之前的帧
                    mH264FrameMap.removeOldFrames(it.frameId)
                    sjUniWatch.wmLog.logI(TAG, "移除发送过的帧数")
                    if (it.frameId === mLatestIframeId) {
                        mCameraFrameInfo =
                            mH264FrameMap.getFrame(mLatestPframeId)
                        sjUniWatch.wmLog.logI(TAG, "没有新的I帧,发送最新的P帧：${mCameraFrameInfo}")
                    } else {
                        if (mLatestIframeId > it.frameId) {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestIframeId)
                            sjUniWatch.wmLog.logI(TAG, "有新的I帧,发送最新的I帧：${mCameraFrameInfo}")
                        } else {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestPframeId)
                            sjUniWatch.wmLog.logI(TAG, "没有新的I帧,发送最新的P帧：${mCameraFrameInfo}")
                        }
                    }
                } else { //发送失败
                    if (mCameraFrameInfo?.frameType === 0) {
                        if (mLatestIframeId > it.frameId) {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestIframeId)
                            sjUniWatch.wmLog.logI(TAG, "P发送失败,发送最新的I帧：${mCameraFrameInfo}")
                        } else {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestPframeId)
                            sjUniWatch.wmLog.logI(TAG, "P发送失败,发送最新的P帧：${mCameraFrameInfo}")
                        }
                    } else {
                        mCameraFrameInfo =
                            mH264FrameMap.getFrame(mLatestIframeId)
                        sjUniWatch.wmLog.logI(TAG, "发送失败,发送最新的I帧：${mCameraFrameInfo}")
                    }
                }

                sendFrameDataAsync(mCameraFrameInfo)
            } else {
                sjUniWatch.wmLog.logI(TAG, "相机关闭，停止发送")
                mH264FrameMap.clear()
            }
        }
    }

    override fun stopCameraPreview() {
        continueUpdateFrame = false
        sjUniWatch.wmLog.logI(TAG, "停止更新frame数据continueUpdateFrame：$continueUpdateFrame")
        mH264FrameMap.clear()
    }


    private fun getCameraPreviewCmdInfo(
        mFramePackageCount: Int,
        mFrameLastLen: Int,
        frameInfo: WmCameraFrameInfo,
        i: Int
    ): OtaCmdInfo {
        val info = OtaCmdInfo()
        val dataArray: ByteArray? = frameInfo.frameData

        dataArray?.let {
            if (i == 0 && mFramePackageCount > 1) {
                mDivide = DIVIDE_Y_F_2
            } else {
                if (mFramePackageCount == 1) {
                    mDivide = DIVIDE_N_2
                } else if (i == mFramePackageCount - 1) {
                    mDivide = DIVIDE_Y_E_2
                } else {
                    mDivide = DIVIDE_Y_M_2
                }
            }

//        sjUniWatch.wmLog.logI(TAG,"分包类型：" + mDivide);
            if (i == mFramePackageCount - 1 && mDivide != DIVIDE_N_2) {
//            sjUniWatch.wmLog.logI(TAG,"最后一包长度：" + mFrameLastLen);
                if (mFrameLastLen == 0) {
                    info.offSet = i * mCellLength
                    info.payload = ByteArray(mCellLength)
                    System.arraycopy(it, i * mCellLength, info.payload, 0, info.payload.size)
                } else {
                    info.offSet = i * mCellLength
                    info.payload = ByteArray(mFrameLastLen)
                    System.arraycopy(it, i * mCellLength, info.payload, 0, info.payload.size)
                }
            } else {
                info.offSet = i * mCellLength
                if (mDivide == DIVIDE_Y_F_2 || mDivide == DIVIDE_N_2) { //首包或者不分包的时候需要传帧大小
                    sjUniWatch.wmLog.logI(TAG, "本帧大小:" + it.size)
                    sjUniWatch.wmLog.logI(TAG, "帧数据长度：" + BtUtils.intToHex(it.size))
                    if (it.size < mCellLength) {
                        sjUniWatch.wmLog.logI(TAG, "不分包：$mDivide")
                        mCellLength = it.size
                    }
                    val byteBuffer =
                        ByteBuffer.allocate(mCellLength + 5).order(ByteOrder.LITTLE_ENDIAN)
                    byteBuffer.put(frameInfo.frameType.toByte())
                    byteBuffer.putInt(it.size)
                    val payload = ByteArray(mCellLength)
                    System.arraycopy(it, 0, payload, 0, payload.size)
                    sjUniWatch.wmLog.logI(TAG, "数据payload：" + payload.size)
                    byteBuffer.put(payload)
                    info.payload = byteBuffer.array()
                    sjUniWatch.wmLog.logI(TAG, "首包payload总长度：" + info.payload.size)
                } else {
                    info.payload = ByteArray(mCellLength)
                    System.arraycopy(it, i * mCellLength, info.payload, 0, info.payload.size)
                }
            }
        }

        return info
    }
}