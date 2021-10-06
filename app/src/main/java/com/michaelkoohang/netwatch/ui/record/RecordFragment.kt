package com.michaelkoohang.netwatch.ui.record

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.michaelkoohang.netwatch.R
import com.michaelkoohang.netwatch.utils.PermissionManager

class RecordFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission() {
        if (PermissionManager.checkPermission()) {
            findNavController().navigate(R.id.action_record_to_map)
        } else {
            findNavController().navigate(R.id.action_record_to_location_permission)
        }
    }
}