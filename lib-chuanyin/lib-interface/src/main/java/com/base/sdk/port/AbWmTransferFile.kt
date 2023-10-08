package com.base.sdk.port

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

/**
 * 传输文件功能抽象类
 */
abstract class AbWmTransferFile {
    abstract fun isSupport(fileType: FileType): Boolean
    abstract fun startTransfer(fileType: FileType, files: List<File>): Observable<WmTransferState>
    abstract fun cancelTransfer(): Single<Boolean>
}

/**
 * 传输文件类型
 */
enum class FileType {
    OTA,//设备ota
    DIAL,//表盘
    MUSIC,//MP3类型
    TXT,//Txt电子书
    SPORT,//运动文件（备用未定）
}

/**
 * 传输状态
 */
class WmTransferState(
    state: State,//传输任务总体状态
    success: Boolean,//当前文件是否传输成功
    total: Int,//传输文件个数
    sendingFile: File//返回当前传输的文件
) {
    var progress: Int = 0//当前文件传输进度
    var index: Int = 0 //正在传输第几个文件
}

/**
 * 文件传输任务状态
 */
enum class State {
    PRE_TRANSFER,
    TRANSFERRING,
    ALL_FINISH
}