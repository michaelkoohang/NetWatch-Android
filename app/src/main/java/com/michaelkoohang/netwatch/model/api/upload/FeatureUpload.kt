package com.michaelkoohang.netwatch.model.api.upload

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class FeatureUpload(
    @SerializedName("timestamp")
    var timestamp: String? = null,

    @SerializedName("battery")
    var battery: Int? = null,

    @SerializedName("network")
    var network: String? = null,

    @SerializedName("service")
    var service: Boolean? = null,

    @SerializedName("connected")
    var connected: Boolean? = null,

    @SerializedName("http")
    var http: Boolean? = null,

    @SerializedName("lat")
    var lat: Double? = null,

    @SerializedName("lon")
    var lon: Double? = null,

    @SerializedName("accuracy")
    var accuracy: Double? = null,

    @SerializedName("speed")
    var speed: Double? = null
) : Serializable