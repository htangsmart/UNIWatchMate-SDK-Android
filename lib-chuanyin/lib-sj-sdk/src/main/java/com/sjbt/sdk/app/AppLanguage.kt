package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmLanguage
import com.base.sdk.port.app.AbAppLanguage
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.charset.StandardCharsets

class AppLanguage(val sjUniWatch: SJUniWatch) : AbAppLanguage() {
    var languageListEmitter: SingleEmitter<List<WmLanguage>>? = null
    var languageSetEmitter: SingleEmitter<WmLanguage>? = null
    val languageList = mutableListOf<WmLanguage>()

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
            sjUniWatch.sendExecuteNodeCmdList(CmdHelper.getExecuteLanguageCmd(language.bcp))
        }
    }

    fun languageBusiness(
        it: NodeData,
        msgBean: MsgBean?
    ) {
        when (it.urn[2]) {
            URN_SETTING_LANGUAGE_LIST -> {

                msgBean?.let {
                    if (it.divideType == DIVIDE_N_2 || it.divideType == DIVIDE_Y_F_2) {
                        languageList.clear()
                    }
                }

                val languageCount = it.data.size / 6
                for (i in 0 until languageCount) {
                    val bcp = String(
                        it.data.copyOfRange(6 * i, 6 * i + 6),
                        StandardCharsets.UTF_8
                    )
                    val language = WmLanguage(bcp, "", i == 0)
                    languageList.add(language)
                }

                languageListEmitter?.onSuccess(languageList)
            }

            URN_SETTING_LANGUAGE_SET -> {

            }
        }
    }
}