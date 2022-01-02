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
    private val MEASUREMENT_FREQUENCY = stringPreferencesKey("measurement_frequency")

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

    suspend fun saveMeasurementFrequency(frequency: String) {
        mDataStore.edit { dataStore ->
            dataStore[MEASUREMENT_FREQUENCY] = frequency
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

    val measurementFrequencyFlow: Flow<String> = mDataStore.data.map { dataStore ->
        dataStore[MEASUREMENT_FREQUENCY] ?: "10 s"
    }
}