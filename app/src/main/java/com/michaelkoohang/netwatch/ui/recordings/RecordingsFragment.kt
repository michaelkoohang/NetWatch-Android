package com.michaelkoohang.netwatch.ui.recordings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentRecordingsBinding
import com.michaelkoohang.netwatch.model.api.ApiError
import com.michaelkoohang.netwatch.model.api.download.RecordingResponse
import com.michaelkoohang.netwatch.model.viewmodel.RecordingsViewModel
import com.michaelkoohang.netwatch.utils.CalcManager
import com.michaelkoohang.netwatch.utils.FormatManager
import kotlin.math.roundToInt

class RecordingsFragment : Fragment() {
    private var _binding: FragmentRecordingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recordings: ArrayList<RecordingResponse>
    private val model: RecordingsViewModel by activityViewModels()

    private val onItemClickListener = object : OnItemClickListener {
        override fun onItemClick(position: Int) {
            model.selectRecording(recordings[position])
            findNavController().navigate(R.id.action_recordings_dest_to_recording_detail)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecordingsBinding.inflate(inflater, container, false)

        // Setup list of recordings
        binding.recordingsView.layoutManager = LinearLayoutManager(requireContext())
        model.loadRecordings()
        observeRecordings()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Update UI

    private fun showRecyclerView(visible: Boolean) {
        if (visible) {
            binding.recordingsScrollView.visibility = View.VISIBLE
            binding.errorAnimation.visibility = View.GONE
            binding.errorTitle.visibility = View.GONE
            binding.errorText.visibility = View.GONE
        } else {
            binding.recordingsScrollView.visibility = View.GONE
            binding.errorAnimation.visibility = View.VISIBLE
            binding.errorTitle.visibility = View.VISIBLE
            binding.errorText.visibility = View.VISIBLE
        }
        binding.loading.visibility = View.GONE
    }

    private fun updateTotals() {
        val connectivity = CalcManager.calcConnectivity(recordings)
        val distance = CalcManager.calcDistance(recordings)
        val seconds = CalcManager.calcTime(recordings)

        binding.recordingsText.text = "${recordings.size}"
        binding.distanceText.text = FormatManager.getDisplayDistance(distance)
        binding.timeText.text = FormatManager.getStopWatchTime(seconds)
        updateConnectivity(connectivity)
    }

    private fun updateConnectivity(percentage: Double) {
        val connectedBar = binding.connectivityView.connectivityBar
        val displayMetrics = requireContext().resources.displayMetrics
        val currentWidth = displayMetrics.widthPixels / displayMetrics.density
        val newWidth = (currentWidth - CONNECTIVITY_BAR_SPACING) * percentage
        val disconnectedBar = binding.connectivityView.connectivityBarDisconnected
        disconnectedBar.layoutParams.width = ((currentWidth - CONNECTIVITY_BAR_SPACING) * displayMetrics.density).roundToInt()

        if (newWidth > 0) {
            connectedBar.visibility = View.VISIBLE
            connectedBar.layoutParams.width = (newWidth * displayMetrics.density).roundToInt()
        } else {
            connectedBar.visibility = View.GONE
        }

        binding.connectivityView.connectivityPercentage.text = FormatManager.getDisplayCoverage(percentage)
        if (percentage >= 0.5) {
            binding.connectivityView.connectivityPercentage.setTextColor(
                resources.getColor(R.color.green, resources.newTheme())
            )
        } else {
            binding.connectivityView.connectivityPercentage.setTextColor(
                resources.getColor(R.color.red, resources.newTheme())
            )
        }
    }

    private fun updateRecyclerView(adapter: RecordingsAdapter) {
        binding.recordingsView.adapter = adapter
    }

    // Functions for observing recording data

    private fun observeRecordings() {
        model.getRecordings().observe(viewLifecycleOwner, { recordings ->
            if (recordings.size > 0) {
                this.recordings = recordings
                showRecyclerView(true)
                updateTotals()
                updateRecyclerView(RecordingsAdapter(this.recordings, onItemClickListener))
            } else {
                observeError()
            }
        })
    }

    private fun observeError() {
        model.getError().observe(viewLifecycleOwner, { error ->
            when(error) {
                ApiError.NO_RECORDINGS -> {
                    showRecyclerView(false)
                    binding.errorTitle.text = resources.getString(R.string.error_no_recordings_title)
                    binding.errorText.text = resources.getString(R.string.error_no_recordings_text)
                }
                ApiError.SERVER -> {
                    showRecyclerView(false)
                    binding.errorTitle.text = resources.getString(R.string.error_server_title)
                    binding.errorText.text = resources.getString(R.string.error_server_text)
                }
                ApiError.CONNECTION -> {
                    showRecyclerView(false)
                    binding.errorTitle.text = resources.getString(R.string.error_connection_title)
                    binding.errorText.text = resources.getString(R.string.error_connection_text)
                }
            }
        })
    }

    companion object {
        private const val CONNECTIVITY_BAR_SPACING = 48
    }
}