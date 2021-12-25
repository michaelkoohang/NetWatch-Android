package com.michaelkoohang.netwatch.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaelkoohang.netwatch.model.api.ApiError
import com.michaelkoohang.netwatch.model.repos.RecordingRepository
import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import com.michaelkoohang.netwatch.utils.FormatManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class RecordingsViewModel : ViewModel() {
    private val recordings = MutableLiveData<ArrayList<RecordingResponse>>()
    private val selectedRecording = MutableLiveData<RecordingResponse>()
    private val error = MutableLiveData<ApiError>()

    /**
     * Callback that handles response from API
     */
    private val recordingsCallback = object : Callback<MutableList<RecordingResponse>> {
        fun selector(h: RecordingResponse): Long = FormatManager.getEpochTime(h.start!!)

        override fun onFailure(call: Call<MutableList<RecordingResponse>>, t: Throwable) {
            recordings.value = ArrayList()
            error.value = ApiError.CONNECTION
        }
        override fun onResponse(
                call: Call<MutableList<RecordingResponse>>,
                response: Response<MutableList<RecordingResponse>>
        ) {
            if (response.isSuccessful) {
                val recordingResponse = response.body() as ArrayList<RecordingResponse>
                if (recordingResponse.size > 0) {
                    recordingResponse.sortByDescending { selector(it) }
                    recordings.value = recordingResponse
                } else {
                    recordings.value = ArrayList()
                    error.value = ApiError.NO_RECORDINGS
                }
            } else {
                recordings.value = ArrayList()
                error.value = ApiError.SERVER
            }
        }
    }

    /**
     * Loads recordings from API
     */
    fun loadRecordings() {
        RecordingRepository.getRecordingData(recordingsCallback)
    }

    /**
     * Updates recording for detail view
     */
    fun selectRecording(recording: RecordingResponse) {
        selectedRecording.value = recording
    }

    /**
     * Observed to update recycler view with new recordings
     */
    fun getRecordings() : LiveData<ArrayList<RecordingResponse>> {
        return recordings
    }

    /**
     * Observed to update detail view with selected recording
     */
    fun getSelectedRecording() : LiveData<RecordingResponse> {
        return selectedRecording
    }

    /**
     * Observed to update error messages in case of request failure
     */
    fun getError() : LiveData<ApiError> {
        return error
    }
}