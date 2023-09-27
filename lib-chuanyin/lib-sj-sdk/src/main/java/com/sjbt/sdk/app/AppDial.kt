package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmDial
import com.base.sdk.`interface`.app.AbAppDial
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AppDial: AbAppDial() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override var syncDialList: Observable<WmDial>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun deleteDial(dialItem: WmDial): Single<WmDial> {
        TODO("Not yet implemented")
    }
}