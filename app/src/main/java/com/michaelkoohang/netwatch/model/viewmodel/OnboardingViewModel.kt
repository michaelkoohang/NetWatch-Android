package com.michaelkoohang.netwatch.model.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaelkoohang.netwatch.model.api.download.IdResponse
import com.michaelkoohang.netwatch.model.repos.UserRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OnboardingViewModel : ViewModel() {
    private val privacyAgreed = MutableLiveData<Boolean>()
    private val keyRetrieved = MutableLiveData<Boolean>()
    private val carrierRetrieved = MutableLiveData<Boolean>()
    private val id = MutableLiveData<String>()

    /**
     * Callback that handles id registration with the API
     */
    private val idCallback = object : Callback<IdResponse> {
        override fun onFailure(call: Call<IdResponse>, t: Throwable) {
            id.value = ""
        }
        override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
            if (response.isSuccessful) {
                id.value = response.body()!!.id!!
            } else {
                id.value = ""
            }
        }
    }

    /**
     * Registers new device id with API
     */
    fun registerId() {
        UserRepository.getId(idCallback)
    }

    // Getters
    fun getId() : LiveData<String> {
        return id
    }

    fun getPrivacyAgreed() : LiveData<Boolean> {
        return privacyAgreed
    }

    fun getKeyRetrieved() : LiveData<Boolean> {
        return keyRetrieved
    }

    fun getCarrierRetrieved() : LiveData<Boolean> {
        return carrierRetrieved
    }

    // Setters
    fun agreeToPrivacy(agreed: Boolean) {
        privacyAgreed.value = agreed
    }

    fun retrieveKey(retrieved: Boolean) {
        keyRetrieved.value = retrieved
    }

    fun retrieveCarrier(retrieved: Boolean) {
        carrierRetrieved.value = retrieved
    }
}