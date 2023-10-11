package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmLanguage
import com.base.sdk.port.app.AbAppLanguage
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.spp.cmd.CmdHelper
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class AppLanguage(val sjUniWatch: SJUniWatch) : AbAppLanguage() {
    lateinit var languageListEmitter: SingleEmitter<List<WmLanguage>>
    lateinit var languageSetEmitter: SingleEmitter<WmLanguage>

    override fun isSupport(): Boolean {
        return true
    }

    override var syncLanguageList: Single<List<WmLanguage>> = Single.create {
        languageListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadLanguageListCmd())
    }

    override fun setLanguage(language: WmLanguage): Single<WmLanguage> {
        return Single.create {
            languageSetEmitter = it
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteLanguageCmd(language.name))
        }
    }
}