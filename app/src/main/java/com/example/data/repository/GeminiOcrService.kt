package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.ui.screens.MedicineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiOcrService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun extractPrescriptionDetails(bitmap: Bitmap): MedicineItem? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiOcrService", "API Key is missing or default!")
            return@withContext null
        }

        val base64Image = bitmap.toBase64()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are an expert pharmacist AI. Analyze this prescription label image. Extract the following information and return ONLY a valid JSON object with no markdown formatting. The JSON must have these exact keys: 'name' (medicine name), 'dosage' (amount, e.g. 500mg), 'frequency' (e.g. Once Daily, Twice a day), 'instructions' (e.g. after food, with water). If a value is not found, use 'Unknown'.")
                        })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("responseFormat", JSONObject().apply {
                    put("text", JSONObject().apply {
                        put("mimeType", "application/json")
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("GeminiOcrService", "API Error: ${response.code} - ${response.message}\n${response.body?.string()}")
                return@withContext null
            }

            val responseString = response.body?.string() ?: return@withContext null
            val responseJson = JSONObject(responseString)
            
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    // Parse the inner JSON text
                    val resultJson = JSONObject(text.trim())
                    
                    val name = resultJson.optString("name", "Unknown Medicine")
                    val dosage = resultJson.optString("dosage", "Unknown Dosage")
                    val frequency = resultJson.optString("frequency", "Unknown Frequency")
                    val instructions = resultJson.optString("instructions", "")
                    
                    val combinedFreq = if (instructions.isNotEmpty() && instructions != "Unknown") {
                        "$frequency • $instructions"
                    } else frequency

                    return@withContext MedicineItem(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        dosage = dosage,
                        frequency = combinedFreq,
                        remainingPills = 30, // Default
                        category = "Prescription"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiOcrService", "Exception: ${e.message}", e)
        }
        return@withContext null
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap to avoid sending too large images if necessary
        val scaledBitmap = if (width > 1200 || height > 1200) {
            val scale = 1200.0f / Math.max(width, height)
            Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        } else this
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
