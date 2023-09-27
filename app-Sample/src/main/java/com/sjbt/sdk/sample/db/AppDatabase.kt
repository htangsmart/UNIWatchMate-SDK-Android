package com.sjbt.sdk.sample.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sjbt.sdk.sample.entity.DeviceBindEntity
import com.sjbt.sdk.sample.entity.SportGoalEntity
import com.sjbt.sdk.sample.entity.UserEntity
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor

@Database(
    version = 1,
    entities = [SportGoalEntity::class,UserEntity::class,DeviceBindEntity::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingDao
    abstract fun userDao(): UserDao

    companion object {
        private const val DB_NAME = "db_sample"
        fun build(context: Context, ioDispatcher: CoroutineDispatcher): AppDatabase {
            val database = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .setQueryExecutor(ioDispatcher.asExecutor())
                //Because this is a sample, version migration is not necessary. So use destructive recreate to avoid crash.
                .fallbackToDestructiveMigration()
                .build()
            return database
        }
    }
}