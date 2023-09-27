package com.sjbt.sdk.sample.di

import com.sjbt.sdk.sample.data.config.SportGoalRepository
import com.sjbt.sdk.sample.di.internal.SingleInstance
import com.sjbt.sdk.sample.base.storage.InternalStorage
import com.sjbt.sdk.sample.di.internal.CoroutinesInstance
import com.sjbt.sdk.sample.data.user.UserInfoRepository
import kotlinx.coroutines.CoroutineScope

/**
 * Because some developers may not use dagger or hilt.
 * In order to reduce their learning cost, this sample uses manual injection of dependencies
 */
object Injector {

    //    fun getAuthManager(): AuthManager {
//        return SingleInstance.authManager
//    }
    /**
     * 由于sdk不包含用户注册，使用默认UserId
     * @return
     */
    fun getDefaultUserId(): Long {
        return 1L
    }
//    fun flowAuthedUserId(): StateFlow<Long?> {
//        return SingleInstance.internalStorage.flowAuthedUserId()!!
//    }
    fun getInternalStorage(): InternalStorage {
        return SingleInstance.internalStorage
    }

    //
//    fun getDeviceManager(): DeviceManager {
//        return SingleInstance.deviceManager
//    }
//
    fun getUserInfoRepository(): UserInfoRepository {
        return SingleInstance.userInfoRepository
    }
//
//    fun getVersionRepository(): VersionRepository {
//        return VersionRepositoryImpl(
//            SingleInstance.deviceManager,
//            SingleInstance.apiClient.apiService
//        )
//    }
//
//    fun getGameRepository(): GameRepository {
//        return GameRepositoryImpl(
//            MyApplication.instance,
//            SingleInstance.deviceManager,
//            SingleInstance.apiClient.apiService
//        )
//    }
//
//    fun getSportPushRepository(): SportPushRepository {
//        return SportPushRepositoryImpl(
//            SingleInstance.deviceManager,
//            SingleInstance.apiClient.apiService
//        )
//    }
//
//    fun getDialRepository(): DialRepository {
//        return SingleInstance.dialRepository
//    }
//
    fun getApplicationScope(): CoroutineScope {
        return CoroutinesInstance.applicationScope
    }

    //
//    fun getWomenHealthRepository(): WomenHealthRepository {
//        return SingleInstance.womenHealthRepository
//    }
//
    fun getExerciseGoalRepository(): SportGoalRepository {
        return SingleInstance.sportGoalRepository
    }
//
//    fun getSyncDataRepository(): SyncDataRepository {
//        return SingleInstance.syncDataRepository
//    }
}