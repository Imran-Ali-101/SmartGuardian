package com.smartguardian.utils

import android.content.Context
import android.telephony.SmsManager

object SmsHelper {

    fun sendSms(context: Context, number: String, message: String) {
        try {
            if (number.isBlank()) return

            val smsManager = context.getSystemService(SmsManager::class.java)

            // Message লম্বা হলে parts এ ভাগ করে পাঠাবে
            val parts = smsManager.divideMessage(message)

            if (parts.size == 1) {
                smsManager.sendTextMessage(
                    number,
                    null,
                    message,
                    null,
                    null
                )
            } else {
                smsManager.sendMultipartTextMessage(
                    number,
                    null,
                    parts,
                    null,
                    null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}