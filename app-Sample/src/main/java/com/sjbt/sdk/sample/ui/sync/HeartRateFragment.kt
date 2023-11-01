package com.sjbt.sdk.sample.ui.sync

import android.content.Context
import com.base.api.UNIWatchMate
import com.base.sdk.entity.data.WmHeartRateData
import com.base.sdk.entity.settings.WmHeartRateAlerts
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.entity.HeartRateItemEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import java.util.*

class HeartRateFragment : DataListFragment<WmHeartRateData>() {

    override val valueFormat: DataListAdapter.ValueFormat<WmHeartRateData> = object : DataListAdapter.ValueFormat<WmHeartRateData> {
        override fun format(context: Context, obj: WmHeartRateData): String {
            return timeFormat.format(obj.timestamp) + "    " +
                    context.getString(R.string.unit_bmp_unit, obj.avgHeartRate)
        }
    }

    override fun queryData(date: Date): List<WmHeartRateData>? {
        return runBlocking {
            UNIWatchMate.wmSync.syncHeartRateData.syncData(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
                .await()

//            UNIWatchMate.wmSync.syncHeartRateData
//            UNIWatchMate.wmSync.syncRealtimeRateData.syncData(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
//                .await()

//            syncDataRepository.queryHeartRate(authedUserId, date)
        }
    }

}

