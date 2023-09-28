package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmFind
import com.base.sdk.port.app.AbAppFind
import com.base.sdk.port.app.StopType
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AppFind : AbAppFind() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override var observeFindMobile: Observable<WmFind>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun stopFindMobile(): Observable<StopType> {
        TODO("Not yet implemented")
    }

    override fun findWatch(ring_count: WmFind): Single<Boolean> {
        TODO("Not yet implemented")
    }

    override fun stopFindWatch(flag: StopType): Single<Boolean> {
        TODO("Not yet implemented")
    }
}