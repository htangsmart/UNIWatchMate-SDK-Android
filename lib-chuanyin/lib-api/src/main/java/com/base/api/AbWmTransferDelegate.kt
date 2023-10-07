package com.base.api

import com.base.sdk.AbUniWatch
import com.base.sdk.entity.data.*
import com.base.sdk.port.setting.AbWmSetting
import com.base.sdk.port.setting.AbWmSettings
import com.base.sdk.entity.settings.*
import com.base.sdk.port.AbWmTransferFile
import com.base.sdk.port.FileType
import com.base.sdk.port.WmTransferState
import com.base.sdk.port.app.*
import com.base.sdk.port.sync.AbSyncData
import com.base.sdk.port.sync.AbWmSyncs
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

internal class AbWmTransferDelegate(
   private val watchObservable: BehaviorObservable<AbUniWatch>
) : AbWmTransferFile() {

    override fun start(fileType: FileType, file: File): Observable<WmTransferState> {
       return watchObservable.value!!.wmTransferFile.start(fileType, file)
    }

    override fun startMultiple(fileType: FileType, file: List<File>): Observable<WmTransferState> {
        return watchObservable.value!!.wmTransferFile.startMultiple(fileType, file)
    }

    override fun isSupportMultiple(): Boolean {
        return watchObservable.value!!.wmTransferFile.isSupportMultiple()
    }

    override fun cancelTransfer(): Single<Boolean> {
        return watchObservable.value!!.wmTransferFile.cancelTransfer()
    }

    override fun isSupport(): Boolean {
        return watchObservable.value!!.wmTransferFile.isSupport()
    }
}