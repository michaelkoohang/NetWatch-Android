package com.michaelkoohang.netwatch.model.repos

import com.michaelkoohang.netwatch.model.api.ApiClient
import com.michaelkoohang.netwatch.model.api.download.IdResponse
import retrofit2.Callback

object UserRepository {
    /**
     * Registers a new device id with the API
     */
    fun getId(callback: Callback<IdResponse>) {
        ApiClient.apiService.getId().enqueue(callback)
    }
}