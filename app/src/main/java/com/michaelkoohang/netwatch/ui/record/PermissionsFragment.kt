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

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)

        val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (permission[permissions[0]] == true && permission[permissions[1]] == true && permission[permissions[2]] == true) {
                findNavController().navigate(R.id.action_location_permission_to_map)
            }
        }

        val alertDialog = activity.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.permission_title)
                setMessage(R.string.permission_dialog_text)
                setNegativeButton(R.string.permission_dialog_cancel, null)
                setPositiveButton(R.string.permission_dialog_ok) { _, _ ->
                    locationPermission.launch(permissions)
                }
            }
            builder.create()
        }

        binding.permissionsButton.setOnClickListener{
            alertDialog.show()
        }

        return binding.root
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