package com.michaelkoohang.netwatch.model.api

import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import com.michaelkoohang.netwatch.model.api.download.IdResponse
import com.michaelkoohang.netwatch.model.api.upload.RecordingUpload
import com.michaelkoohang.netwatch.model.api.upload.UploadResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/register")
    fun getId(): Call<IdResponse>

    @GET("/api/recordings")
    fun getRecordings(
        @Header("Device-Id") deviceId: String
    ): Call<MutableList<RecordingResponse>>

    @POST("/api/recordings")
    fun uploadRecordings(
        @Header("Device-Id") deviceId: String,
        @Body recordings: ArrayList<RecordingUpload>
    ): Call<UploadResponse>
}