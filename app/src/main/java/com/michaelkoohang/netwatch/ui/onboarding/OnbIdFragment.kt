package com.michaelkoohang.netwatch.ui.onboarding

import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.michaelkoohang.netwatch.databinding.FragmentOnbIdBinding
import com.michaelkoohang.netwatch.model.DataStoreManager
import com.michaelkoohang.netwatch.model.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

class OnbIdFragment : Fragment() {
    private var _binding: FragmentOnbIdBinding? = null
    private val binding get() = _binding!!

    private val model: OnboardingViewModel by activityViewModels()
    private lateinit var telephonyManager: TelephonyManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentOnbIdBinding.inflate(inflater, container, false)
        telephonyManager = getSystemService(requireContext(), TelephonyManager::class.java)!!
        binding.getKeyBtn.setOnClickListener {
            model.registerId()
            binding.onbKeyCheckmark.pauseAnimation()
            binding.onbKeyCheckmark.isVisible = false
            binding.onbKeyLoading.isVisible = true
            binding.onbKeyLoading.playAnimation()
        }
        observeId()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun observeId() {
        model.getId().observe(viewLifecycleOwner, { id ->
            if (id.isNotEmpty()) {
                lifecycleScope.launch {
                    DataStoreManager.saveDeviceId(id)
                    DataStoreManager.saveOnboardComplete(true)
                }
                model.retrieveKey(true)
                binding.getKeyBtn.isEnabled = false
                binding.onbKeyLoading.pauseAnimation()
                binding.onbKeyLoading.isVisible = false
                binding.onbKeyCheckmark.isVisible = true
                binding.onbKeyCheckmark.playAnimation()
                Toast.makeText(requireContext(), "Your ID is: $id", Toast.LENGTH_LONG).show()
            } else {
                model.retrieveKey(false)
                binding.onbKeyLoading.pauseAnimation()
                binding.onbKeyLoading.isVisible = false
                Toast.makeText(requireContext(), "Error while getting key. Make sure you have internet and please try again later.", Toast.LENGTH_LONG).show()
            }
        })
    }
}