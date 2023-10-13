package com.sjbt.sdk.app

import com.base.sdk.entity.apps.WmDial
import com.base.sdk.port.app.AbAppDial
import com.sjbt.sdk.SJUniWatch
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.spp.cmd.CmdHelper
import com.sjbt.sdk.spp.cmd.DIAL_MSG_LEN
import com.sjbt.sdk.uparser.model.JpgInfo
import com.sjbt.sdk.utils.BtUtils
import com.sjbt.sdk.utils.UParseUtil
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import java.nio.ByteBuffer
import java.util.*

class AppDial(sjUniWatch: SJUniWatch) : AbAppDial() {
    val sjUniWatch = sjUniWatch
    var syncDialListEmitter: ObservableEmitter<List<WmDial>>?=null
    var deleteEmitter: SingleEmitter<WmDial>?=null
    private var wmDial: WmDial? = null

    //表盘列表
    val mMyDialList = ArrayList<WmDial>()

    override fun isSupport(): Boolean {
        return true
    }

    override fun syncDialList(): Observable<List<WmDial>> {
        return Observable.create(object : ObservableOnSubscribe<List<WmDial>> {
            override fun subscribe(emitter: ObservableEmitter<List<WmDial>>) {
                syncDialListEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.getDialListCmd(0))
            }
        })
    }

    override fun deleteDial(dialItem: WmDial): Single<WmDial> {
        wmDial = dialItem;
        return Single.create(object : SingleOnSubscribe<WmDial> {
            override fun subscribe(emitter: SingleEmitter<WmDial>) {
                deleteEmitter = emitter
                sjUniWatch.sendNormalMsg(CmdHelper.getDialActionCmd(2, dialItem.id))
            }
        })
    }

    fun deleteDialResult(result: Boolean) {
        deleteEmitter?.let {
            if (result) {
                it.onSuccess(wmDial)
            } else {
                it.onError(java.lang.RuntimeException("delete fail"))
            }
        }
    }

    fun addDialList(msgBean: MsgBean) {
        if (msgBean.payload == null) {
            return
        }
        val dialIdLen: Int = msgBean.payload.size
        val dialCount: Int = dialIdLen / DIAL_MSG_LEN
//        val order = msgBean.payload.get(0)
        //        LogUtils.logBlueTooth("表盘信息 序号：$order")
        val byteDialAll = ByteArray(dialIdLen)
        System.arraycopy(msgBean.payload, 0, byteDialAll, 0, dialIdLen)
        for (i in 0 until dialCount) {
            val dialItemArray = ByteArray(17)
            System.arraycopy(byteDialAll, DIAL_MSG_LEN * i, dialItemArray, 0, DIAL_MSG_LEN)
            val dialByteBuffer = ByteBuffer.wrap(dialItemArray)
            //            LogUtils.logBlueTooth("表盘信息 完整：${dialItemArray.size}")
            val builtIn = dialByteBuffer.get()
            //            LogUtils.logBlueTooth("表盘信息 内置：$builtIn")
            val byteArrayId = ByteArray(16)
            System.arraycopy(dialItemArray, 1, byteArrayId, 0, byteArrayId.size)
            val id: String = BtUtils.bytesToHexString(byteArrayId).toLowerCase(Locale.ROOT)

//            LogUtils.logBlueTooth("表盘信息 id：$id")
            val dial = WmDial(id, builtIn.toInt())
            mMyDialList.add(dial)
        }
    }

    /**
     * 获取表盘封面
     */
   override fun parseDialThumpJpg(dialPath: String): ByteArray? {
        try {
            val jpgInfo = JpgInfo()
            val result = UParseUtil.getInstance().getJpgFromDial(dialPath, jpgInfo)

            if (result == 0 || jpgInfo.resouceInfo.size == 0 || jpgInfo.jpgdata.size == 0) {

//                val bitmap = BitmapFactory.decodeByteArray(jpgInfo.jpgdata,0,jpgInfo.jpgdata.size);
                val byteBuffer =
                    ByteBuffer.allocate(jpgInfo.resouceInfo.size + jpgInfo.jpgdata.size)
                byteBuffer.put(jpgInfo.resouceInfo)
                byteBuffer.put(jpgInfo.jpgdata)
                return byteBuffer.array()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}