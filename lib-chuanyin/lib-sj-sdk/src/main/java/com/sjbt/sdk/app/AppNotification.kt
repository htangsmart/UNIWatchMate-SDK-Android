package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.`interface`.app.AbAppNotification
import io.reactivex.rxjava3.core.Single

class AppNotification: AbAppNotification() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendNotification(): Single<List<WmNotification>> {
        TODO("Not yet implemented")
    }
}