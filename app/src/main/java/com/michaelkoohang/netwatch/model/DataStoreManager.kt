package com.michaelkoohang.netwatch.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.michaelkoohang.netwatch.NetWatchApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("data_store")
    private val mDataStore = NetWatchApp.applicationContext().dataStore

    private val ONBOARD_COMPLETE = booleanPreferencesKey("onboard_complete")
    private val DEVICE_ID = stringPreferencesKey("device_id")
    private val CARRIER = stringPreferencesKey("carrier")

    suspend fun saveOnboardComplete(complete: Boolean) {
        mDataStore.edit { dataStore ->
            dataStore[ONBOARD_COMPLETE] = complete
        }
    }

    suspend fun saveDeviceId(id: String) {
        mDataStore.edit { dataStore ->
            dataStore[DEVICE_ID] = id
        }
    }

    suspend fun saveCarrier(carrier: String) {
        mDataStore.edit { dataStore ->
            dataStore[CARRIER] = carrier
        }
    }

    val onboardCompleteFlow: Flow<Boolean> = mDataStore.data.map { dataStore ->
        dataStore[ONBOARD_COMPLETE] ?: false
    }

    val deviceIdFlow: Flow<String> = mDataStore.data.map { dataStore ->
        dataStore[DEVICE_ID] ?: ""
    }

    val carrierFlow: Flow<String> = mDataStore.data.map { dataStore ->
        dataStore[CARRIER] ?: ""
    }
}