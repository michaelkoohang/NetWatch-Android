package com.michaelkoohang.netwatch.model.api.download

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IdResponse(
    @SerializedName("id")
    val id: String? = null,
) : Serializable