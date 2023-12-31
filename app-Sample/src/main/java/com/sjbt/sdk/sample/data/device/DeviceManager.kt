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
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.TimeUtils
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.data.config.SportGoalRepository
import com.sjbt.sdk.sample.data.user.UserInfoRepository
import com.sjbt.sdk.sample.db.AppDatabase
import com.sjbt.sdk.sample.entity.DeviceBindEntity
import com.sjbt.sdk.sample.entity.toModel
import com.sjbt.sdk.sample.model.device.ConnectorDevice
import com.sjbt.sdk.sample.model.device.deviceModeToInt
import com.sjbt.sdk.sample.model.user.UserInfo
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.runCatchingWithLog
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
     * [SyncEvent]
     */
    val flowSyncEvent: Flow<Int>

    /**
     * Trying bind a new device.
     * If bind success, the device info will be automatically saved to storage
     */
    fun bind(address: String, name: String, wmDeviceMode: WmDeviceModel)


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
     *  ConnectorDevice
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
            Timber.e( "device fromMemory:$fromMemory , fromStorage:$fromStorage")
            check(fromStorage == null || !fromStorage.isTryingBind)//device fromStorage, isTryingBind must be false

            //Use device fromMemory first
            fromMemory ?: fromStorage
        }.stateIn(applicationScope, SharingStarted.Eagerly, null)


    /**
     * Connector state combine adapter state and current device
     */
    override val flowConnectorState = combine(
        flowDevice,
        UNIWatchMate.observeConnectState.startWithItem(WmConnectState.DISCONNECTED)
            .asFlow().distinctUntilChanged()
    ) { device, connectorState ->
        //Device trying bind success,save it
        Timber.e("flowConnectorState flowDevice == ${flowDevice.value}  connectorState == $connectorState"
        )
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
            deviceFromStorage.collect { device ->
                Timber.e( "it.device == $device")
                if (deviceFromMemory.value == null) {
                    internalStorage.flowAuthedUserId.value?.let {
                        val userInfo = userInfoRepository.getUserInfo(it)
                        userInfo?.let { userInfo ->
                            device?.let { storageDevice ->
                                Timber.i(  "UNIWatchMate.connect")
                                UNIWatchMate.connect(
                                    address = device!!.address,
                                    WmBindInfo(
                                        userInfo.id.toString(),
                                        "name",
                                        BindType.DISCOVERY,
                                        storageDevice.wmDeviceMode
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        applicationScope.launch {
            flowConnectorState.collect {
                Timber.i( "onConnected if verified state:$it")
                if (it == WmConnectState.VERIFIED) {
                    onConnected()
                }
            }
        }

    }

    private fun onConnected() {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId != null) {
            applicationScope.launchWithLog {
                if (CacheDataHelper.getSynchronizingData()) {
                    return@launchWithLog
                }
                CacheDataHelper.setSynchronizingData(true)
//                showLoadingDialog()
                runCatchingWithLog {
                    Timber.d(  "getDeviceInfo")
                    val deviceInfo =
                        UNIWatchMate.getDeviceInfo()
                            .await()
                    Timber.d("getDeviceInfo=\n$deviceInfo")
                    CacheDataHelper.setCurrentDeviceInfo(deviceInfo)
                }.onFailure {
                    ToastUtil.showToast(it.message,true)
                }
                runCatchingWithLog {
                    val result = UNIWatchMate?.wmApps?.appDateTime?.setDateTime(null).await()
                    Timber.d(  "settingDateTime wmDateTime=${result}")
                }.onFailure {
                    ToastUtil.showToast(it.message,true)
                }
                runCatchingWithLog {
                    //first check has data,if not ,get from watch
                    sportGoalRepository.flowCurrent.value?.let {
                        if (flowConnectorState.value == WmConnectState.VERIFIED) {
                            if (it.activityDuration == 0.toShort() || it.calories == 0 || it.steps == 0) {
                                val sportGoal =
                                    UNIWatchMate.wmSettings.settingSportGoal.get().await()
                                sportGoalRepository.modify(userId, sportGoal)
                                Timber.d( "modify sportGoal= $sportGoal")
                            } else {
                                val result =
                                    UNIWatchMate.wmSettings.settingSportGoal.set(it).await()
                                Timber.d(  "setExerciseGoal $result")
                            }
                        }
                    }
                }.onFailure {
                    ToastUtil.showToast(it.message,true)
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
                        Timber.i(  "setUserInfo $wmPersonalInfo")
                        UNIWatchMate?.wmSettings?.settingPersonalInfo?.set(wmPersonalInfo)?.await()
                    }
                }.onFailure {
                    ToastUtil.showToast(it.message,true)
                }
                Timber.i(  "onConnected over")
                hideLoadingDialog()
                CacheDataHelper.setSynchronizingData(false)
            }
        } else {
            UNIWatchMate.wmLog.logW(TAG, "onConnected error because no authed user")
        }
    }

    private fun showLoadingDialog() {
        if (ActivityUtils.getTopActivity() != null && ActivityUtils.getTopActivity() is BaseActivity) {
            (ActivityUtils.getTopActivity() as BaseActivity).showLoadingDlg()
        }
    }

    private fun hideLoadingDialog() {
        if (ActivityUtils.getTopActivity() != null && ActivityUtils.getTopActivity() is BaseActivity) {
            (ActivityUtils.getTopActivity() as BaseActivity).hideLoadingDlg()
        }
    }

    override val flowBattery: StateFlow<WmBatteryInfo?> = flowConnectorState
        .filter {
            it == WmConnectState.VERIFIED
        }
        .flatMapLatest {//flatMap 不同的是，它会取消先前启动的流

            UNIWatchMate?.observeBatteryChange?.startWith(
                UNIWatchMate.getBatteryInfo()
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

    override fun bind(address: String, name: String, wmDeviceMode: WmDeviceModel) {
        val userId = internalStorage.flowAuthedUserId.value
        if (userId == null) {
            Timber.e( "bind error because no authed user")
            return
        }
        deviceFromMemory.value = ConnectorDevice(address, name, wmDeviceMode, true,0)
        applicationScope.launchWithLog {
            settingDao.clearDeviceBind(userId)
        }
    }

    override fun cancelBind() {
        val device = deviceFromMemory.value
        if (device != null && device.isTryingBind) {
            deviceFromMemory.value = null
        }
    }

    override suspend fun delDevice() {
        clearDevice()
    }

    override suspend fun reset() {
        Timber.d(  "reset")
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
            Timber.w( "saveDevice error because no authed user")
            deviceFromMemory.value = null
        } else {
            deviceFromMemory.value = ConnectorDevice(
                device.address, device.name, device.wmDeviceMode, false,1
            )
            val entity = DeviceBindEntity(
                userId,
                device.address,
                device.name,
                device.deviceModeToInt(),
                device.connectState
            )
            settingDao.insertDeviceBind(entity)
        }
    }

    /**
     * Clear current user's device
     */
    private suspend fun clearDevice() {
        internalStorage.flowAuthedUserId.value?.let { userId ->
            Timber.e("clearDeviceBind.userId =$userId")
            settingDao.clearDeviceBind(userId)
        }
        Timber.e("deviceFromMemory.value = null")
        deviceFromMemory.value = null
    }
//
//    override fun getNextRetrySeconds(): Int {
//        return 0L.coerceAtLeast((connector.getNextRetryTime() - System.currentTimeMillis()) / 1000).toInt()
//    }

//    override fun getDisconnectedReason(): FcDisconnectedReason {
//        return connector.getDisconnectedReason()
//    }


//    override fun newDfuManager(): FcDfuManager {
//        return connector.newDfuManager(true)
//    }

    override fun syncData() {
//        if (connector.dataFeature().isSyncing()) {
//            return
//        }
    }

    companion object {
        private const val TAG = "DeviceManager"
    }

    private data class ConnectionParam(
        val user: UserInfo?,
        val device: ConnectorDevice?,
    )
}