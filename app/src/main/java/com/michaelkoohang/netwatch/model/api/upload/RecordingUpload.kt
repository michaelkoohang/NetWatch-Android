package com.michaelkoohang.netwatch.model.api.upload

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class RecordingUpload(
    @SerializedName("duration")
    var duration: Long? = null,

    @SerializedName("distance")
    var distance: Double? = null,

    @SerializedName("start")
    var start: String? = null,

    @SerializedName("end")
    var end: String? = null,

    @SerializedName("carrier")
    var carrier: String? = null,

    @SerializedName("manufacturer")
    var manufacturer: String? = null,

    @SerializedName("os")
    var os: String? = null,

    @SerializedName("features")
    var features: ArrayList<FeatureUpload>? = null
) : Serializable