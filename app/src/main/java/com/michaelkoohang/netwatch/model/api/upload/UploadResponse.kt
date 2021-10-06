package com.michaelkoohang.netwatch.model.api.upload

import com.google.gson.annotations.SerializedName
import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import java.io.Serializable

data class UploadResponse(
    @SerializedName("success")
    val success: String? = null,

    @SerializedName("data")
    val data: ArrayList<RecordingResponse>? = null
): Serializable