package com.smartguardian.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TelegramHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun pingGoogle(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.google.com")
                .head()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendLocation(
        botToken: String,
        chatId: String,
        lat: Double,
        lng: Double,
        mapsLink: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val message = """
                📍 *Smart Guardian Location*
                
                Latitude: `$lat`
                Longitude: `$lng`
                
                🗺 [Open in Google Maps]($mapsLink)
            """.trimIndent()

            sendMessage(botToken, chatId, message)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendMessage(
        botToken: String,
        chatId: String,
        text: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                put("parse_mode", "Markdown")
            }

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.telegram.org/bot$botToken/sendMessage")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendLocationPoint(
        botToken: String,
        chatId: String,
        lat: Double,
        lng: Double
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("latitude", lat)
                put("longitude", lng)
            }

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.telegram.org/bot$botToken/sendLocation")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}