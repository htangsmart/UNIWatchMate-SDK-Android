package com.sjbt.sdk.sample.data.device

import android.content.Context
import android.text.TextUtils
import androidx.annotation.IntDef
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.base.api.UNIWatchMate
import com.base.sdk.entity.BindType
import com.base.sdk.entity.WmBindInfo
import com.base.sdk.entity.WmDeviceModel
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.data.WmBatteryInfo
import com.base.sdk.entity.settings.WmDateTime
import com.base.sdk.entity.settings.WmPersonalInfo
import com.base.sdk.entity.settings.WmUnitInfo
import com.blankj.utilcode.util.TimeUtils
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.data.config.SportGoalRepository
import com.sjbt.sdk.sample.data.user.UserInfoRepository
import com.sjbt.sdk.sample.db.AppDatabase
import com.sjbt.sdk.sample.entity.DeviceBindEntity
import com.sjbt.sdk.sample.entity.toModel
import com.sjbt.sdk.sample.model.device.ConnectorDevice
import com.sjbt.sdk.sample.model.user.UserInfo
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.CompletableOnSubscribe
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.rx3.awaitSingleOrNull
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

interface DeviceManager {

    val flowDevice: StateFlow<ConnectorDevice?>?

    val flowConnectorState: StateFlow<WmConnectState>// connectorState

    val flowBattery: StateFlow<WmBatteryInfo?>
    /**
     * Sync data state of [FcSyncState].
     * Null for current no any sync state
     */
//    val flowSyncState: StateFlow<Int?>

    /**
     * [SyncEvent]
     */
    val flowSyncEvent: Flow<Int>

    /**
     * Does need weather
     */
//    fun flowWeatherRequire(): Flow<Boolean>

    /**
     * Trying bind a new device.
     * If bind success, the device info will be automatically saved to storage
     */
    fun bind(address: String, name: String)

    /**
     * Rebind current device
     * If bind success, the device info will be automatically saved to storage
     */
    fun rebind()

    /**
     * Cancel if [bind] or [rebind] is in progress
     * Otherwise do nothing.
     */
    fun cancelBind()

    /**
     * Unbind device and clear the device info in the storage.
     */
    suspend fun delDevice()

    /**
     * Reset device and clear the device info in the storage.
     */
    suspend fun reset()

    /**
     * When state is [ConnectorState.PRE_CONNECTING], get the number of seconds to retry the connection next time
     */
//    fun getNextRetrySeconds(): Int

    /**
     * When state is [ConnectorState.DISCONNECTED], get the reason
     */
//    fun getDisconnectedReason(): FcDisconnectedReason


    fun disconnect()

    fun reconnect()

//  fun newDfuManager(): FcDfuManager

    fun syncData()

    @IntDef(
        SyncEvent.SYNCING,
        SyncEvent.SUCCESS,
        SyncEvent.FAIL_DISCONNECT,
        SyncEvent.FAIL,
    )
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.BINARY)
    annotation class SyncEvent {
        companion object {
            const val SYNCING = 0//正在同步
            const val SUCCESS = 1//同步成功
            const val FAIL_DISCONNECT = 2//同步失败，因为连接断开
            const val FAIL = 3//同步失败
        }
    }
}

fun DeviceManager.flowStateConnected(): Flow<Boolean> {
    return flowConnectorState.map { it == WmConnectState.VERIFIED }.distinctUntilChanged()
}

fun DeviceManager.isConnected(): Boolean {
    return flowConnectorState.value == WmConnectState.VERIFIED
}

/**
 * Manage device connectivity and status
 */
internal class DeviceManagerImpl(
    context: Context,
    private val applicationScope: CoroutineScope,
    private val internalStorage: InternalStorage,
    private val userInfoRepository: UserInfoRepository,
    private val sportGoalRepository: SportGoalRepository,
    private val syncDataRepository: SyncDataRepository,
    appDatabase: AppDatabase,
) : DeviceManager {

    private val settingDao = appDatabase.settingDao()

    /**
     * Manually control the current device
     * 用户点击连接设备时更新 ConnectorDevice
     */
    private val deviceFromMemory: MutableStateFlow<ConnectorDevice?> = MutableStateFlow(null)

    /**
     * Flow device from storage
     * 登录用户变化时，deviceFromMemory value设置为null，从数据库获取
     */
    private val deviceFromStorage = internalStorage.flowAuthedUserId.flatMapLatest {
        //ToNote:Clear device in memory every time Authed user changed. Avoid connecting to the previous user's device after switching users
        deviceFromMemory.value = null
        if (it == null) {
            flowOf(null)
        } else {
            settingDao.flowDeviceBind(it)
        }
    }.map {
        it.toModel()
    }

    /**
     * Combine device [deviceFromMemory] and [deviceFromStorage]
     */
    override val flowDevice: StateFlow<ConnectorDevice?> =
        deviceFromMemory.combine(deviceFromStorage) { fromMemory, fromStorage ->
            Timber.tag(TAG).i("device fromMemory:%s , fromStorage:%s", fromMemory, fromStorage)
            check(fromStorage == null || !fromStorage.isTryingBind)//device fromStorage, isTryingBind must be false

            //Use device fromMemory first
            fromMemory ?: fromStorage
        }.stateIn(applicationScope, SharingStarted.Eagerly, null)


//    override val flowDevice: StateFlow<WmDeviceInfo?>? =  UNIWatchMate.mWmSyncs?.syncDeviceInfoData?.observeSyncData?.asFlow()?.stateIn(applicationScope, SharingStarted.Eagerly, null)
    /**
     * Connector state combine adapter state and current device
     */
    override val flowConnectorState = combine(
        flowDevice,
        UNIWatchMate.observeConnectState.startWithItem(UNIWatchMate.getConnectState())
            .asFlow().distinctUntilChanged()
    ) { device, connectorState ->
        //Device trying bind success,save it
        Timber.tag(TAG)
            .e("flowConnectorState flowDevice == ${flowDevice.value}  connectorState == $connectorState")
        if (device != null && device.isTryingBind && connectorState == WmConnectState.VERIFIED) {
            saveDevice(device)
        }
        connectorState
//        combineState(device, connectorState)
    }.stateIn(applicationScope, SharingStarted.Eagerly, WmConnectState.DISCONNECTED)

    private val _flowSyncEvent = Channel<Int>()//通知SyncFragment去响应同步结果
    override val flowSyncEvent = _flowSyncEvent.receiveAsFlow()

    init {
        applicationScope.launch {
            //Connect or disconnect when device changed
            //当登录设备或用户变化时，把个人资料更新到设备
            deviceFromStorage.collect { device ->
                Timber.tag(TAG).e("it.device == $device", device)
                if (deviceFromMemory.value == null) {
                    internalStorage.flowAuthedUserId.value?.let {
                        val userInfo = userInfoRepository.getUserInfo(it)
                        userInfo?.let { userInfo ->
                            device?.let { storageDevice ->
                                Timber.tag(TAG).e("UNIWatchMate.connect")
                                UNIWatchMate.connect(
                                    address = device!!.address,
                                    WmBindInfo(userInfo.id.toString(),
                                        "name",
                                        BindType.DISCOVERY,
                                        WmDeviceModel.SJ_WATCH)
                                )
                            }
                        }
                    }
                }
            }
        }

//        UNIWatchMate.wmConnect.observeConnectState.subscribe {
//                Timber.tag(TAG).e("observeConnectState it=$it")
//        }
        applicationScope.launch {
//            UNIWatchMate.wmConnect.connect()

        }

        applicationScope.launch {
            flowConnectorState.collect {
                Timber.tag(TAG).e("onConnected if verified state:%s", it)
                if (it == WmConnectState.VERIFIED) {
                    onConnected()
                }
            }
        }

        applicationScope.launch {
            sportGoalRepository.flowCurrent.drop(1).collect {
                if (flowConnectorState.value == WmConnectState.VERIFIED) {
                    applicationScope.launchWithLog {
                        UNIWatchMate?.wmSettings?.settingSportGoal?.set(it)?.await()
                    }
                }
            }
        }

    }

    private fun onConnected() {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId != null) {
            applicationScope.launchWithLog {
//                if(abUniWatch?.wmConnect?.)
//                if () {//This connection is in binding mode
//                    //Clear the Step data of the day
//                    runCatchingWithLog {
////                        syncDataRepository.saveTodayStep(userId, null)
//                    }
                runCatchingWithLog {
                    Timber.tag(TAG).i("getDeviceInfo")
                    UNIWatchMate?.wmSync?.syncDeviceInfoData?.syncData(System.currentTimeMillis())?.toObservable()?.asFlow()?.collect{
                        Timber.tag(TAG).i("getDeviceInfo=\n$it")
                    }
                }
                runCatchingWithLog {
                    Timber.tag(TAG).i("settingDateTime")
                    val nowMillis = System.currentTimeMillis()
                    val wmDateTime =
                        WmDateTime(TimeZone.getDefault().id,
                            WmUnitInfo.TimeFormat.TWENTY_FOUR_HOUR,
                            WmUnitInfo.DateFormat.YYYY_MM_DD,
                            nowMillis,
                            TimeUtils.millis2String(nowMillis, TimeUtils.getDefaultHMSFormat()),
                            TimeUtils.millis2String(nowMillis, TimeUtils.getDefaultYMDFormat()))
                    Timber.tag(TAG).i("settingDateTime wmDateTime=${wmDateTime}")
                    UNIWatchMate?.wmSettings?.settingDateTime?.set(wmDateTime).await()
                }
                runCatchingWithLog {
                    sportGoalRepository.flowCurrent.value?.let {
                        if (flowConnectorState.value == WmConnectState.VERIFIED) {
                            Timber.tag(TAG).i("setExerciseGoal")
                            applicationScope.launchWithLog {
                                UNIWatchMate?.wmSettings?.settingSportGoal?.set(it)?.await()
                            }
                        }
                    }
                }
                runCatchingWithLog {
                    userInfoRepository.flowCurrent.value?.let {
                        val birthDate = WmPersonalInfo.BirthDate(
                            it.birthYear.toShort(),
                            it.birthMonth.toByte(),
                            it.birthDay.toByte()
                        )
                        val wmPersonalInfo = WmPersonalInfo(
                            it.height.toShort(),
                            it.weight.toShort(),
                            if (it.sex) WmPersonalInfo.Gender.MALE else WmPersonalInfo.Gender.FEMALE,
                            birthDate
                        )
                        Timber.tag(TAG).i("setUserInfo")
                        UNIWatchMate?.wmSettings?.settingPersonalInfo?.set(wmPersonalInfo)?.await()
                    }
                }

                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    syncData()
                }
                Timber.tag(TAG).d("onConnected over")
            }
        } else {
            Timber.tag(TAG).w("onConnected error because no authed user")
        }
    }

    override val flowBattery: StateFlow<WmBatteryInfo?> = flowConnectorState
        .filter {
            it == WmConnectState.VERIFIED
        }
        .flatMapLatest {//flatMap 不同的是，它会取消先前启动的流

            UNIWatchMate?.wmSync?.syncBatteryInfo?.observeSyncData?.startWith(
                UNIWatchMate.wmSync!!.syncBatteryInfo.syncData(System.currentTimeMillis())
            )?.retryWhen {
                it.flatMap { throwable ->
                    Observable.timer(7500, TimeUnit.MILLISECONDS)
                }
            }?.asFlow()
                ?.catch {
                    //catch avoid crash
                } ?: emptyFlow()
        }
        .stateIn(applicationScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L), null)

//    override fun flowWeatherRequire(): Flow<Boolean> {
//        return flowDevice.flatMapLatest {
//            if (it == null || it.isTryingBind) {
//                flowOf(false)
//            } else {
//                connector.configFeature().observerAnyChanged().filter { type ->
//                    type == FcConfigFeature.TYPE_DEVICE_INFO || type == FcConfigFeature.TYPE_FUNCTION_CONFIG
//                }.debounce(1000, TimeUnit.MILLISECONDS).startWithItem(0).asFlow()
//                    .map {
//                        val deviceInfo = connector.configFeature().getDeviceInfo()
//                        val functionConfig = connector.configFeature().getFunctionConfig()
//                        deviceInfo.isSupportFeature(FcDeviceInfo.Feature.WEATHER) &&
//                                functionConfig.isFlagEnabled(FcFunctionConfig.Flag.WEATHER_DISPLAY)
//                    }
//            }
//        }
//    }

    override fun bind(address: String, name: String) {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId == null) {
            Timber.tag(TAG).w("bind error because no authed user")
            return
        }
        deviceFromMemory.value = ConnectorDevice(address, name, true)
        applicationScope.launchWithLog {
            settingDao.clearDeviceBind(userId)
        }
    }

    override fun rebind() {
//        val device = flowDevice.value
//        if (device == null) {
//            Timber.tag(TAG).w("rebind error because no device")
//            return
//        }
//        bind(device.address, device.name)
    }

    override fun cancelBind() {
        val device = deviceFromMemory.value
        if (device != null && device.isTryingBind) {
            deviceFromMemory.value = null
        }
    }

    override suspend fun delDevice() {
//        UNIWatchMate.disconnect()
//        connector.settingsFeature().unbindUser()
//            .ignoreElement().onErrorComplete()
//            .andThen(
//                connector.settingsFeature().unbindAudioDevice().onErrorComplete()
//            ).await()
        clearDevice()
    }

    override suspend fun reset() {
        Timber.tag(TAG).i("reset")
        UNIWatchMate.reset().onErrorReturn {
             Completable.create { emitter -> emitter.onComplete() }
        }.awaitSingleOrNull()
        clearDevice()
    }


    /**
     * Save device with current user
     */
    private suspend fun saveDevice(device: ConnectorDevice) {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId == null) {
            Timber.tag(TAG).w("saveDevice error because no authed user")
            deviceFromMemory.value = null
        } else {
            deviceFromMemory.value = ConnectorDevice(
                device.address, device.name, false
            )
            val entity = DeviceBindEntity(userId, device.address, device.name)
            settingDao.insertDeviceBind(entity)
        }
    }

    /**
     * Clear current user's device
     */
    private suspend fun clearDevice() {
        internalStorage.flowAuthedUserId.value?.let { userId ->
            Timber.tag(TAG).e("clearDeviceBind.userId =$userId")
            settingDao.clearDeviceBind(userId)
        }
        Timber.tag(TAG).e("deviceFromMemory.value = null")
        deviceFromMemory.value = null
    }
//
//    override fun getNextRetrySeconds(): Int {
//        return 0L.coerceAtLeast((connector.getNextRetryTime() - System.currentTimeMillis()) / 1000).toInt()
//    }

//    override fun getDisconnectedReason(): FcDisconnectedReason {
//        return connector.getDisconnectedReason()
//    }

    override fun disconnect() {

        UNIWatchMate.disconnect()
    }

    override fun reconnect() {
//        abUniWatch?.let {
//            it.wmConnect?.disconnect()
//        }
//        connector.reconnect()
    }

//    override fun newDfuManager(): FcDfuManager {
//        return connector.newDfuManager(true)
//    }

    override fun syncData() {
//        if (connector.dataFeature().isSyncing()) {
//            return
//        }
//        applicationScope.launch {
//            connector.dataFeature().syncData().asFlow()
//                .onStart {
//                    Timber.tag(TAG).i("syncData onStart")
//                    _flowSyncEvent.send(DeviceManager.SyncEvent.SYNCING)
//                }
//                .onCompletion {
//                    Timber.tag(TAG).i(it, "syncData onCompletion")
//                    when (it) {
//                        null -> {
//                            _flowSyncEvent.send(DeviceManager.SyncEvent.SUCCESS)
//                            val userId = internalStorage.flowAuthedUserId.value
//                            if (userId != null) {
//                                //Clear all gpsId every time synchronization is completed
//                                //Because if the synchronization is all successful, then GpsData and SportData should have all returned successfully.
////                                syncDataRepository.clearSportGpsId(userId)
//                            }
//                        }
////                        is BleDisconnectedException -> {
////                            _flowSyncEvent.send(DeviceManager.SyncEvent.FAIL_DISCONNECT)
////                        }
////                        !is FcSyncBusyException -> {
////                            _flowSyncEvent.send(DeviceManager.SyncEvent.FAIL)
////                        }
//                    }
//                }
//                .catch {
//                }
//                .collect {
////                    saveSyncData(it)
//                }
//            Timber.tag(TAG).i("syncData finish")
//        }
    }

    //    private suspend fun saveSyncData(data: FcSyncData) {
//        Timber.tag(TAG).i("saveSyncData:%d", data.type)
//        val userId = internalStorage.flowAuthedUserId.value ?: return
//
//        when (data.type) {
//            FcSyncDataType.STEP -> {
//                syncDataRepository.saveStep(userId, data.toStep(), data.deviceInfo.isSupportFeature(FcDeviceInfo.Feature.STEP_EXTRA))
//            }
//
//            FcSyncDataType.SLEEP -> syncDataRepository.saveSleep(userId, data.toSleep())
//
//            FcSyncDataType.HEART_RATE -> syncDataRepository.saveHeartRate(userId, data.toHeartRate())
//            FcSyncDataType.HEART_RATE_MEASURE -> syncDataRepository.saveHeartRate(userId, data.toHeartRateMeasure())
//
//            FcSyncDataType.OXYGEN -> syncDataRepository.saveOxygen(userId, data.toOxygen())
//            FcSyncDataType.OXYGEN_MEASURE -> syncDataRepository.saveOxygen(userId, data.toOxygenMeasure())
//
//            FcSyncDataType.BLOOD_PRESSURE -> syncDataRepository.saveBloodPressure(userId, data.toBloodPressure())
//            FcSyncDataType.BLOOD_PRESSURE_MEASURE -> syncDataRepository.saveBloodPressureMeasure(userId, data.toBloodPressureMeasure())
//
//            FcSyncDataType.TEMPERATURE -> syncDataRepository.saveTemperature(userId, data.toTemperature())
//            FcSyncDataType.TEMPERATURE_MEASURE -> syncDataRepository.saveTemperature(userId, data.toTemperatureMeasure())
//
//            FcSyncDataType.PRESSURE -> syncDataRepository.savePressure(userId, data.toPressure())
//            FcSyncDataType.PRESSURE_MEASURE -> syncDataRepository.savePressure(userId, data.toPressureMeasure())
//
//            FcSyncDataType.ECG -> {
//                syncDataRepository.saveEcg(userId, data.toEcg(), data.deviceInfo.isSupportFeature(FcDeviceInfo.Feature.TI_ECG))
//            }
//
//            FcSyncDataType.GAME -> syncDataRepository.saveGame(userId, data.toGame())
//
//            FcSyncDataType.SPORT -> syncDataRepository.saveSport(userId, data.toSport())
//            FcSyncDataType.GPS -> syncDataRepository.saveGps(userId, data.toGps())
//
//            FcSyncDataType.TODAY_TOTAL_DATA -> syncDataRepository.saveTodayStep(userId, data.toTodayTotal())
//        }
//    }
//    fun parseScanQr(qrString: String): WmScanDevice {
//        val wmScanDevice = WmScanDevice(WmDeviceModel.SJ_WATCH)
//        val params = UrlParse.getUrlParams(qrString)
//        if (!params.isEmpty()) {
//            val schemeMacAddress = params["mac"]
//            val schemeDeviceName = params["projectname"]
//            val random = params["random"]
//
//            wmScanDevice.randomCode = random
//
//            wmScanDevice.address = schemeMacAddress
//            wmScanDevice.isRecognized =
//                !TextUtils.isEmpty(schemeMacAddress) &&
//                        !TextUtils.isEmpty(schemeDeviceName) &&
//                        !TextUtils.isEmpty(random) &&
//                        isLegalMacAddress(schemeMacAddress)
//        }
//        return wmScanDevice
//    }

    private fun isLegalMacAddress(address: String?): Boolean {
        return !TextUtils.isEmpty(address)
    }

    companion object {
        private const val TAG = "DeviceManager"
    }

    private data class ConnectionParam(
        val user: UserInfo?,
        val device: ConnectorDevice?,
    )
}

//private fun simpleState(state: WmConnectState): ConnectorState {
//    return when {
//        state == WmConnectState.DISCONNECTED -> ConnectorState.DISCONNECTED
////        state == WmConnectState.PRE_CONNECTING -> ConnectorState.PRE_CONNECTING
//        state <= WmConnectState.CONNECTING -> {
//            ConnectorState.CONNECTING
//        }
//
//        else -> {
//            ConnectorState.CONNECTED
//        }
//    }
//}

//private fun combineState(
//    device: ConnectorDevice?,
//    isAdapterEnabled: Boolean,
//    connectorState: ConnectorState,
//): ConnectorState {
//    return if (device == null) {
//        ConnectorState.NO_DEVICE
//    } else if (!isAdapterEnabled) {
//        ConnectorState.BT_DISABLED
//    } else {
//        connectorState
//    }
//}

