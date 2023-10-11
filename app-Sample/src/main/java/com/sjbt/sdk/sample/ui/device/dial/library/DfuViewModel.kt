package com.sjbt.sdk.sample.ui.device.dial.library

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.api.UNIWatchMate
import com.base.sdk.port.FileType
import com.base.sdk.port.WmTransferState
import com.blankj.utilcode.util.FileIOUtils
import com.github.kilnn.tool.dialog.prompt.PromptDialogHolder
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance.applicationScope
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.utils.showFailed
import com.topstep.fitcloud.sdk.exception.FcDfuException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.awaitLast
import okhttp3.internal.wait
import timber.log.Timber
import java.io.File


class DfuViewModel : ViewModel() {

    sealed class DfuEvent {
        object OnSuccess : DfuEvent()
        class OnFail(val error: Throwable) : DfuEvent()
    }

    private val deviceManager = Injector.getDeviceManager()

    private val _flowDfuEvent = Channel<DfuEvent>()
    val flowDfuEvent = _flowDfuEvent.receiveAsFlow()

    private var dfuJob: Job? = null

    fun startDfu(dialMock: DialMock, callBack: CallBack<WmTransferState>) {
        dfuJob?.cancel()
        dfuJob = viewModelScope.launch {
            try {
                val inputStream = MyApplication.instance.assets.open(dialMock.dialAssert!!)
                val curMillis = System.currentTimeMillis()
                val dialPath =
                    MyApplication.instance.filesDir.absolutePath + "/" + curMillis + ".dial"
                val coverPath =
                    MyApplication.instance.filesDir.absolutePath + "/" + curMillis + ".jpg"
                FileIOUtils.writeFileFromIS(dialPath, inputStream)
                val dialCoverArray = UNIWatchMate.wmApps.appDial.parseDialThumpJpg(dialPath)

                FileIOUtils.writeFileFromBytesByChannel(coverPath, dialCoverArray, true)
                val coverList = mutableListOf<File>()
                val dialList = mutableListOf<File>()
                coverList.add(File(coverPath))
                dialList.add(File(dialPath))
                UNIWatchMate.wmTransferFile.startTransfer(FileType.DIAL_COVER, coverList)
                    .asFlow().collect {
                        callBack.callBack(it)
                    }
                UNIWatchMate.wmLog.logI("DfuViewModel","startTransfer DIAL")
                UNIWatchMate.wmTransferFile.startTransfer(FileType.DIAL, dialList)
                    .asFlow().collect {
                        callBack.callBack(it)
                    }
                _flowDfuEvent.send(DfuEvent.OnSuccess)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
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