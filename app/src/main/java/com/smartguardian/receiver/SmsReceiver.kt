package com.smartguardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.PowerManager
import android.provider.Telephony
import android.telephony.SmsMessage
import com.smartguardian.data.RuleDatabase
import com.smartguardian.data.RuleEntity
import com.smartguardian.utils.LocationHelper
import com.smartguardian.utils.SmsHelper
import com.smartguardian.utils.TelegramHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: return
        val body = messages.joinToString("") { it.displayMessageBody ?: "" }.trim()

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = RuleDatabase.getInstance(context)
                val rules = db.ruleDao().getAllRulesSync()

                for (rule in rules) {
                    if (!rule.isEnabled) continue
                    if (!body.contains(rule.smsKeyword, ignoreCase = true)) continue

                    when (rule.type) {
                        "SIREN" -> handleSiren(context, rule)
                        "LOCATION" -> handleLocation(context, rule, sender)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleSiren(context: Context, rule: RuleEntity) {
        val prefs = context.getSharedPreferences("sg_prefs", Context.MODE_PRIVATE)
        val volume = prefs.getInt("siren_volume", 100)

        // Volume 100% force
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val targetVol = (maxVol * volume / 100)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVol, 0)

        // WakeLock — screen on করতে
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "SmartGuardian:SirenWakeLock"
        )
        wl.acquire(10 * 60 * 1000L) // 10 min max

        // Siren Service start
        val sirenIntent = Intent(context, SirenService::class.java)
        context.startForegroundService(sirenIntent)
    }

    private suspend fun handleLocation(context: Context, rule: RuleEntity, sender: String) {
        val prefs = context.getSharedPreferences("sg_prefs", Context.MODE_PRIVATE)
        val botToken = prefs.getString("bot_token", "") ?: ""
        val fallbackNumber = prefs.getString("fallback_number", "") ?: ""

        val location = LocationHelper.getLastKnownLocation(context)
            ?: LocationHelper.requestFreshLocation(context)

        if (location == null) {
            SmsHelper.sendSms(
                context,
                sender,
                "Smart Guardian: Could not get location."
            )
            return
        }

        val lat = location.latitude
        val lng = location.longitude
        val mapsLink = "https://maps.google.com/?q=$lat,$lng"

        // Ping test
        val isOnline = TelegramHelper.pingGoogle()

        if (isOnline && botToken.isNotEmpty()) {
            // Telegram এ পাঠাও — chat id নেই, তাই bot এর updates থেকে chat id নেব
            val chatId = prefs.getString("telegram_chat_id", "") ?: ""
            if (chatId.isNotEmpty()) {
                TelegramHelper.sendLocation(botToken, chatId, lat, lng, mapsLink)
            } else {
                // Fallback SMS
                SmsHelper.sendSms(context, fallbackNumber.ifEmpty { sender }, mapsLink)
            }
        } else {
            // Offline fallback SMS
            SmsHelper.sendSms(context, fallbackNumber.ifEmpty { sender }, mapsLink)
        }
    }
}