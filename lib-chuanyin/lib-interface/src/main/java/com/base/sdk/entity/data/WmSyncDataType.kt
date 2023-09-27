package com.base.sdk.entity.data

import androidx.annotation.IntDef

@IntDef(
    WmSyncDataType.STEP,
    WmSyncDataType.SLEEP,
    WmSyncDataType.HEART_RATE,
    WmSyncDataType.OXYGEN,
    WmSyncDataType.BLOOD_PRESSURE,
    WmSyncDataType.SPORT,
    WmSyncDataType.HEART_RATE_MEASURE,
    WmSyncDataType.OXYGEN_MEASURE,
    WmSyncDataType.BLOOD_PRESSURE_MEASURE,
    WmSyncDataType.TODAY_TOTAL_DATA,
)
@Retention(AnnotationRetention.BINARY)
annotation class WmSyncDataType {
    companion object {
        /**
         * Data type of step(步数数据)
         */
        const val STEP = 0x01

        /**
         * Data type of sleep(睡眠数据)
         */
        const val SLEEP = 0x02 //睡眠数据

        /**
         * Data type of heart rate(心率数据)
         */
        const val HEART_RATE = 0x03 //心率数据

        /**
         * Data type of oxygen(血氧数据)
         */
        const val OXYGEN = 0x04 //血氧数据

        /**
         * Data type of blood pressure(血压数据)
         */
        const val BLOOD_PRESSURE = 0x05 //血压数据

        /**
         * Data type of sport(运动数据)
         */
        const val SPORT = 0x10 //运动数据

        /**
         * Data type of Heart rate value measured manually(手动测量心率数据)
         */
        const val HEART_RATE_MEASURE = 0x83

        /**
         * Data type of oxygen value measured manually(手动测量血氧数据)
         */
        const val OXYGEN_MEASURE = 0x84 //手动测量血氧数据

        /**
         * Data type of blood pressure value measured manually
         */
        const val BLOOD_PRESSURE_MEASURE = 0x85 //手动测量血压数据

        /**
         * Data type of day total data
         */
        const val TODAY_TOTAL_DATA = 0xFF //总数据
    }
}