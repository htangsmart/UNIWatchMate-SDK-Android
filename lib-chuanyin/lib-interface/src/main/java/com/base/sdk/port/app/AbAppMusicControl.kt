package com.base.sdk.port.app

import com.base.sdk.entity.apps.WmMusicControlType
import com.base.sdk.port.IWmSupport
import io.reactivex.rxjava3.core.Observable
/**
 * 应用模块-音乐控制
 */
abstract class AbAppMusicControl : IWmSupport {
    /**
     * 监听音乐控制
     */
    abstract var observableMusicControl: Observable<WmMusicControlType>

}