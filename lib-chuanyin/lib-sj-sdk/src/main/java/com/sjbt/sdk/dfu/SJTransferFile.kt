package com.sjbt.sdk.dfu

import com.base.sdk.`interface`.FileType
import com.base.sdk.`interface`.WmTransferFile
import com.base.sdk.`interface`.WmTransferState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

class SJTransferFile : WmTransferFile() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSupportMultiple(): Boolean {
        TODO("Not yet implemented")
    }

    override fun cancelTransfer(): Single<Boolean> {
        TODO("Not yet implemented")
    }

    override fun start(fileType: FileType, file: File): Observable<WmTransferState> {
        TODO("Not yet implemented")
    }

    override fun startMultiple(fileType: FileType, file: List<File>): Observable<WmTransferState> {
        TODO("Not yet implemented")
    }
}