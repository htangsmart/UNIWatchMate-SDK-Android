package com.sjbt.sdk

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.base.sdk.AbUniWatch
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.WmScanDevice
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.`interface`.AbWmConnect
import com.base.sdk.`interface`.WmTransferFile
import com.base.sdk.`interface`.app.AbWmApps
import com.base.sdk.`interface`.log.WmLog
import com.base.sdk.`interface`.setting.AbWmSettings
import com.base.sdk.`interface`.sync.AbWmSyncs
import com.sjbt.sdk.app.*
import com.sjbt.sdk.dfu.SJTransferFile
import com.sjbt.sdk.entity.CameraFrameInfo
import com.sjbt.sdk.entity.H264FrameMap
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.log.SJLog
import com.sjbt.sdk.settings.*
import com.sjbt.sdk.spp.BtStateReceiver
import com.sjbt.sdk.spp.OnBtStateListener
import com.sjbt.sdk.spp.bt.BtEngine
import com.sjbt.sdk.spp.bt.BtEngine.Listener
import com.sjbt.sdk.spp.bt.BtEngine.Listener.*
import com.sjbt.sdk.spp.cmd.CmdConfig.*
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.sync.*
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.FileUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.io.File
import java.nio.ByteBuffer

abstract class SJUniWatch(context: Application, timeout: Int) : AbUniWatch(), Listener {

    private val TAG = TAG_SJ + "SJUniWatch"

    abstract var mContext: Application
    abstract var mMsgTimeOut: Int

    var mBtStateReceiver: BtStateReceiver? = null
    private var mBtEngine = BtEngine.getInstance(this)
    private var mBtAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var discoveryObservableEmitter: ObservableEmitter<BluetoothDevice>

    //连接
    var mCurrDevice: BluetoothDevice? = null
    var mCurrAddress: String? = null
    var mConnectTryCount = 0

    //文件传输相关
    var mTransferFiles: List<File>? = null
    var mFileDataArray: ByteArray? = null
    var mSendingFile: File? = null
    var mSelectFileCount = 0
    var mSendFileCount = 0
    var mCellLength = 0
    var mOtaProcess = 0
    var mCanceledSend = false
    var mErrorSend: kotlin.Boolean = false
    var mDivide: Byte = 0
    var mPackageCount = 0
    var mLastDataLength: Int = 0
    var mTransferRetryCount = 0
    var mTransferring = false

    //相机预览相关
    var mCameraFrameInfo: CameraFrameInfo? = null
    var mH264FrameMap: H264FrameMap = H264FrameMap()
    var mLatestIframeId: Long = 0
    var mLatestPframeId: Long = 0
    private lateinit var mCameraThread: HandlerThread
    private lateinit var mCameraHandler: Handler

    var needNewH264Frame = false
    var continueUpdateFrame: Boolean = false

    override val wmSettings: AbWmSettings = SJSettings()
    override val wmApps: AbWmApps = SJApps()
    override val wmSync: AbWmSyncs = SJSyncData()
    override val wmConnect: AbWmConnect = SJConnect(mBtEngine, mBtAdapter)
    override val wmTransferFile: WmTransferFile = SJTransferFile()

    private val sjConnect: SJConnect = wmConnect as SJConnect

    //同步数据
    private val syncActivity = wmSync.syncActivityData as SyncActivityData
    private val syncCaloriesData = wmSync.syncCaloriesData as SyncCaloriesData
    private val syncDeviceInfo = wmSync.syncDeviceInfoData as SyncDeviceInfo
    private val syncDistanceData = wmSync.syncDistanceData as SyncDistanceData
    private val syncHeartRateData = wmSync.syncHeartRateData as SyncHeartRateData
    private val syncOxygenData = wmSync.syncOxygenData as SyncOxygenData
    private val syncRealtimeRateData = wmSync.syncRealtimeRateData as SyncRealtimeRateData
    private val syncSleepData = wmSync.syncSleepData as SyncSleepData
    private val syncSportSummaryData = wmSync.syncSportSummaryData as SyncSportSummaryData
    private val syncStepData = wmSync.syncStepData as SyncStepData
    private val syncTodayTotalData = wmSync.syncTodayInfoData as SyncTodayTotalData

    //应用
    private val appCamera = wmApps.appCamera as AppCamera
    private val appContact = wmApps.appContact as AppContact
    private val appDial = wmApps.appDial as AppDial
    private val appFind = wmApps.appFind as AppFind
    private val appLanguage = wmApps.appLanguage as AppLanguage
    private val appNotification = wmApps.appNotification as AppNotification
    private val appSport = wmApps.appSport as AppSport
    private val appWeather = wmApps.appWeather as AppWeather

    //设置
    private val settingAppView = wmSettings.settingAppView as SettingAppView
    private val settingDateTime = wmSettings.settingDateTime as SettingDateTime
    private val settingHeartRateAlerts = wmSettings.settingHeartRate as SettingHeartRateAlerts
    private val settingPersonalInfo = wmSettings.settingPersonalInfo as SettingPersonalInfo
    private val settingSedentaryReminder =
        wmSettings.settingSedentaryReminder as SettingSedentaryReminder
    private val settingSoundAndHaptic = wmSettings.settingSoundAndHaptic as SettingSoundAndHaptic
    private val settingSportGoal = wmSettings.settingSportGoal as SettingSportGoal
    private val settingUnitInfo = wmSettings.settingUnitInfo as SettingUnitInfo
    private val settingWistRaise = wmSettings.settingWistRaise as SettingWistRaise

    init {
        mContext = context
        mMsgTimeOut = timeout
        mCameraThread = HandlerThread("camera_send_thread")
        mBtEngine.listener = this

        mBtStateReceiver = BtStateReceiver(mContext!!, object : OnBtStateListener {

            override fun onClassicBtDisConnect(device: BluetoothDevice) {
                SJLog.logBt(TAG, "onClassicBtDisConnect：" + device.address)

                mCurrAddress?.let {
                    if (device.address == mCurrAddress) {
                        sjConnect.mConnectTryCount = 0

                        mTransferring = false
                        mTransferRetryCount = 0
                        mCanceledSend = true
                        mBtEngine.clearMsgQueue()
                        mBtEngine.clearStateMap()

                        appCamera.stopCameraPreview()
                        sjConnect.disconnect()

//                        removeCallBackRunner(mConnectTimeoutRunner)
                    }
                }
            }

            override fun onClassicBtConnect(device: BluetoothDevice) {
                SJLog.logBt(TAG, "onClassicBtConnect：" + device.address)
                if (!TextUtils.isEmpty(mCurrAddress) && device.address == mCurrAddress) {
                    sjConnect.disconnect()
                }
            }

            override fun onClassicBtDisabled() {
                SJLog.logBt(TAG, "onClassicBtDisabled")
                mTransferring = false
                mTransferRetryCount = 0
                mCanceledSend = true
                mBtEngine.clearMsgQueue()
                mBtEngine.clearStateMap()

                sjConnect.disconnect()
                sjConnect.btStateChange(WmConnectState.BT_DISABLE)

                appCamera.stopCameraPreview()

//                removeCallBackRunner(mConnectTimeoutRunner)
            }

            override fun onClassicBtOpen() {
                SJLog.logBt(TAG, "onClassicBtOpen")
                sjConnect.btStateChange(WmConnectState.BT_ENABLE)
//                removeCallBackRunner(mConnectTimeoutRunner)
            }

            override fun onBindState(device: BluetoothDevice, bondState: Int) {
                if (bondState == BluetoothDevice.BOND_NONE) {
                    if (!TextUtils.isEmpty(mCurrAddress) && device.address == mCurrAddress) {
                        mConnectTryCount = 0
                        mBtEngine.clearStateMap()

                        sjConnect.btStateChange(WmConnectState.DISCONNECTED)

//                        removeCallBackRunner(mConnectTimeoutRunner)
                        SJLog.logBt(TAG, "取消配对：" + device.address)
                    }
                }
            }

            override fun onDiscoveryDevice(device: BluetoothDevice?) {
                discoveryObservableEmitter.onNext(device)
            }

            override fun onStartDiscovery() {

            }

            override fun onStopDiscovery() {

            }
        })
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

    override fun socketNotify(state: Int, obj: Any?) {
        try {
            when (state) {
                MSG -> {
                    val msg = obj as ByteArray
                    val msgBean: MsgBean = CmdHelper.getPayLoadJson(msg)

                    WmLog.d(TAG, "收到msg:" + msgBean.toString())

                    when (msgBean.head) {
                        HEAD_VERIFY -> {

                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {
                                    sendNormalMsg(CmdHelper.biuVerifyCmd)
                                }

                                CMD_ID_8002 -> {
                                    sjConnect.mBindInfo?.let {
                                        sendNormalMsg(CmdHelper.getBindCmd(it))
                                    }
                                }
                            }


                        }

                        HEAD_COMMON -> {

                            when (msgBean.cmdId.toShort()) {

                                CMD_ID_802E -> {
                                    val result = msg[16]
                                    WmLog.d(TAG, "绑定:$result")
                                }

                                CMD_ID_802F -> {

                                }

                            }
                        }

                        HEAD_SPORT_HEALTH -> {

                        }

                        HEAD_CAMERA_PREVIEW -> {

                        }

                        HEAD_FILE_SPP_A_2_D -> {

                        }
                    }
                }

                TIME_OUT -> {
                    SJLog.logBt(TAG, "msg time out:")

                    msgTimeOut(obj as ByteArray)
                }

                BUSY -> {

                }

                ON_SOCKET_CLOSE -> {
                    SJLog.logBt(TAG, "onSocketClose")
                }

                CONNECTED -> {
                    mCurrDevice = obj as BluetoothDevice
                    sjConnect.btStateChange(WmConnectState.CONNECTED)

                    sendNormalMsg(CmdHelper.biuShakeHandsCmd)

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun msgTimeOut(msg: ByteArray) {

        mBtAdapter?.takeIf { !it.isEnabled }?.let {
            mBtEngine.clearMsgQueue()
            mBtEngine.clearStateMap()
            (wmConnect as? SJConnect)?.btStateChange(WmConnectState.DISCONNECTED)
        }

        if (mCanceledSend) {
            mBtEngine.clearMsgQueue()
            return
        }

        val msgBean = CmdHelper.getPayLoadJson(msg)
        when (msgBean.head) {
            HEAD_VERIFY -> {
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT, CMD_STR_8002_TIME_OUT -> {
                        mBtEngine.clearStateMap()
                        mBtEngine.clearMsgQueue()

                        sjConnect.btStateChange(WmConnectState.DISCONNECTED)
                    }
                }
            }

            HEAD_COMMON -> {
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT -> {
//                        syncDeviceInfo.syncTimeOut("get basicInfo timeout!")
                    }

                    CMD_STR_8002_TIME_OUT -> {
                    }

                    CMD_STR_8003_TIME_OUT -> {
//                        if (batteryEmitter != null) {
//                            batteryEmitter.onError(RuntimeException("get battery timeout!"))
//                        }

                    }

                    CMD_STR_8004_TIME_OUT -> {
//                        if (sendNotifyEmitter != null) {
//                            sendNotifyEmitter.onError(RuntimeException("send notify timeout!"))
//                        }

                    }
                    CMD_STR_8005_TIME_OUT -> {}
                    CMD_STR_8006_TIME_OUT -> {}
                    CMD_STR_8007_TIME_OUT -> {
                        settingDateTime.getEmitter.onError(RuntimeException("get sync time timeout!"))
                    }

                    CMD_STR_8008_TIME_OUT -> {
//                        if (emitterGetAppView != null) {
//                            emitterGetAppView.onError(RuntimeException("get app views"))
//                        }
                        settingAppView.getEmitter.onError(RuntimeException("get app views"))
                    }
                    CMD_STR_8009_TIME_OUT -> {
//                        if (singleSetAppViewEmitter != null) {
//                            singleSetAppViewEmitter.onError(RuntimeException("set app view time out"))
//                        }
                        settingAppView.setEmitter.onError(RuntimeException("set app view time out"))
                    }
                    CMD_STR_800A_TIME_OUT -> {}
                    CMD_STR_800B_TIME_OUT -> {}
                    CMD_STR_800C_TIME_OUT -> {
//                        if (timeStateEmitter != null) {
//                            timeStateEmitter.onError(RuntimeException("get time state timeout!"))
//                        }

                        settingDateTime.getEmitter.onError(RuntimeException("get sync time timeout!"))
                    }
                    CMD_STR_800D_TIME_OUT -> {}
                    CMD_STR_800E_TIME_OUT -> {}
                    CMD_STR_800F_TIME_OUT -> {
                        settingAppView.getEmitter.onError(RuntimeException("getAppViews timeout!"))
                    }

//                   TODO
//                    CMD_STR_8010_TIME_OUT -> if (dialDelEmitter != null) {
//                        dialDelEmitter.onError(RuntimeException("delete dial timeout!"))
//
//                    }

                    CMD_STR_8011_TIME_OUT -> {}
                    CMD_STR_8012_TIME_OUT -> {}
                    CMD_STR_8013_TIME_OUT -> {}
                    CMD_STR_8014_TIME_OUT -> {

//                        TODO
//                        if (getDialEmitter != null) {
//                            getDialEmitter.onError(RuntimeException("get dial timeout!"))
//                        }

                    }
                    CMD_STR_8017_TIME_OUT -> {

                    }
                    //                        if (mGetDeviceRingStateListener != null) {
//                            mGetDeviceRingStateListener.onTimeOut(msgBean);
//                        }
//                        if (shakeEmitterSingle != null) {
//                            shakeEmitterSingle.onError(RuntimeException("shake timeout!"))
//                        }
                    CMD_STR_8018_TIME_OUT -> {

                        //                        if (mSetDeviceRingStateListener != null) {
//                            mSetDeviceRingStateListener.onTimeOut(msgBean);
//                        }
//                        if (setDeviceEmitter != null) {
//                            setDeviceEmitter.onError(RuntimeException("set state timeout!"))
//                        }
                    }

                    CMD_STR_801C_TIME_OUT -> {
//                        if (setAlarmEmitter != null) {
//                            setAlarmEmitter.onError(RuntimeException("set alarm timeout!"))
//                        }
                    }

                    CMD_STR_801E_TIME_OUT -> {
//                        if (getAlarmEmitter != null) {
//                            getAlarmEmitter.onError(RuntimeException("get alarm timeout!"))
//                        }
                    }
                    CMD_STR_8021_TIME_OUT -> {
//                        if (searchDeviceEmitter != null) {
//                            searchDeviceEmitter.onError(RuntimeException("search device timeout!"))
//                        }
                    }
                    CMD_STR_8022_TIME_OUT -> {
//                        if (contactListEmitter != null) {
//                            contactListEmitter.onError(RuntimeException("get contact list timeout!"))
//                        }
                    }
                    CMD_STR_8023_TIME_OUT -> {
//                        if (appAddContactEmitter != null) {
//                            appAddContactEmitter.onError(RuntimeException("app add contact time out"))
//                        }
                    }
                    CMD_STR_8025_TIME_OUT -> {
//                        if (appDelContactEmitter != null) {
//                            appDelContactEmitter.onError(RuntimeException("app delete contact timeout"))
//                        }
                    }
                    CMD_STR_8026_TIME_OUT -> {}
                    CMD_STR_8027_TIME_OUT -> {
//                        if (contactActionType == CONTACT_ACTION_LIST) {
//                            contactListEmitter.onError(RuntimeException("get contact list timeout!"))
//                        } else if (contactActionType == CONTACT_ACTION_ADD) {
//                            appAddContactEmitter.onError(RuntimeException("app add contact time out"))
//                        } else if (contactActionType == CONTACT_ACTION_DELETE) {
//                            appDelContactEmitter.onError(RuntimeException("app delete contact timeout"))
//                        }
                    }
                    CMD_STR_8029_TIME_OUT -> {}
                    CMD_STR_802A_TIME_OUT -> {
//                        if (requestDeviceCameraEmitter != null) {
//                            requestDeviceCameraEmitter.onError(RuntimeException("request device camera timeout!"))
//                        }
                    }
                    CMD_STR_802D_TIME_OUT -> {
//                        if (actionSupportEmitter != null) {
//                            actionSupportEmitter.onError(RuntimeException("action bean error!"))
//                        }
                    }
                }
            }

            HEAD_SPORT_HEALTH -> {
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT -> {

//                        if (getSportInfoEmitter != null) {
//                            getSportInfoEmitter.onError(RuntimeException("get sport info timeout"))
//                        }

                        wmApps as SJApps

                    }

                    CMD_STR_8002_TIME_OUT -> {
//                        if (stepEmitter != null) {
//                            stepEmitter.onError(RuntimeException("get step timeout"))
//                        }
                    }

                    CMD_STR_8003_TIME_OUT -> {
//                        if (rateEmitter != null) {
//                            rateEmitter.onError(RuntimeException("get rate timeout"))
//                        }
                    }
                    CMD_STR_8008_TIME_OUT -> {
//                        if (sleepRecordEmitter != null) {
//                            sleepRecordEmitter.onError(RuntimeException("get sleep record timeout"))
//                        }
                    }
                    CMD_STR_8009_TIME_OUT -> {
//                        if (getBloodOxEmitter != null) {
//                            getBloodOxEmitter.onError(RuntimeException("get blood ox timeout"))
//                        }
                    }
                    CMD_STR_800A_TIME_OUT -> {
//                        if (getBloodSugarEmitter != null) {
//                            getBloodSugarEmitter.onError(RuntimeException("get blood sugar timeout"))
//                        }
                    }
                    CMD_STR_800B_TIME_OUT -> {
//                        if (getBloodPressEmitter != null) {
//                            getBloodPressEmitter.onError(RuntimeException("get blood press timeout"))
//                        }
                    }
                    CMD_STR_800C_TIME_OUT -> {
//                        if (sleepSetEmitter != null) {
//                        sleepSetEmitter.onError(RuntimeException("sleep set timeout"))
//                        }
                    }
                    CMD_STR_800D_TIME_OUT -> {
//                        if (setSleepEmitter != null) {
//                        setSleepEmitter.onError(RuntimeException("set sleep timeout"))
//                    }
                    }
                }
            }

            HEAD_CAMERA_PREVIEW -> {
                mTransferring = false
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT -> {
//                        if (cameraPreviewEmitter != null) {
//                        cameraPreviewEmitter.onError(RuntimeException("camera preview timeout"))
//                    }
                    }
                }
            }

            HEAD_FILE_SPP_A_2_D -> {
                mTransferring = false
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT -> {}
                    CMD_STR_8002_TIME_OUT -> if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        mTransferRetryCount++
                        mSendingFile = mTransferFiles!![mSendFileCount]
                        sendNormalMsg(
                            CmdHelper.getTransferFile02Cmd(
                                FileUtils.readFileBytes(
                                    mSendingFile
                                ).size, mSendingFile!!.name
                            )
                        )
                    } else {
                        transferEnd()
                    }
                    CMD_STR_8003_TIME_OUT -> if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        mTransferRetryCount++
                        sendNormalMsg(
                            CmdHelper.getTransfer03Cmd(
                                mOtaProcess,
                                getOtaDataInfoNew(mFileDataArray!!, mOtaProcess),
                                mDivide
                            )
                        )
                    } else {
//                        if (mTransferFileListener != null) {
//                            mTransferFileListener.transferFail(FAIL_TYPE_TIMEOUT, "8003 time out")
//                        }


                    }

                    CMD_STR_8004_TIME_OUT -> if (mTransferRetryCount < MAX_RETRY_COUNT) {
                        mTransferRetryCount++
                        val ota_data = CmdHelper.transfer04Cmd
                        sendNormalMsg(ota_data)
                    } else {
//                        if (mTransferFileListener != null) {
//                            mTransferFileListener.transferFail(FAIL_TYPE_TIMEOUT, "8004 time out")
//                        }
                    }
                }
            }
        }
    }

    private fun getOtaDataInfoNew(dataArray: ByteArray, otaProcess: Int): OtaCmdInfo {
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

    private fun transferEnd() {
        try {
            mBtEngine.clearMsgQueue()
            mOtaProcess = 0
            mTransferRetryCount = 0
            mTransferring = false
            mSendFileCount = 0
//            removeCallBackRunner(mTransferTimeoutRunner)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNormalMsg(msg: ByteArray?) {
        if (mBtEngine == null || msg == null) {
            return
        }
        if (mTransferring) {
            val byteBuffer = ByteBuffer.wrap(msg)
            val head = byteBuffer.get()
            val cmdId = byteBuffer[2]

            if (isMsgStopped(head, cmdId)) {
                SJLog.logBt(TAG, "正在 传输文件中...:" + BtUtils.bytesToHexString(msg))
                return
            }

        }
        mBtEngine.sendMsgOnWorkThread(msg)
    }

    private fun isMsgStopped(head: Byte, cmdId: Byte): Boolean {
        return head != HEAD_FILE_SPP_A_2_D && head != HEAD_CAMERA_PREVIEW && !isCameraCmd(
            head,
            cmdId
        )
    }

    private fun isCameraCmd(head: Byte, cmdId: Byte): Boolean {
        return head == HEAD_COMMON && (cmdId == CMD_ID_8028 || cmdId == CMD_ID_8029 || cmdId == CMD_ID_802A || cmdId == CMD_ID_802B || cmdId == CMD_ID_802C)
    }

    override fun socketNotifyError(obj: ByteArray?) {

    }

    override fun onConnectFailed(device: BluetoothDevice, msg: String?) {

        WmLog.e(TAG, "onConnectFailed:" + msg)

        if (device!!.address == mCurrAddress) {
            if (msg!!.contains("read failed, socket might closed or timeout")
                || msg.contains("Connection reset by peer")
                || msg.contains("Connect refused")
            ) {
                mConnectTryCount++
                if (mConnectTryCount < MAX_RETRY_COUNT) {
                    sjConnect.reConnect(device)
                } else {
                    mConnectTryCount = 0
                    sjConnect.btStateChange(WmConnectState.DISCONNECTED)
                }
            } else {
                mConnectTryCount = 0
                sjConnect.btStateChange(WmConnectState.DISCONNECTED)
            }
        } else {
            sjConnect.btStateChange(WmConnectState.DISCONNECTED)
        }
    }

    override fun startDiscovery(): Observable<BluetoothDevice> {
        return Observable.create(object : ObservableOnSubscribe<BluetoothDevice> {
            override fun subscribe(emitter: ObservableEmitter<BluetoothDevice>) {
                discoveryObservableEmitter = emitter

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    mContext?.let {
                        if (ActivityCompat.checkSelfPermission(
                                it,
                                Manifest.permission.BLUETOOTH_SCAN
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            discoveryObservableEmitter.onError(RuntimeException("permission denied"))
                            return
                        }
                    }
                }

                mBtAdapter?.startDiscovery()
            }
        })
    }

    override fun getDeviceModel(): WmDeviceModel {
        return WmDeviceModel.SJ_WATCH
    }

    override fun setDeviceMode(wmDeviceModel: WmDeviceModel): Boolean {
        return wmDeviceModel == WmDeviceModel.SJ_WATCH
    }

    override fun parseScanQr(qrString: String): WmScanDevice {
        val wmScanDevice = WmScanDevice(WmDeviceModel.SJ_WATCH)
        wmScanDevice.qrUrl = qrString
        wmScanDevice.address = qrString.substring(0, 12)

        wmScanDevice.isRecognized =
            qrString.contains("shenju") && isLegalMacAddress(wmScanDevice.address)

        return wmScanDevice
    }

    private fun isLegalMacAddress(address: String?): Boolean {
        return !TextUtils.isEmpty(address)
    }
}