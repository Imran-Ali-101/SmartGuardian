package com.smartguardian.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.smartguardian.R
import com.smartguardian.SmartGuardianApp

class SirenService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var audioManager: AudioManager

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP_SIREN = "ACTION_STOP_SIREN"
    }

    // Screen unlock হলে siren বন্ধ করার receiver
    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                // User screen unlock করেছে
                stopSiren()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        registerUnlockReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SIREN) {
            stopSiren()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        acquireWakeLock()
        startSiren()

        return START_STICKY
    }

    private fun startSiren() {
        try {
            val prefs = getSharedPreferences("sg_prefs", MODE_PRIVATE)
            val volume = prefs.getInt("siren_volume", 100)

            // Volume force max
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val targetVol = (maxVol * volume / 100)
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                targetVol,
                AudioManager.FLAG_SHOW_UI
            )

            // Silent/Vibrate override
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }

            mediaPlayer = MediaPlayer().apply {
                val afd = resources.openRawResourceFd(R.raw.siren_loop_1)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                isLooping = true
                prepare()
                start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopSiren() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "SmartGuardian:SirenWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 min max
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerUnlockReceiver() {
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, SmartGuardianApp.CHANNEL_SIREN)
            .setContentTitle("⚠️ Anti-Theft Alarm Active")
            .setContentText("Unlock your screen to stop the alarm")
            .setSmallIcon(R.drawable.ic_siren)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            SmartGuardianApp.CHANNEL_SIREN,
            "Siren Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Anti-theft siren alarm"
            setShowBadge(true)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(unlockReceiver)
        stopSiren()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}