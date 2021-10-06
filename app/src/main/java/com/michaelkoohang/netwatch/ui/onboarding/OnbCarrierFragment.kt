package com.michaelkoohang.netwatch.ui.onboarding

import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.michaelkoohang.netwatch.databinding.FragmentOnbCarrierBinding
import com.michaelkoohang.netwatch.model.DataStoreManager
import com.michaelkoohang.netwatch.model.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

class OnbCarrierFragment : Fragment() {
    private var _binding: FragmentOnbCarrierBinding? = null
    private val binding get() = _binding!!

    private val model: OnboardingViewModel by activityViewModels()
    private lateinit var telephonyManager: TelephonyManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentOnbCarrierBinding.inflate(inflater, container, false)
        telephonyManager = getSystemService(requireContext(), TelephonyManager::class.java)!!
        binding.getCarrierBtn.setOnClickListener {
            getCarrier()
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun getCarrier() {
        val carrier = telephonyManager.networkOperatorName
        if (carrier.isNotEmpty()) {
            lifecycleScope.launch {
                DataStoreManager.saveCarrier(carrier)
            }
            model.retrieveCarrier(true)
            binding.getCarrierBtn.isEnabled = false
            binding.onbCarrierCheckmark.playAnimation()
            Toast.makeText(requireContext(), "Your carrier is: $carrier", Toast.LENGTH_LONG).show()
        } else {
            model.retrieveCarrier(false)
            Toast.makeText(requireContext(), "Error while getting carrier. Please make sure you have a valid SIM card and active data plan.", Toast.LENGTH_LONG).show()
        }
    }
}