package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmSport
import com.base.sdk.port.app.AbAppSport
import com.sjbt.sdk.SJUniWatch
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AppSport(val sjUniWatch: SJUniWatch) : AbAppSport() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override var syncSportList: Observable<List<WmSport>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun addSport(sport: WmSport): Single<WmSport> {
        TODO("Not yet implemented")
    }

    override fun deleteSport(sport: WmSport): Single<WmSport> {
        TODO("Not yet implemented")
    }

    override fun sortFixedSportList(list: List<WmSport>): Single<Boolean> {
        TODO("Not yet implemented")
    }
}