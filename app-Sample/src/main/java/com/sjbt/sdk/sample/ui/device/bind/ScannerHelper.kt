package com.sjbt.sdk.sample.ui.device.bind

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.base.api.UNIWatchMate
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.doOnFinish
import com.sjbt.sdk.sample.utils.flowBluetoothAdapterState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScannerHelper(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
) : DefaultLifecycleObserver {


    private var stateJob: Job? = null
    private var scanDisposable: Disposable? = null

    /**
     * Whether an auto scan has been performed.
     * Auto scan only performed once.
     */
    private var isAutoScanned = false

    /**
     * The number of consecutive occurrences of an unknown error
     */
    private var errorUnknownCount = 0

    var listener: Listener? = null

    private val flowPermissionsState = flow {
        val hasPermissions = PermissionHelper.hasBlue(context)
        emit(hasPermissions)
        if (!hasPermissions) {
            while (currentCoroutineContext().isActive && !PermissionHelper.hasBlue(context)) {
                delay(1000)
            }
            //Delay for a while. Sometimes there will be errors in scanning immediately
            delay(500)
            emit(true)
        }
    }.flowOn(Dispatchers.Default)

    private val flowState: Flow<Int> = flowBluetoothAdapterState(context)
        .combine(flowPermissionsState) { isAdapterEnabled, hasPermissions ->
            if (!hasPermissions) {
                STATE_NO_PERMISSION
            } else if (!isAdapterEnabled) {
                STATE_BT_DISABLED
            } else {
                STATE_READY
            }
        }

    private fun getState(): Int {
        val hasPermissions = PermissionHelper.hasBlue(context)
        return if (!hasPermissions) {
            STATE_NO_PERMISSION
        } else if (!bluetoothManager.adapter.isEnabled) {
            STATE_BT_DISABLED
        } else {
            STATE_READY
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        stateJob = owner.lifecycle.coroutineScope.launch {
            flowState.collect {
                checkState(it, true)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        //Cancel scan when onStop
        scanDisposable?.dispose()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stateJob?.cancel()
    }

    /**
     * @return Whether scanning is started
     */
    private fun checkState(state: Int, performedByAuto: Boolean): Boolean {
        if (state == STATE_NO_PERMISSION) {
            listener?.requestPermission()
        } else {
            listener?.bluetoothAlert(state == STATE_BT_DISABLED)
            if (state == STATE_READY) {
                if (!performedByAuto || !isAutoScanned) {
                    isAutoScanned = true
                    scan()
                    return true
                }
            }
        }
        return false
    }

    private fun scan() {
        if (scanDisposable?.isDisposed != false) {
            //It is recommended not to set the scan duration too short
//            scanDisposable = UNIWatchMate.uniWatchSdk?.blockingFirst()?.let {
//                it.startDiscovery().doOnSubscribe {
//                    listener?.onScanStart()
//                }.observeOn(AndroidSchedulers.mainThread())
//                    .doOnFinish {
//                        listener?.onScanStop()
//                    }
//                    .subscribe({
//                        listener?.onScanResult(it)
//                    }, {
//                        //Analysis error
//                        analysisScanError(it)
//                    }, {
//                        errorUnknownCount = 0
//                    })
//            }
        }
    }

    private fun analysisScanError(throwable: Throwable) {
//        val reason = if (throwable is BleScanException) {
//            -30
//        } else {
//            -20
//        }
        val reason = -40
        when (reason) {
            else -> {
                errorUnknownCount++
                if (errorUnknownCount <= 3) {
                    listener?.scanErrorDelayAlert()
                } else {
                    //Prompt the user to restart Bluetooth or Mobile-Phone
                    listener?.scanErrorRestartAlert()
                }
            }
        }
    }

    /**
     * @return Whether scanning is started
     */
    fun start(): Boolean {
        return checkState(getState(), false)
    }

    fun stop() {
        scanDisposable?.dispose()
    }

    fun toggle() {
        if (scanDisposable?.isDisposed != false) {
            start()
        } else {
            stop()
        }
    }

    interface Listener {
        fun requestPermission()
        fun bluetoothAlert(show: Boolean)

        fun scanErrorDelayAlert()
        fun scanErrorRestartAlert()

        fun onScanStart()
        fun onScanStop()
        fun onScanResult(result: BluetoothDevice)
    }

    companion object {
        private const val TAG = "ScannerHelper"
        private const val STATE_NO_PERMISSION = 0
        private const val STATE_BT_DISABLED = 1
        private const val STATE_READY = 2
    }
}