package com.sjbt.sdk.dfu

import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.FileType
import com.base.sdk.port.State
import com.base.sdk.port.WmTransferState
import com.sjbt.sdk.MAX_RETRY_COUNT
import com.sjbt.sdk.MSG_INTERVAL
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.utils.FileUtils
import com.sjbt.sdk.utils.LogUtils
import com.sjbt.sdk.utils.readFileBytes
import io.reactivex.rxjava3.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SJTransferFile(sjUniWatch: SJUniWatch) : AbWmTransferFile() {

    private val sjUniWatch = sjUniWatch

    //文件传输相关
    private var mTransferFiles: List<File>? = null
    private var mFileDataArray: ByteArray? = null
    private var mSendingFile: File? = null

    private var mSelectFileCount = 0
    private var mSendFileCount = 0
    private var mCellLength = 0
    private var mOtaProcess = 0
    private var mCanceledSend = false
    private var mErrorSend: Boolean = false
    private var mDivide: Byte = 0
    private var mPackageCount = 0
    private var mLastDataLength: Int = 0
    private var mTransferRetryCount = 0
    private var mTransferring = false

    val mSportMap = HashMap<FileType, Boolean>()
    lateinit var cancelTransfer: SingleEmitter<Boolean>
    lateinit var observableTransferEmitter: ObservableEmitter<WmTransferState>

    override fun isSupport(fileType: FileType): Boolean {
        return mSportMap[fileType] == true
    }

    override fun cancelTransfer(): Single<Boolean> {
        return Single.create(object : SingleOnSubscribe<Boolean> {
            override fun subscribe(emitter: SingleEmitter<Boolean>) {
                cancelTransfer = emitter

                sjUniWatch.sendNormalMsg(CmdHelper.transferCancelCmd)
            }
        })
    }

    fun transferEnd() {
        try {
//            mBtEngine.clearMsgQueue()
            mOtaProcess = 0
            mTransferRetryCount = 0
            mTransferring = false
            mSendFileCount = 0
//            removeCallBackRunner(mTransferTimeoutRunner)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun startTransfer(fileType: FileType, files: List<File>): Observable<WmTransferState> {
        return Observable.create(object : ObservableOnSubscribe<WmTransferState> {
            override fun subscribe(emitter: ObservableEmitter<WmTransferState>) {
                observableTransferEmitter = emitter

                var fileLen: Long = 0

                files.forEach {
                    fileLen += it.length()
                }

                sjUniWatch.sendNormalMsg(
                    CmdHelper.getTransferFile01Cmd(
                        fileType.ordinal.toByte(),
                        fileLen.toInt(),
                        files.size
                    )
                )
            }
        })
    }

    fun transferFileBuz(msgBean: MsgBean, msg: ByteArray) {
        when (msgBean.cmdId.toShort()) {
            CMD_ID_8001 -> {
                mSendFileCount = 0
                mErrorSend = false
                val byteBuffer = ByteBuffer.wrap(msg)
                val ota_allow = byteBuffer[16] //是否容许升级 0允许 1不允许
                val reason = byteBuffer[17] //是否容许升级 0允许 1不允许
                LogUtils.logBle("1.允许传输:$ota_allow")
                if (ota_allow.toInt() == 1) {
                    mSendingFile = mTransferFiles!![0]

                    mSendingFile?.let { file ->
                        file.readFileBytes()?.let {
                            sjUniWatch.sendNormalMsg(
                                CmdHelper.getTransferFile02Cmd(
                                    it.size,
                                    file.name
                                )
                            )
                        }

                    }?.let {
                        observableTransferEmitter.onError(
                            RuntimeException("error file list , resaon:-1")
                        )
                    }

                } else {
                    observableTransferEmitter.onError(
                        RuntimeException("device not allow transfer file , resaon:$reason")
                    )
                }
            }
            CMD_ID_8002 -> {
                val lenArray = ByteArray(4)
                System.arraycopy(msg, 16, lenArray, 0, lenArray.size)
                mOtaProcess = 0
                mCellLength = ByteBuffer.wrap(lenArray)
                    .order(ByteOrder.LITTLE_ENDIAN).int - 4
                LogUtils.logBlueTooth("cell_length:$mCellLength")
                if (mCellLength != 0) {

                    GlobalScope.launch {
                        // 在后台线程执行耗时操作
                        withContext(Dispatchers.IO) {
                            // 执行耗时操作
                            mFileDataArray =
                                FileUtils.readFileBytes(mTransferFiles!![mSendFileCount])

                            mFileDataArray?.let {
                                continueSendFileData(0, it)
                            }?.let {
                                observableTransferEmitter.onError(
                                    RuntimeException("transfer file fail,reason: -1")
                                )
                            }
                        }
                        // 操作完成后在主线程更新 UI
                        withContext(Dispatchers.Main) {
                            // 更新 UI
                        }
                    }

                } else {
                    mCanceledSend = true
                    transferEnd()
                    observableTransferEmitter.onError(
                        RuntimeException("transfer file fail,reason: cmd 02")
                    )
                }
            }

            CMD_ID_8003 -> {
                val buffer = ByteBuffer.wrap(msg).order(ByteOrder.LITTLE_ENDIAN)
                val isRight = buffer[16]
                mTransferring = true
                mErrorSend = isRight.toInt() != 1
                mOtaProcess = buffer.getInt(17)
                LogUtils.logBlueTooth("返回消息状态:$mErrorSend 返回ota_process:$mOtaProcess 总包个数:$mPackageCount")
                if (mErrorSend) { //失败
                    //                                        removeCallBackRunner(mTransferTimeoutRunner)
                    //                                        mOtaProcess = mOtaProcess > 0 ? mOtaProcess - 1 : mOtaProcess;
                    LogUtils.logBlueTooth("出错后序号：$mOtaProcess")
                    if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        sendErrorMsg(mOtaProcess)
                    } else {
                        transferEnd()
                    }
                } else { //成功
                    mTransferRetryCount = 0
                    LogUtils.logBlueTooth("掰正的消息：$mOtaProcess")

                    GlobalScope.launch {
                        // 在后台线程执行耗时操作
                        withContext(Dispatchers.IO) {
                            // 执行耗时操作
                            mFileDataArray =
                                FileUtils.readFileBytes(mTransferFiles!![mSendFileCount])

                            mFileDataArray?.let {

                                if (mOtaProcess.toInt() != mPackageCount - 1) {
                                    mOtaProcess++
                                    continueSendFileData(
                                        mOtaProcess,
                                        it
                                    )
                                }

                            }?.let {
                                observableTransferEmitter.onError(
                                    RuntimeException("transfer file fail,reason: -1")
                                )
                            }


                        }
                        // 操作完成后在主线程更新 UI
                        withContext(Dispatchers.Main) {
                            // 更新 UI
                        }
                    }


                }
            }

            CMD_ID_8004 -> {
                mTransferRetryCount = 0
                mOtaProcess = 0
                //                                    removeCallBackRunner(mTransferTimeoutRunner)
                val data_success = ByteBuffer.wrap(msg)[16]
                LogUtils.logBlueTooth("发送结果:$data_success")
                sjUniWatch.clearMsg()
                if (data_success == 1.toByte()) {
                    mSendFileCount++
                    if (mSendFileCount >= mTransferFiles!!.size) {
                        mTransferring = false

                        val transferState = WmTransferState(
                            State.ALL_FINISH,
                            true,
                            mTransferFiles!!.size,
                            mSendingFile!!
                        )

                        transferState.progress = 100
                        transferState.index = mSendFileCount

                        observableTransferEmitter.onNext(
                            transferState
                        )
                        transferEnd()
                    } else {

                        val transferState = WmTransferState(
                            State.TRANSFERRING,
                            true,
                            mTransferFiles!!.size,
                            mSendingFile!!
                        )

                        transferState.progress = 100
                        transferState.index = mSendFileCount

                        observableTransferEmitter.onNext(
                            transferState
                        )

                        mSendingFile = mTransferFiles!![mSendFileCount]

                        sjUniWatch.sendNormalMsg(
                            CmdHelper.getTransferFile02Cmd(
                                FileUtils.readFileBytes(mSendingFile).size,
                                mSendingFile!!.name
                            )
                        )
                    }
                } else {
                    mTransferring = false

                    observableTransferEmitter.onError(
                        RuntimeException("file transfer error reason:04 Error")
                    )

                    transferEnd()
                }
            }

            CMD_ID_8005 -> {
                mTransferring = false
                mCanceledSend = true

                cancelTransfer.onSuccess(true)
                transferEnd()
            }

            CMD_ID_8006 -> {
                mTransferring = false
                mCanceledSend = true
                val reason_cancel = ByteBuffer.wrap(msg)[16]
                LogUtils.logBlueTooth("设备取消传输原因：$reason_cancel")
                transferEnd()

                observableTransferEmitter.onError(
                    RuntimeException("file transfer error reason:06 Error")
                )
            }
        }
    }

    fun continueSendFileData(startProcess: Int, dataArray: ByteArray) {
        mPackageCount = dataArray.size / mCellLength
        mLastDataLength = dataArray.size % mCellLength
        if (mLastDataLength != 0) {
            mPackageCount = mPackageCount + 1
        }

        val fileTransferState = WmTransferState(
            State.TRANSFERRING,
            false,
            mSelectFileCount,
            mSendingFile!!
        )

        observableTransferEmitter.onNext(fileTransferState)

        for (i in startProcess.toInt() until mPackageCount) {
            mOtaProcess = i
            if (mCanceledSend || mErrorSend) { //取消或者中途出错
                LogUtils.logBlueTooth("消息取消或者出错：$mOtaProcess")
                mTransferring = false
                break
            }

            if (dataArray == null) {
                break
            }

            try {
                mTransferring = true
                val info = getOtaDataInfoNew(dataArray, i)
                sjUniWatch.sendNormalMsg(CmdHelper.getTransfer03Cmd(i, info, mDivide))

                val process_percent = 100f * (mOtaProcess + 1) / mPackageCount

                fileTransferState.progress = process_percent.toInt()
                //                            LogUtils.logBlueTooth("进度：" + (mOtaProcess + 1) + " 总个数：" + mPackageCount + " process_percent:" + process_percent);
//                        if (mTransferFileListener != null) {
//                            mTransferFileListener.transferProcess(
//                                mTransferFiles!![mSendFileCount].name,
//                                mSendFileCount + 1,
//                                mSelectFileCount,
//                                process_percent
//                            )
//                        }

                fileTransferState.index = mSendFileCount + 1
                observableTransferEmitter.onNext(fileTransferState)

                Thread.sleep(MSG_INTERVAL.toLong())
//                if (mOtaProcess == mPackageCount - 1) {
//                    mHandler.postDelayed(mTransferTimeoutRunner, TRANSFER_TIMEOUT)
//                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                mTransferring = false
                LogUtils.logBlueTooth("连续发送过程中出错：" + e.message)

                observableTransferEmitter.onError(e)

            }
        }
    }

    fun sendErrorMsg(errorProcess: Int) {
        mTransferRetryCount++
        val bytes = CmdHelper.getTransfer03Cmd(
            errorProcess,
            getOtaDataInfoNew(mFileDataArray!!, errorProcess),
            mDivide
        )
        sjUniWatch.sendNormalMsg(bytes)
    }

    fun getOtaDataInfoNew(dataArray: ByteArray, otaProcess: Int): OtaCmdInfo {
        val info = OtaCmdInfo()
        mDivide = if (otaProcess == 0 && mPackageCount > 1) {
            DIVIDE_Y_F_2
        } else {
            if (otaProcess == mPackageCount - 1) {
                DIVIDE_Y_E_2
            } else {
                DIVIDE_Y_M_2
            }
        }

//        LogUtils.logBlueTooth("分包类型：" + mDivide);
        if (otaProcess != mPackageCount - 1) {
            info.offSet = otaProcess * mCellLength
            info.payload = ByteArray(mCellLength)
            System.arraycopy(
                dataArray,
                otaProcess * mCellLength,
                info.payload,
                0,
                info.payload.size
            )
        } else {
//            LogUtils.logBlueTooth("最后一包长度：" + mLastDataLength);
            if (mLastDataLength == 0) {
                info.offSet = otaProcess * mCellLength
                info.payload = ByteArray(mCellLength)
                System.arraycopy(
                    dataArray,
                    otaProcess * mCellLength,
                    info.payload,
                    0,
                    info.payload.size
                )
            } else {
                info.offSet = otaProcess * mCellLength
                info.payload = ByteArray(mLastDataLength)
                System.arraycopy(
                    dataArray,
                    otaProcess * mCellLength,
                    info.payload,
                    0,
                    info.payload.size
                )
            }
        }
        return info
    }

}