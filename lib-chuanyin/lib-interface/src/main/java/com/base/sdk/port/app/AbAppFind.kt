package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmFind
import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * (app-find)应用模块-查找功能
 */
abstract class AbAppFind : IWmSupport {

    /**
     * find mobile(查找手机)
     * @return 0:连续响 其它：响铃次数
     */
    abstract var observeFindMobile : Observable<WmFind>

    /**
     * stop find mobile(停止查找手机)
     * @return 0:停止声音和关闭界面 1:停止声音 2:关闭界面
     */
    abstract fun stopFindMobile(): Observable<StopType>

    /**
     * find watch(查找手表)
     * @param ring_count 铃声次数
     */
    abstract fun findWatch(ring_count: WmFind): Single<Boolean>

    /**
     * stop find watch(停止查找手表)
     *
     * @param flag 0:停止声音和关闭界面 1:停止声音 2:关闭界面
     * @return
     */
    abstract fun stopFindWatch(flag: StopType): Single<Boolean>

}

enum class StopType(val type: Int) {
    STOP_VOICE(1), STOP_UI(2), STOP_ALL(0)
}