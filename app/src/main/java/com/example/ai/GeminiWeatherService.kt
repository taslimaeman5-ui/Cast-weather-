package com.example.ai

import com.example.BuildConfig
import com.example.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiWeatherService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askWeatherQuestion(
        question: String,
        currentWeather: WeatherData?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext provideLocalContextualWeatherAnswer(question, currentWeather)
        }

        try {
            val weatherContext = currentWeather?.let {
                """
                Current Location: ${it.cityName}, ${it.country}
                Current Temperature: ${it.currentTempC}°C (Feels like: ${it.feelsLikeC}°C)
                Weather Condition: ${it.condition.displayName}
                Humidity: ${it.humidityPercent}%
                Wind Speed: ${it.windSpeedKmh} km/h
                UV Index: ${it.uvIndex}
                Air Pressure: ${it.pressureHpa} hPa
                Active Alerts: ${if (it.alerts.isNotEmpty()) it.alerts.joinToString("; ") { a -> "${a.severity.level}: ${a.title} - ${a.headline}" } else "None"}
                """.trimIndent()
            } ?: "No active weather location selected."

            val systemPrompt = """
                You are SkyCast AI, an expert meteorologist and helpful personal weather assistant.
                Answer weather-related questions clearly, helpfully, and concisely (2-4 sentences unless detailed instructions are requested).
                Use the current real-time weather context provided below to give tailored advice regarding clothing, outdoor activities, travel, precipitation, and safety precautions.
                
                REAL-TIME WEATHER CONTEXT:
                $weatherContext
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemPrompt\n\nUser Question: $question")
                            })
                        })
                    })
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                return@withContext provideLocalContextualWeatherAnswer(question, currentWeather)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                provideLocalContextualWeatherAnswer(question, currentWeather)
            }
        } catch (e: Exception) {
            provideLocalContextualWeatherAnswer(question, currentWeather)
        }
    }

    /**
     * Smart fallback weather engine that answers questions accurately using active weather data
     * even if offline or if an API key hasn't yet been configured in the Secrets panel.
     */
    private fun provideLocalContextualWeatherAnswer(
        question: String,
        weather: WeatherData?
    ): String {
        if (weather == null) {
            return "Please select a city or enable GPS location to get live weather answers."
        }

        val q = question.lowercase()
        val city = weather.cityName
        val temp = weather.currentTempC
        val cond = weather.condition.displayName
        val humidity = weather.humidityPercent
        val wind = weather.windSpeedKmh
        val uv = weather.uvIndex

        return when {
            q.contains("umbrella") || q.contains("rain") -> {
                if (weather.condition.name.contains("RAIN") || weather.condition.name.contains("DRIZZLE")) {
                    "Yes, take an umbrella! It's currently ${cond.lowercase()} in $city with high chance of precipitation."
                } else {
                    "No umbrella needed right now in $city. Current weather is $cond with minimal rain expected."
                }
            }
            q.contains("jacket") || q.contains("coat") || q.contains("wear") || q.contains("cold") -> {
                when {
                    temp < 10.0 -> "It's chilly in $city at ${temp.toInt()}°C. A warm coat or heavy jacket is recommended."
                    temp in 10.0..18.0 -> "It's mild in $city (${temp.toInt()}°C). A light jacket or sweater will keep you comfortable."
                    else -> "It's warm in $city (${temp.toInt()}°C). Light and breathable clothing is perfect for today."
                }
            }
            q.contains("run") || q.contains("jog") || q.contains("walk") || q.contains("outdoor") -> {
                when {
                    weather.alerts.isNotEmpty() -> "Exercise caution with outdoor activities: ${weather.alerts.first().title} is active in $city."
                    temp > 30.0 -> "It's quite hot in $city (${temp.toInt()}°C). If exercising outdoors, hydrate well and aim for morning or evening."
                    wind > 35.0 -> "Brisk winds around ${wind.toInt()} km/h in $city. A walk is fine, but expect gusty conditions."
                    else -> "Great conditions for outdoor activities in $city! Currently $cond and ${temp.toInt()}°C."
                }
            }
            q.contains("sunscreen") || q.contains("uv") || q.contains("sun") -> {
                if (uv >= 6.0) {
                    "Yes! The UV Index in $city is high at $uv. Sunscreen (SPF 30+) and sunglasses are strongly recommended."
                } else {
                    "The UV Index in $city is low to moderate at $uv. Routine sun protection is adequate."
                }
            }
            else -> {
                "In $city, it's currently $cond at ${temp.toInt()}°C (feels like ${weather.feelsLikeC.toInt()}°C) with $humidity% humidity and ${wind.toInt()} km/h winds."
            }
        }
    }
}
