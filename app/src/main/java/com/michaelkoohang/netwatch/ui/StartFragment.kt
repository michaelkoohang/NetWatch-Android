package com.michaelkoohang.netwatch.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.model.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StartFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val onboardCompleted = runBlocking {
            DataStoreManager.onboardCompleteFlow.first()
        }
        if (onboardCompleted) {
            findNavController().navigate(R.id.action_start_to_main_fragment)
        } else {
            findNavController().navigate(R.id.action_start_to_onboarding_fragment)
        }
        return inflater.inflate(R.layout.fragment_start, container, false)
    }
}