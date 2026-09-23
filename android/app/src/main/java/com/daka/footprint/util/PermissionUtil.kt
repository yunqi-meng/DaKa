package com.daka.footprint.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtil {

    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return hasPermissions(context, LOCATION_PERMISSIONS)
    }

    fun hasCameraPermission(context: Context): Boolean {
        return hasPermissions(context, CAMERA_PERMISSIONS)
    }
}