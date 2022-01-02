package com.michaelkoohang.netwatch.ui.record

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentRecordMainBinding
import com.michaelkoohang.netwatch.model.DataStoreManager
import com.michaelkoohang.netwatch.model.repos.RecordingRepository
import com.michaelkoohang.netwatch.service.MeasureService
import com.michaelkoohang.netwatch.utils.PermissionManager
import com.michaelkoohang.netwatch.utils.UploadManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecordMainFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var _binding: FragmentRecordMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecordMainBinding.inflate(inflater, container, false)

        binding.recordingSwitch.isChecked = MeasureService.recordingInProgress
        binding.spinner.isEnabled = !MeasureService.recordingInProgress
        binding.recordingSwitch.setOnCheckedChangeListener { _, b ->
            if (b) { this.startMeasuring() } else { this.stopMeasuring() }
            binding.spinner.isEnabled = !b
        }
        binding.spinner.onItemSelectedListener = this
        binding.spinner.setSelection(getFrequencyIndex(runBlocking {
            DataStoreManager.measurementFrequencyFlow.first()
        }))
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        val item = parent.getItemAtPosition(pos)
        lifecycleScope.launch {
            DataStoreManager.saveMeasurementFrequency(item as String)
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    private fun checkPermission() {
        if (!PermissionManager.checkPermission()) {
            findNavController().navigate(R.id.action_map_to_location_permission)
        }
    }

    // Measuring methods
    private fun startMeasuring() {
        Toast.makeText(requireContext(), "Recording started.", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({
            val intent = Intent(requireActivity(), MeasureService::class.java)
            requireActivity().startService(intent)
        }, 300)
    }

    private fun stopMeasuring() {
        if (MeasureService.testInProgress) {
            Toast.makeText(requireContext(), "Network test in progress. Please wait a few seconds until it finishes.", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(requireActivity(), MeasureService::class.java)
            requireActivity().stopService(intent)
            Toast.makeText(requireContext(), "Uploading data...", Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                RecordingRepository.uploadRecordingData(UploadManager.uploadCallback)
            }, 2000)
        }
    }

    private fun getFrequencyIndex(frequency: String): Int {
        when (frequency) {
            "10 s" -> return 0
            "30 s" -> return 1
            "1 min" -> return 2
            "3 min" -> return 3
            "5 min" -> return 4
        }
        return 0
    }
}