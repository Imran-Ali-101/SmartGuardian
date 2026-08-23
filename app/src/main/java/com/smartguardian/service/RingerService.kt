package com.smartguardian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smartguardian.R

class RingerService : Service() {

    private lateinit var audioManager: AudioManager
    private lateinit var screenReceiver: BroadcastReceiver

    companion object {
        const val CHANNEL_ID = "ringer_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START_RINGER"
        const val ACTION_STOP = "ACTION_STOP_RINGER"
        const val ACTION_VARSITY_ON = "ACTION_VARSITY_ON"
        const val ACTION_VARSITY_OFF = "ACTION_VARSITY_OFF"
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        when (intent?.action) {
            ACTION_VARSITY_ON -> {
                saveVarsityMode(true)
                setVibrate()
            }
            ACTION_VARSITY_OFF -> {
                saveVarsityMode(false)
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun registerScreenReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val varsityOn = getSharedPreferences("sg_prefs", MODE_PRIVATE)
                    .getBoolean("varsity_mode", false)

                if (varsityOn) {
                    // Varsity mode — সবসময় vibrate
                    setVibrate()
                    return
                }

                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> setVibrate()
                    Intent.ACTION_SCREEN_OFF -> setRing()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        registerReceiver(screenReceiver, filter)
    }

    private fun setVibrate() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        }
    }

    private fun setRing() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
    }

    private fun saveVarsityMode(enabled: Boolean) {
        getSharedPreferences("sg_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("varsity_mode", enabled)
            .apply()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Guardian Active")
            .setContentText("Ringer sync is running")
            .setSmallIcon(R.drawable.ic_vibrate)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ringer Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Smart Guardian ringer sync service"
            setShowBadge(false)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}