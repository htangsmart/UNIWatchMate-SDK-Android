package com.sjbt.sdk.entity

data class DivideInfo(val divideType: Byte, val payloadPackTotalLen: Short) {
    override fun toString(): String {
        return "DivideInfo(divideType=$divideType, value=$payloadPackTotalLen)"
    }
}