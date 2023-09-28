package com.sjbt.sdk.sample.model.user

import com.base.sdk.port.AbWmConnect

data class UserInfo(
    val id: Long,
    var height: Int,//user height(cm)
    var weight: Int,//user weight(kg)
    var sex: Boolean,//True for male, false for female
    var birthYear: Int,
    var birthMonth: Int,
    var birthDay: Int
)

fun UserInfo?.getBirthday(): String {
    if (this != null) {
        return "${birthYear}年${birthMonth}月${birthDay}日"
    }
    return ""
}
/**
 * Calculate step size in meters
 */
fun UserInfo?.getStepLength(): Float {
    var height = 0f
    var man = false
    if (this != null) {
        height = this.height.toFloat()
        man = this.sex
    }
    var stepLength = height * if (man) 0.415f else 0.413f
    if (stepLength < 30) {
        stepLength = 30f
    }
    if (stepLength > 100) {
        stepLength = 100f
    }
    return stepLength / 100
}

/**
 * Obtain body weight in kilograms
 */
fun UserInfo?.getWeight(): Float {
    var weight = 0f
    if (this != null) {
        weight = this.weight.toFloat()
    }
    if (weight <= 0f) {
        weight = 50f
    }
    return weight
}

fun UserInfo?.toSdkUser(): AbWmConnect.UserInfo {
    return AbWmConnect.UserInfo("${this?.id ?:0} ", "name")
}
