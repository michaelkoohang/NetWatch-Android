package com.michaelkoohang.netwatch.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.michaelkoohang.netwatch.NetWatchApp

object PermissionManager {
    fun checkPermission() : Boolean {
        val backgroundPermission = ContextCompat.checkSelfPermission(NetWatchApp.applicationContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        val phonePermission = ContextCompat.checkSelfPermission(NetWatchApp.applicationContext(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        return backgroundPermission && phonePermission
    }
}