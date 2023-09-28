package com.sjbt.sdk

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.base.sdk.AbUniWatch
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.WmScanDevice
import com.base.sdk.entity.apps.WmCameraFrameInfo
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.settings.AppView
import com.base.sdk.entity.settings.WmAppView
import com.base.sdk.entity.settings.WmDeviceInfo
import com.base.sdk.entity.settings.WmSportGoal
import com.base.sdk.port.log.WmLog
import com.google.gson.Gson
import com.sjbt.sdk.app.*
import com.sjbt.sdk.dfu.SJTransferFile
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.OtaCmdInfo
import com.sjbt.sdk.entity.PayloadPackage
import com.sjbt.sdk.entity.old.AppViewBean
import com.sjbt.sdk.entity.old.BasicInfo
import com.sjbt.sdk.entity.old.BiuBatteryBean
import com.sjbt.sdk.log.SJLog
import com.sjbt.sdk.settings.*
import com.sjbt.sdk.spp.BtStateReceiver
import com.sjbt.sdk.spp.OnBtStateListener
import com.sjbt.sdk.spp.bt.BtEngine
import com.sjbt.sdk.spp.bt.BtEngine.Listener
import com.sjbt.sdk.spp.bt.BtEngine.Listener.*
import com.sjbt.sdk.spp.cmd.*
import com.sjbt.sdk.sync.*
import com.sjbt.sdk.utils.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class SJUniWatch(context: Application, timeout: Int) : AbUniWatch(), Listener {

    private val TAG = TAG_SJ + "SJUniWatch"

    abstract var mContext: Application
    abstract var mMsgTimeOut: Int

    var mBtStateReceiver: BtStateReceiver? = null
    var mBtEngine = BtEngine.getInstance(this)
    var mBtAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var discoveryObservableEmitter: ObservableEmitter<BluetoothDevice>


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

    override val wmSettings = SJSettings(this)
    override val wmApps = SJApps(this)
    override val wmSync = SJSyncData(this)
    override val wmConnect = SJConnect(this)
    override val wmTransferFile = SJTransferFile()

    private val sjConnect: SJConnect = wmConnect as SJConnect

    val mBindStateMap = HashMap<String, Boolean>()

    //同步数据
    private val syncActivity = wmSync.syncActivityData as SyncActivityData
    private val syncCaloriesData = wmSync.syncCaloriesData as SyncCaloriesData
    private val syncDeviceInfo = wmSync.syncDeviceInfoData as SyncDeviceInfo
    private val syncBatteryInfo = wmSync.syncBatteryInfo as SyncBatteryInfo
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

    val gson = Gson()

    init {
        mContext = context
        mMsgTimeOut = timeout

        mBtEngine.listener = this

        mBtStateReceiver = BtStateReceiver(mContext!!, object : OnBtStateListener {

            override fun onClassicBtDisConnect(device: BluetoothDevice) {
                SJLog.logBt(TAG, "onClassicBtDisConnect：" + device.address)

                sjConnect.mCurrDevice?.let {
                    if (device == sjConnect.mCurrDevice) {

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
                if (device == sjConnect.mCurrDevice) {
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
                    if (device == sjConnect.mCurrDevice) {
                        sjConnect.mConnectTryCount = 0
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

                                CMD_ID_8001 -> {
                                    val basicInfo: BasicInfo = gson.fromJson(
                                        msgBean.payloadJson,
                                        BasicInfo::class.java
                                    )

                                    basicInfo?.let {
                                        val wm = WmDeviceInfo(
                                            it.prod_mode,
                                            it.mac_addr,
                                            it.soft_ver,
                                            it.dev_id,
                                            it.dev_name,
                                            it.dev_name
                                        )
                                        syncDeviceInfo.deviceEmitter?.onSuccess(wm)
                                    }

                                }
                                CMD_ID_8002 -> {


                                }

                                CMD_ID_8003 -> {
                                    val batteryBean = gson.fromJson(
                                        msgBean.payloadJson,
                                        BiuBatteryBean::class.java
                                    )

                                    batteryBean?.let {
                                        val batteryInfo =
                                            WmBatteryInfo(it.isIs_charging == 1, it.battery_main)
                                        syncBatteryInfo.batteryEmitter?.onSuccess(batteryInfo)
                                        syncBatteryInfo.observeBatteryEmitter?.onNext(batteryInfo)
                                    }
                                }

                                CMD_ID_8004 -> {
//                                    msg[16].toInt() == 1
//                                    appNotification.
                                }

                                CMD_ID_8008 -> {
                                    val appViewBean =
                                        gson.fromJson(msgBean.payloadJson, AppViewBean::class.java)

                                    val appViews = mutableListOf<AppView>()
                                    appViewBean?.let {
                                        it.list.forEach {
                                            appViews.add(AppView(it.using, it.id))
                                        }
                                    }

                                    val wmAppView = WmAppView(appViews)
                                    settingAppView.getEmitter.onSuccess(wmAppView)
                                }

                                CMD_ID_802E -> {//绑定
                                    val result = msg[16]
                                    WmLog.d(TAG, "绑定结果:$result")

                                    if (result.toInt() == 1) {
                                        sjConnect.btStateChange(WmConnectState.VERIFIED)
                                        sjConnect.mCurrAddress?.let {
                                            mBindStateMap.put(it, true)
                                        }

                                    } else {
                                        val success = ClsUtils.removeBond(
                                            BluetoothDevice::class.java,
                                            sjConnect.mCurrDevice
                                        )

                                        sjConnect.mCurrAddress?.let {
                                            mBindStateMap.put(it, false)
                                        }

                                        sjConnect.disconnect()
                                    }
                                }

                                CMD_ID_802F -> {//解绑
                                    val result = msg[16].toInt()

                                    sjConnect.mCurrAddress?.let {
                                        mBindStateMap.put(it, false)
                                    }

                                    LogUtils.logBlueTooth("解绑成功:" + result)

                                    if (result == 1) {
                                        sjConnect.mCurrDevice?.let {
                                            ClsUtils.removeBond(BluetoothDevice::class.java, it)
                                        }
                                    }
                                }
                            }
                        }

                        HEAD_SPORT_HEALTH -> {

                        }

                        HEAD_CAMERA_PREVIEW -> {

                        }

                        HEAD_FILE_SPP_A_2_D -> {

                        }

                        HEAD_NODE_TYPE -> {
                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {

                                    LogUtils.logBlueTooth("节点数据：" + msgBean.payload)

                                    var payloadPackage: PayloadPackage =
                                        PayloadPackage.fromByteArray(msgBean.payload)

                                    payloadPackage.itemList.forEach {
                                        when (it.urn[0]) {
                                            URN_1 -> {//蓝牙连接 暂用旧协议格式

                                            }

                                            URN_2 -> {//设置同步
                                                when (it.urn[1]) {
                                                    URN_1 -> {//运动目标

                                                        when (it.urn[2]) {
                                                            URN_0 -> {

                                                                val byteBuffer =
                                                                    ByteBuffer.wrap(it.data)
                                                                val step = byteBuffer.getInt()
                                                                val distance = byteBuffer.getInt()
                                                                val calories = byteBuffer.getInt()
                                                                val activityDuration =
                                                                    byteBuffer.getShort()

                                                                val wmSportGoal = WmSportGoal(
                                                                    step,
                                                                    distance,
                                                                    calories,
                                                                    activityDuration
                                                                )

                                                                settingSportGoal.setEmitter.onSuccess(
                                                                    wmSportGoal
                                                                )
                                                            }

                                                            URN_1 -> {//步数

                                                            }
                                                            URN_2 -> {//热量（卡）

                                                            }
                                                            URN_3 -> {//距离（米）

                                                            }
                                                            URN_4 -> {//活动时长（分钟）

                                                            }
                                                        }
                                                    }

                                                    URN_2 -> {//健康信息

                                                    }

                                                    URN_3 -> {//单位同步

                                                    }

                                                    URN_4 -> {//语言设置

                                                    }

                                                    URN_4 -> {//语言设置

                                                    }

                                                }

                                            }

                                            URN_3 -> {//表盘

                                            }

                                            URN_4 -> {//应用

                                            }

                                            URN_5 -> {//运动同步

                                            }
                                        }
                                    }
                                }
                            }
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
                    sjConnect.btStateChange(WmConnectState.DISCONNECTED)
                }

                CONNECTED -> {
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

    fun sendNormalMsg(msg: ByteArray?) {
        if (mBtEngine == null || msg == null) {
            return
        }
        if (mTransferring) {
            val byteBuffer = ByteBuffer.wrap(msg)
            val head = byteBuffer.get()
            val cmdId: Short = byteBuffer[2].toShort()

            if (isMsgStopped(head, cmdId)) {
                SJLog.logBt(TAG, "正在 传输文件中...:" + BtUtils.bytesToHexString(msg))
                return
            }

        }
        mBtEngine.sendMsgOnWorkThread(msg)
    }

    //发送Node节点消息
    fun sendNodeCmdList(payloadPackage: PayloadPackage) {
        payloadPackage.toByteArray().forEach {
            var payload: ByteArray = it

            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )

            sendNormalMsg(cmdArray)
        }
    }

    private fun isMsgStopped(head: Byte, cmdId: Short): Boolean {
        return head != HEAD_FILE_SPP_A_2_D && head != HEAD_CAMERA_PREVIEW && !isCameraCmd(
            head,
            cmdId
        )
    }

    private fun isCameraCmd(head: Byte, cmdId: Short): Boolean {
        return head == HEAD_COMMON && (cmdId == CMD_ID_8028 || cmdId == CMD_ID_8029 || cmdId == CMD_ID_802A || cmdId == CMD_ID_802B || cmdId == CMD_ID_802C)
    }

    override fun socketNotifyError(obj: ByteArray?) {

    }

    override fun onConnectFailed(device: BluetoothDevice, msg: String?) {

        WmLog.e(TAG, "onConnectFailed:" + msg)

        if (device == sjConnect.mCurrDevice) {

            if (msg!!.contains("read failed, socket might closed or timeout")
                || msg.contains("Connection reset by peer")
                || msg.contains("Connect refused")
                && mBindStateMap.get(device.address) == true
            ) {
                sjConnect.mConnectTryCount++
                if (sjConnect.mConnectTryCount < MAX_RETRY_COUNT) {
                    sjConnect.reConnect(device)
                } else {
                    sjConnect.mConnectTryCount = 0
                    sjConnect.btStateChange(WmConnectState.DISCONNECTED)
                }
            } else {
                sjConnect.mConnectTryCount = 0
                sjConnect.btStateChange(WmConnectState.DISCONNECTED)
            }
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

    //    https://static-ie.oraimo.com/oh.htm&mac=15:7E:78:A2:4B:30&projectname=OSW-802N&random=4536abcdhwer54q
    override fun parseScanQr(qrString: String): WmScanDevice {
        val wmScanDevice = WmScanDevice(WmDeviceModel.SJ_WATCH)
        val params = UrlParse.getUrlParams(qrString)
        if (!params.isEmpty()) {
            val schemeMacAddress = params["mac"]
            val schemeDeviceName = params["projectname"]
            val random = params["random"]

            wmScanDevice.randomCode = random

            wmScanDevice.address = schemeMacAddress
            wmScanDevice.isRecognized =
                !TextUtils.isEmpty(schemeMacAddress) &&
                        !TextUtils.isEmpty(schemeDeviceName) &&
                        !TextUtils.isEmpty(random) &&
                        isLegalMacAddress(schemeMacAddress)
        }
        return wmScanDevice
    }

    private fun isLegalMacAddress(address: String?): Boolean {
        return !TextUtils.isEmpty(address)
    }

    fun getCameraPreviewCmdInfo(
        mFramePackageCount: Int,
        mFrameLastLen: Int,
        frameInfo: WmCameraFrameInfo,
        i: Int
    ): OtaCmdInfo {
        val info = OtaCmdInfo()
        val dataArray: ByteArray = frameInfo.frameData
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

//        LogUtils.logBlueTooth("分包类型：" + mDivide);
        if (i == mFramePackageCount - 1 && mDivide != DIVIDE_N_2) {
//            LogUtils.logBlueTooth("最后一包长度：" + mFrameLastLen);
            if (mFrameLastLen == 0) {
                info.offSet = i * mCellLength
                info.payload = ByteArray(mCellLength)
                System.arraycopy(dataArray, i * mCellLength, info.payload, 0, info.payload.size)
            } else {
                info.offSet = i * mCellLength
                info.payload = ByteArray(mFrameLastLen)
                System.arraycopy(dataArray, i * mCellLength, info.payload, 0, info.payload.size)
            }
        } else {
            info.offSet = i * mCellLength
            if (mDivide == DIVIDE_Y_F_2 || mDivide == DIVIDE_N_2) { //首包或者不分包的时候需要传帧大小
                LogUtils.logBlueTooth("本帧大小:" + dataArray.size)
                LogUtils.logBlueTooth("帧数据长度：" + BtUtils.intToHex(dataArray.size))
                if (dataArray.size < mCellLength) {
                    LogUtils.logBlueTooth("不分包：$mDivide")
                    mCellLength = dataArray.size
                }
                val byteBuffer = ByteBuffer.allocate(mCellLength + 5).order(ByteOrder.LITTLE_ENDIAN)
                byteBuffer.put(frameInfo.frameType.toByte())
                byteBuffer.putInt(dataArray.size)
                val payload = ByteArray(mCellLength)
                System.arraycopy(dataArray, 0, payload, 0, payload.size)
                LogUtils.logBlueTooth("数据payload：" + payload.size)
                byteBuffer.put(payload)
                info.payload = byteBuffer.array()
                LogUtils.logBlueTooth("首包payload总长度：" + info.payload.size)
            } else {
                info.payload = ByteArray(mCellLength)
                System.arraycopy(dataArray, i * mCellLength, info.payload, 0, info.payload.size)
            }
        }
        return info
    }
}