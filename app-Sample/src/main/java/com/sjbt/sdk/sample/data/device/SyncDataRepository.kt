package com.sjbt.sdk.sample.data.device

import com.sjbt.sdk.sample.data.user.UserInfoRepository
import com.sjbt.sdk.sample.db.AppDatabase
interface SyncDataRepository {

}

internal class SyncDataRepositoryImpl(
    appDatabase: AppDatabase,
    private val userInfoRepository: UserInfoRepository
) : SyncDataRepository {

//    private val stringTypedDao = appDatabase.stringTypedDao()
//    private val syncDao = appDatabase.syncDataDao()

}