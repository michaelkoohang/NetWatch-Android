package com.michaelkoohang.netwatch.model.api.download

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FeatureResponse(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null,

    @SerializedName("battery")
    val battery: Int? = null,

    @SerializedName("network")
    val network: String? = null,

    @SerializedName("service")
    val service: Int? = null,

    @SerializedName("connected")
    val connected: Int? = null,

    @SerializedName("http")
    val http: Int? = null,

    @SerializedName("lat")
    val lat: Double? = null,

    @SerializedName("lon")
    val lon: Double? = null,

    @SerializedName("accuracy")
    val accuracy: Double? = null,

    @SerializedName("speed")
    val speed: Double? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("hike_id")
    val hikeId: Int? = null
) : Serializable