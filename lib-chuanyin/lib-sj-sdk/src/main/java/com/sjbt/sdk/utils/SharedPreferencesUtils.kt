package com.sjbt.sdk.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtils private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    companion object {

        private const val PREFS_NAME = "my_prefs"

        @Volatile
        private var instance: SharedPreferencesUtils? = null

        fun getInstance(context: Context): SharedPreferencesUtils {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesUtils(context).also { instance = it }
            }
        }
    }
}
