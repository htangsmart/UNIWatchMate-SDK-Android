package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import kotlinx.coroutines.runBlocking
import java.util.*

class HeartRateFragment : DataListFragment<HeartRateItemEntity>() {

    override val valueFormat: DataListAdapter.ValueFormat<HeartRateItemEntity> = object : DataListAdapter.ValueFormat<HeartRateItemEntity> {
        override fun format(context: Context, obj: HeartRateItemEntity): String {
            return timeFormat.format(obj.time) + "    " +
                    context.getString(R.string.unit_bmp_unit, obj.heartRate)
        }
    }

    override fun queryData(date: Date): List<HeartRateItemEntity>? {
        return runBlocking { syncDataRepository.queryHeartRate(authedUserId, date) }
    }

}

