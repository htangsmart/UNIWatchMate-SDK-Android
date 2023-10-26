package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmLanguage
import com.base.sdk.port.app.AbAppLanguage
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.ErrorCode
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.entity.NodeData
import com.sjbt.sdk.spp.cmd.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import java.nio.charset.StandardCharsets

class AppLanguage(val sjUniWatch: SJUniWatch) : AbAppLanguage() {
    private var languageListEmitter: SingleEmitter<List<WmLanguage>>? = null
    private var languageSetEmitter: SingleEmitter<WmLanguage>? = null
    private val languageList = mutableListOf<WmLanguage>()

    private val TAG = "AppLanguage"

    private var wmLanguage: WmLanguage? = null

    override fun isSupport(): Boolean {
        return true
    }

    override var syncLanguageList: Single<List<WmLanguage>> = Single.create {
        languageListEmitter = it
        sjUniWatch.sendReadNodeCmdList(CmdHelper.getReadLanguageListCmd())
    }

    override fun setLanguage(language: WmLanguage): Single<WmLanguage> {
        wmLanguage = language
        return Single.create {
            languageSetEmitter = it
            sjUniWatch.sendWriteNodeCmdList(CmdHelper.getWriteLanguageCmd(language.bcp))
        }
    }

    fun onTimeOut(msgBean: MsgBean,nodeData: NodeData) {
        TODO("Not yet implemented")
    }

    fun languageBusiness(
        nodeData: NodeData,
        msgBean: MsgBean?
    ) {
        when (nodeData.urn[2]) {
            URN_SETTING_LANGUAGE_LIST -> {

                msgBean?.let {
                    if (it.divideType == DIVIDE_N_2 || it.divideType == DIVIDE_Y_F_2) {
                        languageList.clear()
                    }
                }

                val languageCount = nodeData.data.size / 6
                var currLanguageBcp = ""

                for (i in 0 until languageCount) {
                    val bcpArray =
                        nodeData.data.copyOfRange(6 * i, 6 * i + 6).takeWhile { it > 0 }
                            .toByteArray()

//                    sjUniWatch.wmLog.logE(TAG, "language bcpArray:" + bcpArray.size)

                    val bcp = String(bcpArray, StandardCharsets.UTF_8)

//                    sjUniWatch.wmLog.logE(TAG, "language bcp:" + bcp)

                    if (i != 0) {
                        val language = WmLanguage(bcp, "", bcp == currLanguageBcp)
                        languageList.add(language)
                    } else {
                        currLanguageBcp = bcp
                    }
                }

                languageListEmitter?.onSuccess(languageList)
            }

            URN_SETTING_LANGUAGE_SET -> {
                wmLanguage?.let {
                    val result = nodeData.data[0].toInt() == ErrorCode.ERR_CODE_OK.ordinal
                    if (result) {
                        languageSetEmitter?.onSuccess(it)
                    } else {
                        languageSetEmitter?.onError(RuntimeException("set fail"))
                    }
                }
            }
        }
    }
}