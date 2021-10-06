package com.michaelkoohang.netwatch.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.michaelkoohang.netwatch.databinding.FragmentOnbPrivacyBinding
import com.michaelkoohang.netwatch.model.Constants.apiUrl
import com.michaelkoohang.netwatch.model.viewmodel.OnboardingViewModel

class OnbPrivacyFragment : Fragment() {
    private var _binding: FragmentOnbPrivacyBinding? = null
    private val binding get() = _binding!!

    private val model: OnboardingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentOnbPrivacyBinding.inflate(inflater, container, false)

        binding.privacyPolicyBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$apiUrl/#/privacy"))
            requireContext().startActivity(browserIntent)
        }
        binding.termsBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$apiUrl/#/terms"))
            requireContext().startActivity(browserIntent)
        }
        binding.agreePrivacyCheckbox.setOnCheckedChangeListener { _, isChecked ->
            model.agreeToPrivacy(isChecked)
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}