package com.base.sdk.port.app

import com.base.sdk.entity.common.WmSport
import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * 应用模块-运动
 */
abstract class AbAppSport : IWmSupport {

    /**
     * syncSportList 同步运动列表
     */
    abstract var syncSportList : Observable<List<WmSport>>

    /**
     * addSport 增加运动项目
     */
    abstract fun addSport(sport: WmSport): Single<WmSport>

    /**
     * deleteSport 删除运动项目
     */
    abstract fun deleteSport(sport: WmSport): Single<WmSport>

    /**
     * sortFixedSportList 运动列表排序
     */
    abstract fun sortFixedSportList(list: List<WmSport>): Single<Boolean>

}