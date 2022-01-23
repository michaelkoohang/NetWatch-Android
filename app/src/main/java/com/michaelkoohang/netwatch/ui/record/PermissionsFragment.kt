package com.michaelkoohang.netwatch.ui.record

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.databinding.FragmentPermissionsBinding
import com.michaelkoohang.netwatch.utils.PermissionManager

class PermissionsFragment : Fragment() {
    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    private val standardPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )

    private val backgroundPermissions = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    )

    private val standardPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
        if (permission[standardPermissions[0]] == true && permission[standardPermissions[1]] == true && permission[standardPermissions[2]] == true) {
            backgroundPermissionsRequest.launch(backgroundPermissions[0])
        }
    }

    private val backgroundPermissionsRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        if (permission) {
            findNavController().navigate(R.id.action_location_permission_to_map)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.permissionsButton.setOnClickListener {
            val alertDialog = activity.let {
                val builder = AlertDialog.Builder(activity)
                builder.apply {
                    setTitle(R.string.permission_title)
                    setMessage(R.string.permission_dialog_text)
                    setPositiveButton(R.string.permission_dialog_ok) { _, _ ->
                        standardPermissionRequest.launch(standardPermissions)
                    }
                }
                builder.create()
            }
            alertDialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun checkPermission() {
        if (PermissionManager.checkPermission()) {
            findNavController().navigate(R.id.action_location_permission_to_map)
        }
    }
}