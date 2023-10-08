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
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AppCamera(sjUniWatch: SJUniWatch) : AbAppCamera() {

    val sjUniWatch = sjUniWatch
    lateinit var cameraObserveOpenEmitter: ObservableEmitter<Boolean>
    lateinit var cameraSingleOpenEmitter: SingleEmitter<Boolean>
    lateinit var cameraObserveTakePhotoEmitter: ObservableEmitter<Boolean>

    lateinit var cameraObserveFlashEmitter: ObservableEmitter<WMCameraFlashMode>
    lateinit var cameraObserveFrontBackEmitter: ObservableEmitter<WMCameraPosition>

    lateinit var cameraBackSwitchEmitter: ObservableEmitter<WMCameraPosition>
    lateinit var cameraFlashSwitchEmitter: ObservableEmitter<WMCameraFlashMode>
    lateinit var cameraPreviewReadyEmitter: SingleEmitter<Boolean>

    //相机预览相关
    var mCameraFrameInfo: WmCameraFrameInfo? = null
    var mH264FrameMap: H264FrameMap = H264FrameMap()
    var mLatestIframeId: Long = 0
    var mLatestPframeId: Long = 0
    private lateinit var mCameraThread: HandlerThread
    private lateinit var mCameraHandler: Handler
    var needNewH264Frame = false
    var continueUpdateFrame: Boolean = false
     var mCellLength = 0
     var mDivide: Byte = 0
     var mOtaProcess = 0
     var mFramePackageCount = 0
     var mFrameLastLen = 0

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

    override fun openCloseCamera(open: Boolean): Single<Boolean> {
        return Single.create(object : SingleOnSubscribe<Boolean> {
            override fun subscribe(emitter: SingleEmitter<Boolean>) {
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
        })
    }

    override var observeCameraOpenState: Observable<Boolean> =
        Observable.create(object : ObservableOnSubscribe<Boolean> {
            override fun subscribe(emitter: ObservableEmitter<Boolean>) {
                cameraObserveOpenEmitter = emitter
            }
        })

    override var observeCameraTakePhoto: Observable<Boolean> =
        Observable.create(object : ObservableOnSubscribe<Boolean> {
            override fun subscribe(emitter: ObservableEmitter<Boolean>) {
                cameraObserveTakePhotoEmitter = emitter
            }
        })

    override var observeCameraFlash: Observable<WMCameraFlashMode> =
        Observable.create(object : ObservableOnSubscribe<WMCameraFlashMode> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraFlashMode>) {
                cameraObserveFlashEmitter = emitter
            }
        })

    override var observeCameraFrontBack: Observable<WMCameraPosition> =
        Observable.create(object : ObservableOnSubscribe<WMCameraPosition> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraPosition>) {
                cameraObserveFrontBackEmitter = emitter
            }
        })

    override fun cameraFlashSwitch(wmCameraFlashMode: WMCameraFlashMode): Observable<WMCameraFlashMode> {
        return Observable.create(object : ObservableOnSubscribe<WMCameraFlashMode> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraFlashMode>) {
                cameraFlashSwitchEmitter = emitter

                sjUniWatch.sendNormalMsg(
                    CmdHelper.getCameraStateActionCmd(
                        1,
                        wmCameraFlashMode.ordinal.toByte()
                    )
                )
            }
        })
    }

    override fun cameraBackSwitch(wmCameraPosition: WMCameraPosition): Observable<WMCameraPosition> {
        return Observable.create(object : ObservableOnSubscribe<WMCameraPosition> {
            override fun subscribe(emitter: ObservableEmitter<WMCameraPosition>) {
                cameraBackSwitchEmitter = emitter
                sjUniWatch.sendNormalMsg(
                    CmdHelper.getCameraStateActionCmd(
                        0,
                        wmCameraPosition.ordinal.toByte()
                    )
                )
            }
        })
    }

    override fun isCameraPreviewEnable(): Boolean {
        return continueUpdateFrame
    }

    override fun isCameraPreviewReady(): Single<Boolean> {
        return Single.create(object : SingleOnSubscribe<Boolean> {
            override fun subscribe(emitter: SingleEmitter<Boolean>) {
                cameraPreviewReadyEmitter = emitter

                sjUniWatch.sendNormalMsg(
                    CmdHelper.getCameraPreviewCmd01()
                )
            }
        })
    }

    override fun updateCameraPreview(cameraFrameInfo: WmCameraFrameInfo) {
        LogUtils.logBlueTooth("更新frame continueUpdateFrame：$continueUpdateFrame")
        cameraFrameInfo?.let {
            if (it.frameType === 2) {
                mLatestIframeId = it.frameId
                LogUtils.logBlueTooth("最新的I帧：" + mLatestIframeId);
            } else {
                mLatestPframeId = it.frameId
                LogUtils.logBlueTooth("最新的P帧：" + mLatestIframeId)
            }
            mH264FrameMap.putFrame(it)

            LogUtils.logBlueTooth("来新数据了:" + needNewH264Frame);
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
                    val info: OtaCmdInfo = sjUniWatch.getCameraPreviewCmdInfo(
                        mFramePackageCount,
                        mFrameLastLen,
                        cameraFrameInfo,
                        i
                    )
                    //                    LogUtils.logBlueTooth("执行发送：" + info + " 分包类型：" + mDivide);
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
            sjUniWatch.sendNormalMsg(CmdHelper.getCameraPreviewDataCmd02(dataArray, mDivide))
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
        LogUtils.logBlueTooth("相机预览传输包大小：${mCellLength}")

        continueUpdateFrame = camera_pre_allow.toInt() == 1

        cameraPreviewReadyEmitter.onSuccess(continueUpdateFrame)

        LogUtils.logBlueTooth("是否支持相机预览 continueUpdateFrame：$continueUpdateFrame")

        if (camera_pre_allow.toInt() == 1) {
            LogUtils.logBlueTooth("预发送数据：" + mH264FrameMap.frameCount)
            if (!mH264FrameMap.isEmpty()) {
                LogUtils.logBlueTooth("发送的帧ID：${mLatestIframeId}")
                mCameraFrameInfo =
                    mH264FrameMap.getFrame(mLatestIframeId)
                LogUtils.logBlueTooth("发送的帧信息：${mCameraFrameInfo}")
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
                    LogUtils.logBlueTooth("没数据了-》1")
                    return
                }

                if (frameSuccess.toInt() == 1) { //发送成功
                    //删除掉已经发送成功之前的帧
                    mH264FrameMap.removeOldFrames(it.frameId)
                    LogUtils.logBlueTooth("移除发送过的帧数")
                    if (it.frameId === mLatestIframeId) {
                        mCameraFrameInfo =
                            mH264FrameMap.getFrame(mLatestPframeId)
                        LogUtils.logBlueTooth("没有新的I帧,发送最新的P帧：${mCameraFrameInfo}")
                    } else {
                        if (mLatestIframeId > it.frameId) {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestIframeId)
                            LogUtils.logBlueTooth("有新的I帧,发送最新的I帧：${mCameraFrameInfo}")
                        } else {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestPframeId)
                            LogUtils.logBlueTooth("没有新的I帧,发送最新的P帧：${mCameraFrameInfo}")
                        }
                    }
                } else { //发送失败
                    if (mCameraFrameInfo?.frameType === 0) {
                        if (mLatestIframeId > it.frameId) {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestIframeId)
                            LogUtils.logBlueTooth("P发送失败,发送最新的I帧：${mCameraFrameInfo}")
                        } else {
                            mCameraFrameInfo =
                                mH264FrameMap.getFrame(mLatestPframeId)
                            LogUtils.logBlueTooth("P发送失败,发送最新的P帧：${mCameraFrameInfo}")
                        }
                    } else {
                        mCameraFrameInfo =
                            mH264FrameMap.getFrame(mLatestIframeId)
                        LogUtils.logBlueTooth("发送失败,发送最新的I帧：${mCameraFrameInfo}")
                    }
                }

                sendFrameDataAsync(mCameraFrameInfo)
            } else {
                LogUtils.logBlueTooth("相机关闭，停止发送")
                mH264FrameMap.clear()
            }
        }
    }

    override fun stopCameraPreview() {
        continueUpdateFrame = false
        LogUtils.logBlueTooth("停止更新frame数据continueUpdateFrame：$continueUpdateFrame")
        mH264FrameMap.clear()
    }
}