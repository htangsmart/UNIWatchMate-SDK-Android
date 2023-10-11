package com.sjbt.sdk.sample.ui.device.dial.library

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.port.FileType
import com.github.kilnn.tool.dialog.prompt.PromptDialogHolder
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.utils.showFailed
import com.topstep.fitcloud.sdk.exception.FcDfuException
import com.topstep.fitcloud.sdk.v2.dfu.FcDfuManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DfuViewModel : ViewModel() {

    sealed class DfuEvent {
        object OnSuccess : DfuEvent()
        class OnFail(val error: Throwable) : DfuEvent()
    }

    private val deviceManager = Injector.getDeviceManager()

    private val _flowDfuEvent = Channel<DfuEvent>()
    val flowDfuEvent = _flowDfuEvent.receiveAsFlow()

    private var dfuJob: Job? = null

    fun startDfu(dialMock: DialMock) {
        dfuJob?.cancel()
        dfuJob = viewModelScope.launch {
            try {
                val transCoverState =
                    UNIWatchMate.wmTransferFile.startTransfer(FileType.DIAL_COVER, mutableListOf())
                        .concatMap {
                        UNIWatchMate.wmTransferFile.startTransfer(FileType.DIAL, mutableListOf())
                    }
                _flowDfuEvent.send(DfuEvent.OnSuccess)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _flowDfuEvent.send(DfuEvent.OnFail(e))
                }
            }
        }
    }

    fun setGUICustomDialComponent(spaceIndex: Int, styleIndex: Int) {
        applicationScope.launch {
            try {
//                withTimeout(90 * 1000) {
//                    deviceManager.flowState.filter { it == ConnectorState.CONNECTED }.first()
//                }
//                deviceManager.settingsFeature.setDialComponent(
//                    spaceIndex,
//                    byteArrayOf(styleIndex.toByte())
//                ).await()
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    fun isDfuIng(): Boolean {
        return dfuJob?.isActive == true
    }

    override fun onCleared() {
        super.onCleared()
    }
}

/**
 * General prompt message for Dfu
 */
fun PromptDialogHolder.showDfuFail(context: Context, throwable: Throwable) {
    Timber.w(throwable)
    var toastId = 0
    if (throwable is FcDfuException) {
        val errorType = throwable.errorType
        val errorCode = throwable.errorCode
        when (errorType) {
        }

    } else {
        showFailed(throwable)
    }
}