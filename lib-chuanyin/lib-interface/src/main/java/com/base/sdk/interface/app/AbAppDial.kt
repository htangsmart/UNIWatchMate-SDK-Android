package com.base.sdk.`interface`.app

import com.base.sdk.entity.apps.WmDial
import com.base.sdk.`interface`.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * App - Dial（应用模块 - 表盘）
 */
abstract class AbAppDial : IWmSupport {
    /**
     * 同步表盘列表
     */
    abstract var syncDialList : Observable<WmDial>

    /**
     * 删除表盘
     */
    abstract fun deleteDial(dialItem: WmDial): Single<WmDial>
}