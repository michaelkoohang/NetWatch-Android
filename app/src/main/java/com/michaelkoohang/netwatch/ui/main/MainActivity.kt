package com.michaelkoohang.netwatch.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.model.repos.RecordingRepository
import com.michaelkoohang.netwatch.utils.UploadManager

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        RecordingRepository.uploadRecordingData(UploadManager.uploadCallback)
    }
}