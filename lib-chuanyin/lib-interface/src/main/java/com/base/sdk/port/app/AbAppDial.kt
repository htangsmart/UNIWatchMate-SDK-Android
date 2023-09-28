package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmDial
import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * App - Dial（应用模块 - 表盘）
 */
abstract class AbAppDial : IWmSupport {
    /**
     * 同步表盘列表
     */
    abstract fun syncDialList(index:Byte) : Observable<List<WmDial>>

    /**
     * 删除表盘
     */
    abstract fun deleteDial(dialItem: WmDial): Single<WmDial>
}