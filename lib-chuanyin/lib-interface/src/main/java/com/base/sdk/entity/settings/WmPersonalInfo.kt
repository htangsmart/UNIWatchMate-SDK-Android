package com.base.sdk.entity.settings
/**
 * Personal information(个人信息)
 */
data class WmPersonalInfo(
    /**
     * Height(身高)
     */
    val height: Int,
    /**
     * Weight(体重)
     */
    val weight: Int,
    /**
     * Gender(性别)
     */
    val gender: Gender,
    /**
     * Birth date(出生日期)
     */
    val birthDate: BirthDate
) {
    enum class Gender {
        MALE, FEMALE, OTHER
    }

    data class BirthDate(val year: Int, val month: Int, val day: Int)

    override fun toString(): String {
        return "WmPersonalInfo(height='$height', weight='$weight', gender='$gender', birthDate=$birthDate)"
    }
}
