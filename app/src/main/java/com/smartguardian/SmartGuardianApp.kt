package com.smartguardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.smartguardian.data.RuleDatabase

class SmartGuardianApp : Application() {

    companion object {
        const val CHANNEL_RINGER = "ringer_service_channel"
        const val CHANNEL_SIREN = "siren_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Database initialize
        RuleDatabase.getInstance(this)
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Ringer Service Channel
        NotificationChannel(
            CHANNEL_RINGER,
            "Ringer Sync Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Smart Guardian ringer sync"
            setShowBadge(false)
            nm.createNotificationChannel(this)
        }

        // Siren Service Channel
        NotificationChannel(
            CHANNEL_SIREN,
            "Siren Alarm Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Smart Guardian anti-theft siren"
            setShowBadge(true)
            nm.createNotificationChannel(this)
        }
    }
}