package com.base.sdk.entity.settings
/**
 * Personal information(个人信息)
 */
data class WmPersonalInfo(
    /**
     * Height(身高)
     */
    val height: Short,
    /**
     * Weight(体重)
     */
    val weight: Short,
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

    data class BirthDate(val year: Short, val month: Byte, val day: Byte)

    override fun toString(): String {
        return "WmPersonalInfo(height='$height', weight='$weight', gender='$gender', birthDate=$birthDate)"
    }
}
