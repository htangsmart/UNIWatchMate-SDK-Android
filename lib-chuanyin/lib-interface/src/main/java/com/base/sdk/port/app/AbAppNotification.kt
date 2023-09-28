package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmNotification
import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Single

/**
 * App-notification 应用模块-通知
 */
abstract class AbAppNotification :IWmSupport {
    /**
     * sendNotification 发送通知
     */
    abstract fun sendNotification(): Single<List<WmNotification>>
}