package com.sjbt.sdk.sample.ui.device.dial.library

import androidx.lifecycle.viewModelScope
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.AsyncViewModel
import com.sjbt.sdk.sample.base.SingleAsyncState
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.model.user.DialMock
import kotlinx.coroutines.launch

/**
 * ViewModel only use for request [DialPushParams]
 */
class DialPushViewModel : AsyncViewModel<SingleAsyncState<DialMock>>(SingleAsyncState()) {

    val deviceManager = Injector.getDeviceManager()
    init {
        viewModelScope.launch {
            deviceManager.flowStateConnected().collect {
                //Refresh every time device connected, because the DialPushParams may change
                if (it) {
                    refresh()
                }
            }
        }
    }

    /**
     * Refresh [DialPushParams]
     */
    fun refresh() {
        suspend {
//            生成表盘数据
            DialMock(R.mipmap.a1245156a62de4d6d8d60d8f8ff751302,"123")
        }.execute(SingleAsyncState<DialMock>::async) {
            copy(async = it)
        }
    }

}
