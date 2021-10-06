package com.michaelkoohang.netwatch.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentSettingsBinding
import com.michaelkoohang.netwatch.model.Constants.apiUrl
import com.michaelkoohang.netwatch.model.db.DatabaseManager
import com.michaelkoohang.netwatch.model.DataStoreManager
import com.michaelkoohang.netwatch.service.MeasureService
import org.json.JSONArray

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var exportJson: ActivityResultLauncher<String>
    private lateinit var deviceId: String
    private lateinit var recordings: JSONArray

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        recordings = DatabaseManager.getRecordingsJson()

        exportJson = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
            if (uri != null) {
                val fos = requireActivity().contentResolver.openOutputStream(uri)
                fos?.bufferedWriter().use { it?.write(recordings.toString()) }
                Toast.makeText(context, "Data Exported", Toast.LENGTH_LONG).show()
            }
        }

        binding.setAboutBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apiUrl))
            requireContext().startActivity(browserIntent)
        }
        binding.setPrivacyBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$apiUrl/#/privacy"))
            requireContext().startActivity(browserIntent)
        }
        binding.setTermsBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$apiUrl/#/terms"))
            requireContext().startActivity(browserIntent)
        }
        binding.setIdBtn.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Your ID", deviceId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "ID Copied", Toast.LENGTH_LONG).show()
        }

        toggleExportButton()
        binding.setExportText.text = resources.getString(R.string.settings_export_text, recordings.length())
        binding.setExportBtn.setOnClickListener {
            exportJson.launch("export.json")
        }

        DataStoreManager.deviceIdFlow.asLiveData().observe(viewLifecycleOwner, { id ->
            deviceId = id
            binding.setIdText.text = getString(R.string.settings_id_label, id)
        })

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        recordings = DatabaseManager.getRecordingsJson()
    }

    private fun toggleExportButton() {
        if (recordings.length() > 0 && !MeasureService.recordingInProgress) {
            binding.setExportBtn.visibility = View.VISIBLE
            binding.setExportText.visibility = View.VISIBLE
            binding.setExportImage.visibility = View.VISIBLE
        } else {
            binding.setExportBtn.visibility = View.GONE
            binding.setExportText.visibility = View.GONE
            binding.setExportImage.visibility = View.GONE
        }
    }

}