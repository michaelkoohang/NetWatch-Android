package com.michaelkoohang.netwatch.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentOnboardingBinding
import com.michaelkoohang.netwatch.model.viewmodel.OnboardingViewModel

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private var currentPage: Int = 0
    private val model: OnboardingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        binding.onboardingViewPager.isUserInputEnabled = false
        binding.onboardingViewPager.adapter = OnboardingAdapter(this)
        binding.onbNextBtn.setOnClickListener {
            binding.onboardingViewPager.currentItem = ++currentPage
            updateDots()
            when (currentPage) {
                1 -> {
                    model.agreeToPrivacy(false)
                    model.getPrivacyAgreed().observe(viewLifecycleOwner, { agree ->
                        binding.onbNextBtn.isEnabled = agree
                    })
                }
                2 -> {
                    model.retrieveCarrier(false)
                    model.getCarrierRetrieved().observe(viewLifecycleOwner, { retrieved ->
                        binding.onbNextBtn.isEnabled = retrieved
                    })
                }
                3 -> {
                    model.retrieveKey(false)
                    model.getKeyRetrieved().observe(viewLifecycleOwner, { retrieved ->
                        binding.onbNextBtn.isEnabled = retrieved
                    })
                }
                4 -> {
                    binding.onbNextBtn.text = getString(R.string.onb_done)
                    binding.onbNextBtn.setOnClickListener {
                        findNavController().navigate(R.id.action_onboarding_fragment_to_main_fragment)
                    }
                }
            }
        }
        updateDots()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateDots() {
        binding.onbDots.removeAllTabs()
        for (i in 0..4) {
            if (i <= currentPage) {
                binding.onbDots.addTab(binding.onbDots.newTab().setIcon(R.drawable.view_dot_active))
            } else {
                binding.onbDots.addTab(binding.onbDots.newTab().setIcon(R.drawable.view_dot_inactive))
            }
        }
    }

    private inner class OnboardingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            when(position) {
                0 -> return OnbStartFragment()
                1 -> return OnbPrivacyFragment()
                2 -> return OnbCarrierFragment()
                3 -> return OnbIdFragment()
                4 -> return OnbEndFragment()
            }
            return Fragment()
        }
    }
}