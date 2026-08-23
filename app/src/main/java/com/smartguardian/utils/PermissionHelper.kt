package com.smartguardian.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val REQUEST_CODE_PERMISSIONS = 1001

    private val RUNTIME_PERMISSIONS = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // সব permission check করে missing গুলো return করে
    fun getMissingPermissions(context: Context): List<MissingPermission> {
        val missing = mutableListOf<MissingPermission>()

        // Runtime permissions
        for (perm in RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, perm)
                != PackageManager.PERMISSION_GRANTED
            ) {
                missing.add(
                    MissingPermission(
                        name = getFriendlyName(perm),
                        type = PermissionType.RUNTIME,
                        permission = perm
                    )
                )
            }
        }

        // DND Access
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            missing.add(
                MissingPermission(
                    name = "Do Not Disturb Access",
                    type = PermissionType.DND
                )
            )
        }

        // Battery Optimization
        val pm = context.getSystemService(Context.POWER_SERVICE)
            as android.os.PowerManager
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            missing.add(
                MissingPermission(
                    name = "Battery Optimization Exempt",
                    type = PermissionType.BATTERY
                )
            )
        }

        return missing
    }

    fun requestRuntimePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            RUNTIME_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    fun showMissingPermissionDialog(context: Context, missing: List<MissingPermission>) {
        val names = missing.joinToString("\n") { "• ${it.name}" }

        AlertDialog.Builder(context)
            .setTitle("Permissions Required")
            .setMessage("The following permissions are missing:\n\n$names\n\nPlease grant them for Smart Guardian to work properly.")
            .setCancelable(false)
            .setPositiveButton("Fix Now") { dialog, _ ->
                dialog.dismiss()
                openSettingsForFirstMissing(context, missing)
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openSettingsForFirstMissing(
        context: Context,
        missing: List<MissingPermission>
    ) {
        val first = missing.firstOrNull() ?: return

        when (first.type) {
            PermissionType.RUNTIME -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
            PermissionType.DND -> {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                )
            }
            PermissionType.BATTERY -> {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                ).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    }

    fun openAutoStartSettings(context: Context) {
        // Meizu / Flyme
        try {
            val intent = Intent("com.meizu.safe.security.SHOW_APPSEC").apply {
                putExtra("packageName", context.packageName)
            }
            context.startActivity(intent)
            return
        } catch (e: Exception) { }

        // General fallback — App Info
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) { }
    }

    private fun getFriendlyName(permission: String): String {
        return when (permission) {
            Manifest.permission.RECEIVE_SMS -> "Receive SMS"
            Manifest.permission.READ_SMS -> "Read SMS"
            Manifest.permission.SEND_SMS -> "Send SMS"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Precise Location (GPS)"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Approximate Location"
            else -> permission
        }
    }

    data class MissingPermission(
        val name: String,
        val type: PermissionType,
        val permission: String = ""
    )

    enum class PermissionType {
        RUNTIME,
        DND,
        BATTERY
    }
}