package com.sjbt.sdk

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.base.sdk.AbUniWatch
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDevice
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.*
import com.base.sdk.entity.common.WmDiscoverDevice
import com.base.sdk.entity.common.WmTimeUnit
import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.settings.*
import com.base.sdk.port.log.AbWmLog
import com.base.sdk.port.app.WMCameraFlashMode
import com.base.sdk.port.app.WMCameraPosition
import com.google.gson.Gson
import com.sjbt.sdk.app.*
import com.sjbt.sdk.dfu.SJTransferFile
import com.sjbt.sdk.entity.*
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
import io.reactivex.rxjava3.core.*
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.nio.ByteBuffer

abstract class SJUniWatch(context: Application, timeout: Int) : AbUniWatch(), Listener {

    private val TAG = "SJUniWatch"

    abstract var mContext: Application
    abstract var mMsgTimeOut: Int

    var mBtStateReceiver: BtStateReceiver? = null

    private val mBtAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var discoveryObservableEmitter: ObservableEmitter<WmDiscoverDevice>

    private var connectEmitter: ObservableEmitter<WmConnectState>? = null

    var mBindInfo: WmBindInfo? = null
    var mCurrDevice: BluetoothDevice? = null
    var mCurrAddress: String? = null
    var mConnectTryCount = 0
    var mConnectState: WmConnectState = WmConnectState.DISCONNECTED

    override val wmSettings = SJSettings(this)
    override val wmApps = SJApps(this)
    override val wmSync = SJSyncData(this)
    override val wmTransferFile = SJTransferFile(this)
    override val wmLog: AbWmLog = SJLog(this)
    val mBtEngine: BtEngine = BtEngine(this)
    private val mBindStateMap = HashMap<String, Boolean>()

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
    private val appDateTime = wmApps.appDateTime as AppDateTime
    private val appCamera = wmApps.appCamera as AppCamera
    private val appAlarm = wmApps.appAlarm as AppAlarm
    private val appContact = wmApps.appContact as AppContact
    private val appDial = wmApps.appDial as AppDial
    private val appFind = wmApps.appFind as AppFind
    private val appLanguage = wmApps.appLanguage as AppLanguage
    private val appNotification = wmApps.appNotification as AppNotification
    private val appSport = wmApps.appSport as AppSport
    private val appWeather = wmApps.appWeather as AppWeather
    private val appMusicControl = wmApps.appMusicControl as AppMusicControl

    //设置
    private val settingAppView = wmSettings.settingAppView as SettingAppView
    private val settingHeartRateAlerts = wmSettings.settingHeartRate as SettingHeartRateAlerts
    private val settingPersonalInfo = wmSettings.settingPersonalInfo as SettingPersonalInfo
    private val settingSedentaryReminder =
        wmSettings.settingSedentaryReminder as SettingSedentaryReminder
    private val settingSoundAndHaptic = wmSettings.settingSoundAndHaptic as SettingSoundAndHaptic
    private val settingSportGoal = wmSettings.settingSportGoal as SettingSportGoal
    private val settingUnitInfo = wmSettings.settingUnitInfo as SettingUnitInfo
    private val settingWistRaise = wmSettings.settingWistRaise as SettingWistRaise
    private val settingSleepSet = wmSettings.settingSleepSettings as SettingSleepSet
    private val settingDrinkWaterReminder =
        wmSettings.settingDrinkWater as SettingDrinkWaterReminder

    private val gson = Gson()
    private var sharedPreferencesUtils: SharedPreferencesUtils
    var sdkLogEnable = false
    private val mHandler = Handler(Looper.getMainLooper())
    var MTU: Int = 600
    private var mtuEmitter: SingleEmitter<Int>? = null
    private val mPayloadMap = PayloadMap()

    val observableMtu: Single<Int> = Single.create { emitter ->
        mtuEmitter = emitter
        sendNormalMsg(CmdHelper.getMTUCmd)
    }

    override fun setLogEnable(logEnable: Boolean) {
        this.sdkLogEnable = logEnable
        Log.e(">>>>>>>>>", "logEnable:" + logEnable)
    }

    //当前消息的节点信息
    var mPayloadPackage: PayloadPackage? = null

    init {
        mContext = context
        mMsgTimeOut = timeout

        plant(Timber.DebugTree())

        mBtEngine.listener = this
        sharedPreferencesUtils = SharedPreferencesUtils.getInstance(mContext)

        mBtStateReceiver = BtStateReceiver(mContext!!, wmLog, object : OnBtStateListener {

            override fun onClassicBtDisConnect(device: BluetoothDevice) {
                wmLog.logD(TAG, "onClassicBtDisConnect：" + device.address)

                if (device == mCurrDevice) {
                    btStateChange(WmConnectState.DISCONNECTED)
                    wmTransferFile.mTransferring = false
                    mBtEngine.clearMsgQueue()
                    mBtEngine.clearStateMap()

                    appCamera.stopCameraPreview()
                    disconnect()

                    //                        removeCallBackRunner(mConnectTimeoutRunner)
                }
            }

            override fun onClassicBtConnect(device: BluetoothDevice) {
                wmLog.logD(TAG, "onClassicBtConnect：" + device.address)

                if (TextUtils.isEmpty(mCurrAddress)) {
                    mCurrAddress = sharedPreferencesUtils.getString(BT_ADDRESS, "")
                }

                if (device.address == mCurrAddress) {
                    mBindInfo?.let {
                        connect(device, it)
                    }
                }
            }

            override fun onClassicBtDisabled() {
                wmLog.logD(TAG, "onClassicBtDisabled")
                mBtEngine.clearMsgQueue()
                mBtEngine.clearStateMap()

                btStateChange(WmConnectState.BT_DISABLE)
                disconnect()

                appCamera.stopCameraPreview()
                wmTransferFile.transferEnd()

//                removeCallBackRunner(mConnectTimeoutRunner)
            }

            override fun onClassicBtOpen() {
                wmLog.logD(TAG, "onClassicBtOpen")
//                removeCallBackRunner(mConnectTimeoutRunner)
            }

            override fun onBindState(device: BluetoothDevice, bondState: Int) {
                if (bondState == BluetoothDevice.BOND_NONE) {
                    if (device == mCurrDevice) {
//                        mConnectTryCount = 0
                        mBtEngine.clearStateMap()

                        btStateChange(WmConnectState.DISCONNECTED)

//                        removeCallBackRunner(mConnectTimeoutRunner)
                        wmLog.logD(TAG, "取消配对：" + device.address)
                    }
                }
            }

            override fun onDiscoveryDevice(device: BluetoothDevice) {
                val wmDiscoverDevice = WmDiscoverDevice(device, 20)
                discoveryObservableEmitter.onNext(wmDiscoverDevice)
            }

            override fun onStartDiscovery() {

            }

            override fun onStopDiscovery() {

            }
        })

        if (mBtAdapter.isEnabled) {
            btStateChange(WmConnectState.BT_DISABLE)
        }

        appCamera.startCameraThread()
    }

    override fun socketNotify(state: Int, obj: Any?) {
        try {
            when (state) {
                MSG -> {
                    val msg = obj as ByteArray
                    val msgBean: MsgBean = CmdHelper.getPayLoadJson(msg)

//                    wmLog.logD(TAG, "收到msg:" + msgBean.toString())

                    when (msgBean.head) {
                        HEAD_VERIFY -> {

                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {
                                    sendNormalMsg(CmdHelper.biuVerifyCmd)
                                }

                                CMD_ID_8002 -> {
                                    mBindInfo?.let {
//                                        if (it.bindType != BindType.CONNECT_BACK) {
                                        wmLog.logI(TAG, "bindinfo:" + it)
                                        sendNormalMsg(CmdHelper.getBindCmd(it))
//                                        } else {
//                                            btStateChange(WmConnectState.VERIFIED)
//                                        }
                                    }
                                }
                            }
                        }

                        HEAD_COMMON -> {

                            when (msgBean.cmdId.toShort()) {

                                CMD_ID_8001 -> {//基本信息
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
                                            it.dev_name,
                                            it.dial_ability,
                                            it.screen,
                                            it.lang,
                                            it.cw,
                                            it.ch
                                        )
                                        syncDeviceInfo.deviceEmitter?.onSuccess(wm)
                                    }
                                }

                                CMD_ID_8002 -> {

                                }

                                CMD_ID_8003 -> {//电量消息
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
                                    appNotification.sendNotificationEmitter?.onSuccess(msg[16].toInt() == 1)
                                }

                                CMD_ID_8007 -> {//同步时间
                                    appDateTime.setEmitter?.onSuccess(true)
                                }

                                CMD_ID_8008 -> {//获取AppView List

                                    val appViewBean =
                                        gson.fromJson(msgBean.payloadJson, AppViewBean::class.java)

                                    val appViews = mutableListOf<AppView>()
                                    appViewBean?.let {
                                        it.list.forEach {
                                            appViews.add(AppView(it.using, it.id))
                                        }
                                    }

                                    val wmAppView = WmAppView(appViews)
                                    settingAppView.getEmitter?.onSuccess(wmAppView)
                                }

                                CMD_ID_8009 -> {//APP 视图设置
                                    settingAppView.setAppViewResult(msg[16].toInt() == 1)
                                }

                                CMD_ID_8010 -> {//设置/删除表盘返回
                                    val type = msg[16].toInt() // 1设定 2删除
                                    val actResult = msg[17].toInt() //是否操作成功
                                    val reason = msg[18].toInt() //是否操作成功

                                    appDial.deleteDialResult(actResult == 1)
                                }

                                CMD_ID_800F -> {
                                    if (msgBean.divideType === DIVIDE_N_2) {
                                        appDial.mMyDialList.clear()
                                        appDial.addDialList(msgBean)
                                    } else {
                                        if (msgBean.divideType === DIVIDE_Y_F_2) {
                                            appDial.mMyDialList.clear()
                                            appDial.addDialList(msgBean)
                                            return
                                        } else if (msgBean.divideType === DIVIDE_Y_M_2) {
                                            appDial.addDialList(msgBean)
                                            return
                                        } else if (msgBean.divideType === DIVIDE_Y_E_2) {
                                            appDial.addDialList(msgBean)
                                        }
                                    }

                                    appDial.syncDialListEmitter?.onNext(appDial.mMyDialList)
                                    appDial.syncDialListEmitter?.onComplete()
                                }

                                CMD_ID_8014 -> {//查询表盘当前信息

                                }

                                CMD_ID_8017 -> {//获取触感

                                    val ringState = msg[16].toInt()
                                    val msgShake = msg[17].toInt()
                                    val crowShake = msg[18].toInt()
                                    val sysShake = msg[19].toInt()
                                    val armScreen = msg[20].toInt()
                                    var keepNoVoice = 0
                                    if (msg.size > 21) {
                                        keepNoVoice = msg[21].toInt()
                                    }

                                    val wmWistRaise = WmWistRaise(armScreen == 1)

                                    settingWistRaise.getWmWistRaise(wmWistRaise)
                                    settingWistRaise.observeWmWistRaiseChange(wmWistRaise)

                                    val wmSoundAndHaptic = WmSoundAndHaptic(
                                        ringState == 1,
                                        msgShake == 1,
                                        crowShake == 1,
                                        sysShake == 1,
                                        keepNoVoice == 1
                                    )

                                    settingSoundAndHaptic.getWmWistRaise(wmSoundAndHaptic)
                                    settingSoundAndHaptic.observeWmWistRaiseChange(wmSoundAndHaptic)

                                }

                                CMD_ID_8018 -> {//设置触感

                                    val setSuccess = msg[16].toInt() == 1

                                    if (setSuccess) {
                                        settingSoundAndHaptic.setSuccess()
                                        settingWistRaise.setSuccess()
                                    }

                                }

                                CMD_ID_8019 -> {//监听触感
                                    sendNormalMsg(CmdHelper.deviceRingStateRespondCmd)
                                    val ctype = msg[16].toInt()
                                    val vValue = msg[17].toInt()

                                    when (ctype) {

                                        4 -> {
                                            settingWistRaise.observeWmWistRaiseChange(ctype, vValue)
                                        }

                                        else -> {
                                            settingSoundAndHaptic.observeWmWistRaiseChange(
                                                ctype,
                                                vValue
                                            )
                                        }
                                    }
                                }

                                CMD_ID_8028 -> {//收到dev拍照命令
                                    appCamera.cameraObserveTakePhotoEmitter?.onNext(true)

                                    sendNormalMsg(
                                        CmdHelper.getCameraRespondCmd(
                                            CMD_ID_8028,
                                            1.toByte()
                                        )
                                    )
                                }

                                CMD_ID_8029 -> {//设备拉起或者关闭相机监听
                                    appCamera.cameraObserveOpenEmitter?.onNext(msg[16].toInt() == 1)
                                }

                                CMD_ID_802A -> {//
                                    appCamera.cameraSingleOpenEmitter?.onSuccess(msg[16].toInt() == 1)
                                }

                                CMD_ID_802B -> {
                                    val action = msg[16]
                                    val stateOn = msg[17]

                                    if (action == CHANGE_CAMERA) {
                                        val front = stateOn.toInt() == 0
                                        if (front) {
                                            appCamera.cameraObserveFrontBackEmitter?.onNext(
                                                WMCameraPosition.WMCameraPositionFront
                                            )
                                        } else {
                                            appCamera.cameraObserveFrontBackEmitter?.onNext(
                                                WMCameraPosition.WMCameraPositionRear
                                            )
                                        }

                                    } else {
                                        val flashOn = stateOn.toInt() == 1
                                        if (flashOn) {
                                            appCamera.cameraObserveFlashEmitter?.onNext(
                                                WMCameraFlashMode.WMCameraFlashModeOn
                                            )
                                        } else {
                                            appCamera.cameraObserveFlashEmitter?.onNext(
                                                WMCameraFlashMode.WMCameraFlashModeOff
                                            )
                                        }
                                    }

                                    sendNormalMsg(
                                        CmdHelper.getCameraRespondCmd(
                                            CMD_ID_802B,
                                            1.toByte()
                                        )
                                    )
                                }

                                CMD_ID_802C -> {
//                                    appCamera.cameraFlashSwitchEmitter.onNext()
                                }

                                CMD_ID_802E -> {//绑定
                                    val result = msg[16]
                                    wmLog.logI(TAG, "绑定结果:$result")

                                    if (result.toInt() == 1) {
                                        btStateChange(WmConnectState.VERIFIED)
                                        mCurrAddress?.let {
                                            mBindStateMap.put(it, true)
                                        }

                                    } else {
                                        val success = ClsUtils.removeBond(
                                            BluetoothDevice::class.java,
                                            mCurrDevice
                                        )

                                        mCurrAddress?.let {
                                            mBindStateMap.put(it, false)
                                        }

                                        disconnect()
                                    }
                                }

                                CMD_ID_802F -> {//解绑
                                    val result = msg[16].toInt()

                                    mCurrAddress?.let {
                                        mBindStateMap.put(it, false)
                                    }

                                    wmLog.logD(TAG, "解绑成功:" + result)

                                    if (result == 1) {
                                        unbindEmitter?.onComplete()
                                        mCurrDevice?.let {
                                            ClsUtils.removeBond(BluetoothDevice::class.java, it)
                                        }
                                    } else {
                                        unbindEmitter?.onError(RuntimeException("unbind failed"))
                                    }
                                }
                            }
                        }

                        HEAD_SPORT_HEALTH -> {
                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_800C, CMD_ID_800D, CMD_ID_800E -> {
                                    settingSleepSet.sleepSetBusiness(msgBean, msg)
                                }
                            }
                        }

                        HEAD_CAMERA_PREVIEW -> {

                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {
                                    appCamera.cameraPreviewBuz(msg)
                                }

                                CMD_ID_8003 -> {
                                    val frameSuccess = msg[16]

                                    wmLog.logI(TAG, "发送成功：$frameSuccess")
                                    wmLog.logI(TAG, "发送下一帧：" + appCamera.mH264FrameMap.frameCount)

                                    wmLog.logI(
                                        TAG,
                                        "continueUpdateFrame 03:${appCamera.continueUpdateFrame}"
                                    )

                                    appCamera.sendFrameData03(frameSuccess)

                                }
                            }
                        }

                        HEAD_FILE_SPP_A_2_D -> {
                            wmTransferFile.transferFileBuz(msgBean, msg)
                        }

                        HEAD_NODE_TYPE -> {
                            when (msgBean.cmdId.toShort()) {
                                CMD_ID_8001 -> {//请求

//                                    1B000280F8001F00000000008EE800000700FFFFFFFF6480000132313030000E00000013880000010E0000005A001E
                                    if (msgBean.payload.size > 10) {//设备应用层回复
                                        wmLog.logI(TAG, "应用层消息：" + msgBean.payload.size)

                                        var payloadPackage: PayloadPackage =
                                            PayloadPackage.fromByteArray(msgBean.payload)

                                        parseNodePayload(true, msgBean, payloadPackage)

                                    } else {//设备传输层回复
                                        wmLog.logI(TAG, "传输层消息：" + msgBean.payload.size)
                                        mPayloadPackage?.let {
                                            it.itemList[0].data = msgBean.payload

                                            parseNodePayload(false, msgBean, it)
                                        }
                                    }
                                }

                                CMD_ID_8002 -> {//响应
                                    //
                                    sendCommunityResponse()

                                    if (msgBean.payloadLen >= 10) {//设备应用层回复
                                        wmLog.logI(TAG, "应用层消息：" + msgBean.payloadLen)

                                        var payloadPackage: PayloadPackage =
                                            PayloadPackage.fromByteArray(msgBean.payload)

                                        parseNodePayload(true, msgBean, payloadPackage)
                                    } else {//设备传输层回复
                                        wmLog.logI(TAG, "传输层消息：" + msgBean.payloadLen)

                                    }

                                    wmLog.logI(TAG, "响应消息：" + msgBean.payload.size)

                                }

                                CMD_ID_8003 -> {
                                    MTU =
                                        BtUtils.byte2short(msgBean.payload.reversedArray()).toInt()
                                    mtuEmitter?.onSuccess(MTU)
                                    wmLog.logD(TAG, "MTU:$MTU")
                                }

                                CMD_ID_8004 -> {
                                    wmLog.logI(TAG, "收到通讯层消息：" + msgBean.payload.size)
                                }
                            }
                        }
                    }
                }

                TIME_OUT -> {
                    wmLog.logD(TAG, "msg time out:")

                    msgTimeOut(obj as ByteArray)
                }

                BUSY -> {

                }

                ON_SOCKET_CLOSE -> {
                    wmLog.logD(TAG, "onSocketClose")
                    btStateChange(WmConnectState.DISCONNECTED)
                }

                CONNECTED -> {
                    mCurrAddress?.let {
                        sharedPreferencesUtils.putString(BT_ADDRESS, it)
                    }

                    btStateChange(WmConnectState.CONNECTED)
                    sendNormalMsg(CmdHelper.biuShakeHandsCmd)
                    mBtStateReceiver?.let {
                        it.setmSocket(mBtEngine.getmSocket())
                        it.setmCurrDevice(mCurrAddress)
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 回复通讯层消息
     */
    private fun sendCommunityResponse() {
        sendNormalMsg(
            CmdHelper.communityMsg
        )
    }

    private fun msgTimeOut(msg: ByteArray) {

        mBtAdapter?.takeIf { !it.isEnabled }?.let {
            mBtEngine.clearMsgQueue()
            mBtEngine.clearStateMap()
//            btStateChange(WmConnectState.DISCONNECTED)
        }

        val msgBean = CmdHelper.getPayLoadJson(msg)
        when (msgBean.head) {
            HEAD_VERIFY -> {
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT, CMD_STR_8002_TIME_OUT -> {
                        mBtEngine.clearStateMap()
                        mBtEngine.clearMsgQueue()

//                        disconnect()
//                        btStateChange(WmConnectState.DISCONNECTED)
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
                        appDateTime.setEmitter?.onSuccess(false)
                    }

                    CMD_STR_8008_TIME_OUT -> {
                        settingAppView.getEmitter?.onError(RuntimeException("get app views"))
                    }
                    CMD_STR_8009_TIME_OUT -> {
                        settingAppView.setEmitter?.onError(RuntimeException("set app view time out"))
                    }
                    CMD_STR_800A_TIME_OUT -> {}
                    CMD_STR_800B_TIME_OUT -> {}
                    CMD_STR_800C_TIME_OUT -> {}
                    CMD_STR_800D_TIME_OUT -> {}
                    CMD_STR_800E_TIME_OUT -> {}
                    CMD_STR_800F_TIME_OUT -> {
                        settingAppView.getEmitter?.onError(RuntimeException("getAppViews timeout!"))
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

            HEAD_FILE_SPP_A_2_D -> {
                wmTransferFile.timeOut(msg)
            }

            HEAD_CAMERA_PREVIEW -> {
                wmTransferFile.mTransferring = false
                when (msgBean.cmdIdStr) {
                    CMD_STR_8001_TIME_OUT -> {
//                        if (cameraPreviewEmitter != null) {
//                        cameraPreviewEmitter.onError(RuntimeException("camera preview timeout"))
//                    }
                    }
                }
            }
        }
    }

    fun clearMsg() {
        mBtEngine.clearMsgQueue()
    }

    fun sendNormalMsg(msg: ByteArray) {
        if (wmTransferFile.mTransferring) {
            val byteBuffer = ByteBuffer.wrap(msg)
            val head = byteBuffer.get()
            val cmdId: Short = byteBuffer[2].toShort()

            if (isMsgStopped(head, cmdId)) {
                wmLog.logD(TAG, "正在 传输文件中...:" + BtUtils.bytesToHexString(msg))
                return
            }
        }

        mBtEngine.sendMsgOnWorkThread(msg)
    }

    /**
     * 分包发送写入类型Node节点消息
     */
    fun sendWriteSubpackageNodeCmdList(
        totalLen: Short,
        itemLen: Int,
        payloadPackage: PayloadPackage
    ) {

        mPayloadMap.putFrame(payloadPackage)
        val nodeLen = payloadPackage.itemList[0].dataLen + 4
        val nodeCount = payloadPackage.itemList.size

//        var itemLen = 0
//
//        if (nodeLen * nodeCount > ITEM_MAX_LEN) {
//
//            if (nodeLen < 20) {
//                itemLen = nodeLen * 30
//            } else if (nodeLen < 30) {
//                itemLen = nodeLen * 20
//            } else if (nodeLen < 60) {
//                itemLen = nodeLen * 10
//            } else if (nodeLen < 90) {
//                itemLen = nodeLen * 7
//            }
//        }

        payloadPackage.toByteArray(
            mtu = itemLen,
            requestType = RequestType.REQ_TYPE_WRITE
        ).forEach {
            var payload: ByteArray = it

            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                totalLen,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )

            sendNormalMsg(cmdArray)
        }

        parseNodePayload(false, null, payloadPackage)
    }

    /**
     * 发送写入类型Node节点消息
     */
    fun sendWriteNodeCmdList(payloadPackage: PayloadPackage) {

        payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_WRITE).forEach {
            var payload: ByteArray = it

            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )

            sendNormalMsg(cmdArray)
        }

        mPayloadPackage = payloadPackage

//        parseNodePayload(false, null, payloadPackage)
    }

    /**
     * 发送读取类型Node节点消息
     */
    fun sendReadNodeCmdList(payloadPackage: PayloadPackage) {
        payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_READ).forEach {
            var payload: ByteArray = it

            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )

            sendNormalMsg(cmdArray)
        }

        mPayloadPackage = payloadPackage

//        parseNodePayload(false, null, payloadPackage)
    }

    /**
     * 发送操作类型Node节点消息
     */
    fun sendExecuteNodeCmdList(payloadPackage: PayloadPackage) {
        payloadPackage.toByteArray(requestType = RequestType.REQ_TYPE_EXECUTE).forEach {
            var payload: ByteArray = it

            val cmdArray = CmdHelper.constructCmd(
                HEAD_NODE_TYPE,
                CMD_ID_8001,
                DIVIDE_N_2,
                0,
                0,
                BtUtils.getCrc(HEX_FFFF, payload, payload.size),
                payload
            )

            sendNormalMsg(cmdArray)
        }

        mPayloadPackage = payloadPackage

//        parseNodePayload(false, null, payloadPackage)
    }

    private fun parseNodePayload(
        response: Boolean,
        msgBean: MsgBean? = null,
        payloadPackage: PayloadPackage
    ) {

        if (response) {
//            if (mPayloadMap.getFrame(payloadPackage._id) != null) {
                if (payloadPackage.actionType == ResponseResultType.RESPONSE_ALL_OK.type) {
                    wmLog.logD(TAG, "结果全部OK")
                } else if (payloadPackage.actionType == ResponseResultType.RESPONSE_ALL_FAIL.type) {
                    wmLog.logD(TAG, "结果全部Fail")
                } else if (payloadPackage.actionType == ResponseResultType.RESPONSE_EACH.type) {
                    wmLog.logD(TAG, "返回所有节点消息")
                    parseResponseEachNode(payloadPackage, msgBean)
                }
//            } else {
//                wmLog.logE(TAG, "设备回复错误消息！！！")
//            }

        } else {
            parseResponseEachNode(payloadPackage, msgBean)
        }

    }

    private fun parseResponseEachNode(
        payloadPackage: PayloadPackage,
        msgBean: MsgBean?
    ) {
        payloadPackage.itemList.forEach {
            when (it.urn[0]) {
                URN_CONNECT -> {//蓝牙连接 暂用旧协议格式

                }

                URN_SETTING -> {//设置同步
                    when (it.urn[1]) {
                        URN_SETTING_SPORT -> {//运动目标
                            if (it.data.size <= 1) {
                                wmLog.logD(TAG, "体育目标设置成功")
                            } else {
                                settingSportGoal.sportInfoBusiness(it)
                            }
                        }

                        URN_SETTING_PERSONAL -> {//健康信息
                            settingPersonalInfo.personalInfoBusiness(it)
                        }

                        URN_SETTING_UNIT -> {//单位同步
                            settingUnitInfo.unitInfoBusiness(it)
                        }

                        URN_SETTING_LANGUAGE -> {//语言设置
                            appLanguage.languageBusiness(it, msgBean)
                        }

                        URN_SETTING_SEDENTARY -> {//久坐提醒
                            settingSedentaryReminder.sedentaryReminderBusiness(it)
                        }

                        URN_SETTING_DRINK -> {//喝水提醒
                            settingDrinkWaterReminder.drinkWaterBusiness(it)
                        }

                        URN_SETTING_DATE_TIME -> {//时间同步

                        }

                        URN_SETTING_SOUND -> {//声音和触感

                        }

                        URN_SETTING_ARM -> {//抬腕亮屏

                        }

                        URN_SETTING_APP_VIEW -> {//AppView

                        }

                        URN_SETTING_DEVICE_INFO -> {//DeviceInfo

                        }

                    }
                }

                URN_APP -> {//应用

                    when (it.urn[1]) {
                        URN_APP_ALARM -> {
                            appAlarm.alarmBusiness(it)
                        }

                        URN_APP_SPORT -> {
                            when (it.urn[2]) {

                            }
                        }

                        URN_APP_CONTACT -> {
                            appContact.contactBusiness(it, msgBean)
                        }

                        URN_APP_WEATHER -> {

                            appWeather.weatherBusiness(it)
                        }

                        URN_APP_RATE -> {

                        }

                        URN_APP_FIND_PHONE -> {
                            when (it.urn[2]) {

                                URN_APP_FIND_PHONE_START -> {

                                    //                                    appFind.startFindPhoneEmitter?.onSuccess(true)

                                }

                                URN_APP_FIND_PHONE_STOP -> {

                                }
                            }
                        }

                        URN_APP_FIND_DEVICE -> {
                            appFind.appFindBusiness(it)
                        }

                        URN_APP_MUSIC_CONTROL -> {
                            appMusicControl.musicControlBusiness(it)
                        }
                    }
                }

                URN_SPORT -> {//运动同步
                }
            }
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

        wmLog.logE(TAG, "onConnectFailed:" + msg)

        if (device == mCurrDevice) {

            if (msg!!.contains("read failed, socket might closed or timeout")
                || msg.contains("Connection reset by peer")
                || msg.contains("Connect refused")
                && mBindStateMap.get(device.address) == true
            ) {
                mConnectTryCount++
                if (mConnectTryCount < MAX_RETRY_COUNT) {
                    reConnect(device)
                } else {
                    mConnectTryCount = 0
                    btStateChange(WmConnectState.DISCONNECTED)
                }
            } else {
                mConnectTryCount = 0
                btStateChange(WmConnectState.DISCONNECTED)
            }
        }
    }

    //    https://static-ie.oraimo.com/oh.htm&mac=15:7E:78:A2:4B:30&projectname=OSW-802N&random=4536abcdhwer54q
    override fun connectScanQr(qrString: String, bindInfo: WmBindInfo): WmDevice? {
        mBindInfo = bindInfo
        val params = UrlParse.getUrlParams(qrString)

        if (params.isNotEmpty()) {
            val schemeMacAddress = params["mac"]
            bindInfo.randomCode = params["random"]
            return schemeMacAddress?.let {
                connect(it, bindInfo)
            }
        } else {
            return WmDevice(bindInfo.model)
        }
    }

    /**
     * 通过address 连接
     */
    override fun connect(
        address: String,
        bindInfo: WmBindInfo
    ): WmDevice {
        mCurrAddress = address
        mBtStateReceiver?.let {
            it.setmCurrDevice(mCurrAddress)
        }
        val wmDevice = WmDevice(bindInfo.model)
        wmDevice.address = address
        wmDevice.mode = bindInfo.model
        mBindInfo = bindInfo
        wmDevice.isRecognized = bindInfo.model == WmDeviceModel.SJ_WATCH

        if (wmDevice.isRecognized) {
            wmLog.logD(TAG, " connect:${address}")
            try {
                if (!mBtAdapter.isEnabled) {
                    connectEmitter?.onNext(WmConnectState.BT_DISABLE)
                    return wmDevice
                }

                connectEmitter?.onNext(WmConnectState.CONNECTING)
                mCurrDevice = mBtAdapter.getRemoteDevice(address)
                mBtEngine.connect(mCurrDevice)
            } catch (e: Exception) {
                e.printStackTrace()
                connectEmitter?.onNext(WmConnectState.DISCONNECTED)
            }
        } else {
            connectEmitter?.onNext(WmConnectState.DISCONNECTED)
        }

        return wmDevice
    }

    /**
     * 通过BluetoothDevice 连接
     */
    override fun connect(
        bluetoothDevice: BluetoothDevice,
        bindInfo: WmBindInfo
    ): WmDevice {
        mBindInfo = bindInfo
        mCurrDevice = bluetoothDevice
        val wmDevice = WmDevice(bindInfo.model)
        mCurrAddress = bluetoothDevice.address

        mBtStateReceiver?.let {
            it.setmCurrDevice(mCurrAddress)
        }

        wmDevice.address = bluetoothDevice.address
        wmDevice.isRecognized = bindInfo.model == WmDeviceModel.SJ_WATCH

        if (wmDevice.isRecognized) {

            if (!mBtAdapter.isEnabled) {
                connectEmitter?.onNext(WmConnectState.BT_DISABLE)
                return wmDevice
            }

            wmLog.logE(TAG, " connect:${wmDevice}")
            connectEmitter?.onNext(WmConnectState.CONNECTING)
            mBtEngine.connect(bluetoothDevice)
        } else {
            connectEmitter?.onError(RuntimeException("not recognized device"))
        }

        return wmDevice
    }

    /**
     * 重连
     */
    fun reConnect(device: BluetoothDevice) {
        mBindInfo?.let {
            connect(device, it)
        }
    }

    fun btStateChange(state: WmConnectState) {
        connectEmitter?.onNext(state)
        mConnectState = state
    }

    override fun disconnect() {
        mBtEngine.closeSocket("user", true)
    }

    private var unbindEmitter: CompletableEmitter? = null
    override fun reset(): Completable {
        return Completable.create { emitter ->
            unbindEmitter = emitter

            if (mConnectState == WmConnectState.VERIFIED) {
                sendNormalMsg(CmdHelper.getUnBindCmd())
            } else {
                emitter.onError(RuntimeException("not VERIFIED"))
            }
        }
    }

    private fun getObservableConnectState(): Observable<WmConnectState> {
        if (currentConnectState == null || connectEmitter == null || connectEmitter!!.isDisposed) {
            currentConnectState = Observable.create {
                connectEmitter = it
            }
        } else {
            currentConnectState
        }

        return currentConnectState!!
    }

    private var currentConnectState: Observable<WmConnectState>? = null

    override val observeConnectState: Observable<WmConnectState> = getObservableConnectState()

    override fun getConnectState(): WmConnectState {
        return mConnectState
    }

    override fun setDeviceModel(wmDeviceModel: WmDeviceModel): Boolean {
        return wmDeviceModel == WmDeviceModel.SJ_WATCH
    }

    override fun startDiscovery(
        scanTime: Int,
        wmTimeUnit: WmTimeUnit
    ): Observable<WmDiscoverDevice> {
        return Observable.create(object : ObservableOnSubscribe<WmDiscoverDevice> {
            override fun subscribe(emitter: ObservableEmitter<WmDiscoverDevice>) {
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

                val stopAfter: Long = when (wmTimeUnit) {
                    WmTimeUnit.SECONDS -> {
                        scanTime * 1000L
                    }
                    WmTimeUnit.MILLISECONDS -> {
                        scanTime.toLong()
                    }
                    WmTimeUnit.MINUTES -> {
                        scanTime * 1000 * 60L
                    }
                    else -> {
                        scanTime.toLong()
                    }
                }

                mHandler.postDelayed(object : Runnable {
                    override fun run() {

                        if (ActivityCompat.checkSelfPermission(
                                mContext,
                                Manifest.permission.BLUETOOTH_SCAN
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }

                        mBtAdapter?.cancelDiscovery()
                    }
                }, stopAfter)
            }
        })
    }

    override fun getDeviceModel(): WmDeviceModel {
        return WmDeviceModel.SJ_WATCH
    }
}