package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmLanguage
import com.base.sdk.port.app.AbAppLanguage
import io.reactivex.rxjava3.core.Single

class AppLanguage : AbAppLanguage() {
    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override var syncLanguageList: Single<List<WmLanguage>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun setLanguage(language: WmLanguage): Single<WmLanguage> {
        TODO("Not yet implemented")
    }
}