package com.smartguardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartguardian.service.RingerService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                restoreServices(context)
            }
        }
    }

    private fun restoreServices(context: Context) {
        val prefs = context.getSharedPreferences("sg_prefs", Context.MODE_PRIVATE)

        // Onboarding শেষ না হলে কিছু করব না
        val onboardingDone = prefs.getBoolean("onboarding_done", false)
        if (!onboardingDone) return

        // Varsity Mode বা Smart Ringer যেকোনো একটা চালু থাকলে RingerService restart
        val varsityOn = prefs.getBoolean("varsity_mode", false)
        val ringerSyncOn = prefs.getBoolean("ringer_sync", false)

        if (varsityOn || ringerSyncOn) {
            val serviceIntent = Intent(context, RingerService::class.java).apply {
                action = if (varsityOn) {
                    RingerService.ACTION_VARSITY_ON
                } else {
                    RingerService.ACTION_START
                }
            }
            context.startForegroundService(serviceIntent)
        }
    }
}