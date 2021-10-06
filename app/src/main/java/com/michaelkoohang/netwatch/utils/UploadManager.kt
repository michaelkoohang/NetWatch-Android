package com.michaelkoohang.netwatch.utils

import android.widget.Toast
import com.michaelkoohang.netwatch.NetWatchApp
import com.michaelkoohang.netwatch.model.api.upload.UploadResponse
import com.michaelkoohang.netwatch.model.db.DatabaseManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UploadManager {
    private const val uploadSuccessMessage = "Data uploaded!"
    private const val uploadFailureMessage = "Upload error! We will automatically try again later."

    val uploadCallback = object : Callback<UploadResponse> {
        override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
            Toast.makeText(NetWatchApp.applicationContext(), uploadFailureMessage, Toast.LENGTH_LONG).show()
        }
        override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
            if (response.isSuccessful) {
                Toast.makeText(NetWatchApp.applicationContext(), uploadSuccessMessage, Toast.LENGTH_LONG).show()
                DatabaseManager.clearCache()
            } else {
                Toast.makeText(NetWatchApp.applicationContext(), uploadFailureMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}