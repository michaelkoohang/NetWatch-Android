package com.michaelkoohang.netwatch.model.repos

import com.michaelkoohang.netwatch.model.DataStoreManager
import com.michaelkoohang.netwatch.model.api.ApiClient
import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import com.michaelkoohang.netwatch.model.api.upload.UploadResponse
import com.michaelkoohang.netwatch.model.db.DatabaseManager
import com.michaelkoohang.netwatch.service.MeasureService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import retrofit2.Callback

object RecordingRepository {
    /**
     * Retrieves hikes from the API
     */
    fun getRecordingData(callback: Callback<MutableList<RecordingResponse>>) {
        val deviceId = runBlocking {
            DataStoreManager.deviceIdFlow.first()
        }
        ApiClient.apiService.getRecordings(deviceId).enqueue(callback)
    }

    /**
     * Uploads hikes to the API
     */
    fun uploadRecordingData(callback: Callback<UploadResponse>) {
        val deviceId = runBlocking {
            DataStoreManager.deviceIdFlow.first()
        }
        val payload = DatabaseManager.getRecordings()
        if (payload.size > 0 && !MeasureService.recordingInProgress) {
            ApiClient.apiService.uploadRecordings(deviceId, payload).enqueue(callback)
        }
    }
}