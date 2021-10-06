package com.michaelkoohang.netwatch.model.api.download

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RecordingResponse(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("duration")
    val duration: Long? = null,

    @SerializedName("distance")
    val distance: Double? = null,

    @SerializedName("start")
    val start: String? = null,

    @SerializedName("end")
    val end: String? = null,

    @SerializedName("carrier")
    val carrier: String? = null,

    @SerializedName("manufacturer")
    val manufacturer: String? = null,

    @SerializedName("os")
    val os: String? = null,

    @SerializedName("device_id")
    val deviceId: Int? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("features")
    val features: ArrayList<FeatureResponse>? = null
) : Serializable