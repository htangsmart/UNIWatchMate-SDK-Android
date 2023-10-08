package com.sjbt.sdk.utils

import java.io.File
import java.io.FileInputStream

fun File.readFileBytes(): ByteArray? {
    if (this.exists()) {
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(this)
            val length = fis.available()
            val buffer = ByteArray(length)
            fis.read(buffer)
            return buffer
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    return null
}