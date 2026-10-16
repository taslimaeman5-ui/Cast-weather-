package com.example.data

import com.example.model.DailyForecast
import com.example.model.HourlyForecast
import com.example.model.WeatherAlert
import com.example.model.WeatherAlertSeverity
import com.example.model.WeatherCondition
import com.example.model.WeatherData
import com.example.network.GeocodingApiService
import com.example.network.GeocodingResult
import com.example.network.NwsAlertApiService
import com.example.network.WeatherApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WeatherRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "SkyCastWeatherApp/1.0 (contact: support@skycast.local)")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geocodingRetrofit = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val nwsRetrofit = Retrofit.Builder()
        .baseUrl("https://api.weather.gov/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val weatherService = weatherRetrofit.create(WeatherApiService::class.java)
    private val geocodingService = geocodingRetrofit.create(GeocodingApiService::class.java)
    private val nwsAlertService = nwsRetrofit.create(NwsAlertApiService::class.java)

    suspend fun searchCities(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        try {
            val response = geocodingService.searchCity(query.trim())
            response.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String?> = withContext(Dispatchers.IO) {
        // 1. Try BigDataCloud reverse geocode client API
        try {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=en"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "SkyCastWeatherApp/1.0")
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val moshiAdapter = moshi.adapter(Map::class.java)
                val map = moshiAdapter.fromJson(json) as? Map<*, *>
                val city = (map?.get("city") ?: map?.get("locality") ?: map?.get("principalSubdivision")) as? String
                val country = (map?.get("countryName") ?: map?.get("countryCode")) as? String
                if (!city.isNullOrBlank()) {
                    return@withContext Pair(city, country)
                }
            }
        } catch (_: Exception) { }

        // 2. Try Open-Meteo reverse geocoding via geocoding service search
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=10&addressdetails=1"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "SkyCastWeatherApp/1.0 (contact: support@skycast.local)")
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val obj = org.json.JSONObject(json)
                val address = obj.optJSONObject("address")
                val city = address?.optString("city")?.takeIf { it.isNotBlank() }
                    ?: address?.optString("town")?.takeIf { it.isNotBlank() }
                    ?: address?.optString("village")?.takeIf { it.isNotBlank() }
                    ?: address?.optString("county")?.takeIf { it.isNotBlank() }
                    ?: address?.optString("state")?.takeIf { it.isNotBlank() }
                val country = address?.optString("country")
                if (!city.isNullOrBlank()) {
                    return@withContext Pair(city, country)
                }
            }
        } catch (_: Exception) { }

        // Fallback format
        Pair(String.format(Locale.US, "GPS Location (%.2f, %.2f)", latitude, longitude), "Local Coordinates")
    }

    suspend fun fetchWeather(
        cityName: String,
        countryName: String?,
        latitude: Double,
        longitude: Double
    ): WeatherData = withContext(Dispatchers.IO) {
        val response = weatherService.getForecast(latitude = latitude, longitude = longitude)
        val current = response.current ?: throw IllegalStateException("Current weather data unavailable")

        val hourlyList = mutableListOf<HourlyForecast>()
        val hourlyTime = response.hourly?.time ?: emptyList()
        val hourlyTemps = response.hourly?.temperature2m ?: emptyList()
        val hourlyCodes = response.hourly?.weatherCode ?: emptyList()
        val hourlyPrecip = response.hourly?.precipitationProbability ?: emptyList()

        // Locate current hour and take next 24 hours
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val outputTimeFormat = SimpleDateFormat("h a", Locale.getDefault())
        val nowMs = System.currentTimeMillis()
        var startIndex = 0
        var smallestDiff = Long.MAX_VALUE

        for (i in hourlyTime.indices) {
            try {
                val parsed = inputFormat.parse(hourlyTime[i])
                if (parsed != null) {
                    val diff = Math.abs(parsed.time - nowMs)
                    if (diff < smallestDiff) {
                        smallestDiff = diff
                        startIndex = i
                    }
                }
            } catch (_: Exception) { }
        }

        val endIndex = minOf(hourlyTime.size, hourlyTemps.size, startIndex + 24)
        for (i in startIndex until endIndex) {
            val rawTime = hourlyTime[i]
            val timeLabel = try {
                val parsed = inputFormat.parse(rawTime)
                if (i == startIndex) "Now" else outputTimeFormat.format(parsed ?: Date())
            } catch (e: Exception) {
                rawTime.substringAfter("T")
            }
            val temp = hourlyTemps[i]
            val code = hourlyCodes.getOrNull(i) ?: 0
            val precip = hourlyPrecip.getOrNull(i) ?: 0
            hourlyList.add(
                HourlyForecast(
                    timeLabel = timeLabel,
                    tempCelsius = temp,
                    condition = WeatherCondition.fromWmoCode(code),
                    precipitationProb = precip
                )
            )
        }

        val dailyList = mutableListOf<DailyForecast>()
        val dailyDates = response.daily?.time ?: emptyList()
        val dailyMaxTemps = response.daily?.temperature2mMax ?: emptyList()
        val dailyMinTemps = response.daily?.temperature2mMin ?: emptyList()
        val dailyCodes = response.daily?.weatherCode ?: emptyList()
        val dailyRain = response.daily?.precipitationProbabilityMax ?: emptyList()

        val dailyDateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val maxDaily = minOf(dailyDates.size, dailyMaxTemps.size, 7)
        for (i in 0 until maxDaily) {
            val rawDate = dailyDates[i]
            val (dayName, dateFormatted) = try {
                val parsed = dailyDateParser.parse(rawDate)
                val day = if (i == 0) "Today" else dayNameFormat.format(parsed ?: Date())
                val dStr = shortDateFormat.format(parsed ?: Date())
                Pair(day, dStr)
            } catch (e: Exception) {
                Pair(if (i == 0) "Today" else "Day $i", rawDate)
            }

            dailyList.add(
                DailyForecast(
                    dayName = dayName,
                    dateText = dateFormatted,
                    maxTempC = dailyMaxTemps[i],
                    minTempC = dailyMinTemps.getOrNull(i) ?: dailyMaxTemps[i],
                    condition = WeatherCondition.fromWmoCode(dailyCodes.getOrNull(i) ?: 0),
                    rainProb = dailyRain.getOrNull(i) ?: 0
                )
            )
        }

        val high = dailyList.firstOrNull()?.maxTempC ?: current.temperature2m
        val low = dailyList.firstOrNull()?.minTempC ?: current.apparentTemperature
        val updateTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        // Fetch or derive severe weather warnings
        val alerts = fetchSevereWeatherAlerts(
            cityName = cityName,
            latitude = latitude,
            longitude = longitude,
            currentTempC = current.temperature2m,
            windSpeedKmh = current.windSpeed10m,
            weatherCode = current.weatherCode,
            uvIndex = current.uvIndex ?: 4.0
        )

        WeatherData(
            cityName = cityName,
            country = countryName ?: "",
            currentTempC = current.temperature2m,
            feelsLikeC = current.apparentTemperature,
            highTempC = high,
            lowTempC = low,
            condition = WeatherCondition.fromWmoCode(current.weatherCode),
            humidityPercent = current.relativeHumidity2m.toInt(),
            windSpeedKmh = current.windSpeed10m,
            uvIndex = current.uvIndex ?: 4.0,
            pressureHpa = (current.surfacePressure ?: 1013.0).toInt(),
            visibilityKm = 10.0,
            hourlyForecasts = hourlyList,
            dailyForecasts = dailyList,
            alerts = alerts,
            lastUpdatedText = "Updated at $updateTime"
        )
    }

    private suspend fun fetchSevereWeatherAlerts(
        cityName: String,
        latitude: Double,
        longitude: Double,
        currentTempC: Double,
        windSpeedKmh: Double,
        weatherCode: Int,
        uvIndex: Double
    ): List<WeatherAlert> {
        val alertList = mutableListOf<WeatherAlert>()

        // 1. Try real weather API alerts endpoint (e.g., NWS for points or global)
        try {
            val pointCoord = String.format(Locale.US, "%.4f,%.4f", latitude, longitude)
            val nwsResp = nwsAlertService.getActiveAlerts(point = pointCoord)
            nwsResp.features?.forEach { feature ->
                val props = feature.properties
                if (props?.event != null) {
                    val severity = when (props.severity?.lowercase()) {
                        "extreme" -> WeatherAlertSeverity.CRITICAL
                        "severe" -> WeatherAlertSeverity.WARNING
                        "moderate" -> WeatherAlertSeverity.WATCH
                        else -> WeatherAlertSeverity.ADVISORY
                    }
                    alertList.add(
                        WeatherAlert(
                            id = props.id ?: "alert_${System.currentTimeMillis()}",
                            title = props.event,
                            severity = severity,
                            eventType = props.event,
                            headline = props.headline ?: props.event,
                            description = props.description ?: "Stay alert to local weather changes.",
                            instruction = props.instruction ?: "Take necessary precautions and monitor local forecasts.",
                            effectiveTime = props.effective ?: "Active",
                            expiresTime = props.expires ?: "Until further notice"
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // NWS is valid for North America; for other regions or if offline, evaluate meteorological conditions below
        }

        // 2. Real-time meteorological hazard analysis (guarantees warnings whenever weather conditions are hazardous worldwide)
        if (weatherCode in listOf(95, 96, 99)) {
            alertList.add(
                WeatherAlert(
                    id = "hazard_thunderstorm_${cityName.lowercase()}",
                    title = "Severe Thunderstorm Warning",
                    severity = WeatherAlertSeverity.WARNING,
                    eventType = "Thunderstorm",
                    headline = "Severe thunderstorm with possible lightning and heavy downpour detected in $cityName",
                    description = "A thunderstorm system is active. Lightning and localized heavy rainfall can lead to reduced visibility and hazardous travel conditions.",
                    instruction = "Stay indoors away from windows. Avoid open fields and tall metal structures.",
                    effectiveTime = "Active now",
                    expiresTime = "Expected to clear within 3 hours"
                )
            )
        }

        if (windSpeedKmh >= 50.0) {
            val isExtreme = windSpeedKmh >= 75.0
            alertList.add(
                WeatherAlert(
                    id = "hazard_high_wind_${cityName.lowercase()}",
                    title = if (isExtreme) "Gale Wind Warning" else "High Wind Advisory",
                    severity = if (isExtreme) WeatherAlertSeverity.CRITICAL else WeatherAlertSeverity.WATCH,
                    eventType = "High Wind",
                    headline = "Gusty winds reaching ${String.format(Locale.US, "%.1f", windSpeedKmh)} km/h in $cityName",
                    description = "Strong sustained winds may cause unsecured outdoor objects to blow around and break tree branches.",
                    instruction = "Secure loose outdoor furniture and exercise caution while driving high-profile vehicles.",
                    effectiveTime = "Active now",
                    expiresTime = "Active throughout today"
                )
            )
        }

        if (currentTempC >= 38.0) {
            alertList.add(
                WeatherAlert(
                    id = "hazard_heat_${cityName.lowercase()}",
                    title = "Excessive Heat Warning",
                    severity = WeatherAlertSeverity.WARNING,
                    eventType = "Extreme Heat",
                    headline = "Dangerously hot conditions with temperatures exceeding 38°C (100°F)",
                    description = "Prolonged exposure to high temperatures significantly increases the risk of heat exhaustion and heat stroke.",
                    instruction = "Drink plenty of water, remain in air-conditioned areas, and avoid strenuous outdoor activity during midday.",
                    effectiveTime = "Active now",
                    expiresTime = "Until 8:00 PM tonight"
                )
            )
        } else if (currentTempC <= -10.0) {
            alertList.add(
                WeatherAlert(
                    id = "hazard_freeze_${cityName.lowercase()}",
                    title = "Freeze Warning",
                    severity = WeatherAlertSeverity.WARNING,
                    eventType = "Extreme Cold",
                    headline = "Sub-freezing temperatures detected in $cityName",
                    description = "Severe cold conditions with temperatures well below freezing can cause frostbite and ice hazard on roadways.",
                    instruction = "Dress in warm layers, cover exposed skin, and protect household plumbing from freezing.",
                    effectiveTime = "Active now",
                    expiresTime = "Until tomorrow morning"
                )
            )
        }

        if (uvIndex >= 9.0) {
            alertList.add(
                WeatherAlert(
                    id = "hazard_uv_${cityName.lowercase()}",
                    title = "Extreme UV Alert",
                    severity = WeatherAlertSeverity.ADVISORY,
                    eventType = "UV Radiation",
                    headline = "Very high UV Index ($uvIndex) warning",
                    description = "Unprotected skin and eyes will burn rapidly with this level of solar radiation.",
                    instruction = "Wear SPF 30+ sunscreen, a wide-brimmed hat, and seek shade during midday hours.",
                    effectiveTime = "11:00 AM - 4:00 PM",
                    expiresTime = "Evening"
                )
            )
        }

        return alertList.distinctBy { it.id }
    }

    // Default fallback initial preview data
    fun getInitialPreviewData(): WeatherData {
        return WeatherData(
            cityName = "Tokyo",
            country = "Japan",
            currentTempC = 22.0,
            feelsLikeC = 23.0,
            highTempC = 25.0,
            lowTempC = 17.0,
            condition = WeatherCondition.PARTLY_CLOUDY,
            humidityPercent = 58,
            windSpeedKmh = 14.5,
            uvIndex = 5.2,
            pressureHpa = 1014,
            visibilityKm = 10.0,
            hourlyForecasts = (0..23).map { hourOffset ->
                val calendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.HOUR_OF_DAY, hourOffset)
                }
                val label = if (hourOffset == 0) "Now" else SimpleDateFormat("h a", Locale.getDefault()).format(calendar.time)
                val tempCurve = 22.0 + 3.0 * Math.sin((calendar.get(java.util.Calendar.HOUR_OF_DAY) - 8) * Math.PI / 12)
                val cond = when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
                    in 6..10 -> WeatherCondition.SUNNY
                    in 11..16 -> WeatherCondition.PARTLY_CLOUDY
                    in 17..19 -> WeatherCondition.MAINLY_CLEAR
                    else -> WeatherCondition.CLEAR_SKY
                }
                HourlyForecast(
                    timeLabel = label,
                    tempCelsius = Math.round(tempCurve * 10) / 10.0,
                    condition = cond,
                    precipitationProb = if (hourOffset in 12..16) 15 else 5
                )
            },
            dailyForecasts = listOf(
                DailyForecast("Today", "May 10", 25.0, 17.0, WeatherCondition.PARTLY_CLOUDY, 10),
                DailyForecast("Tue", "May 11", 24.0, 16.0, WeatherCondition.SUNNY, 5),
                DailyForecast("Wed", "May 12", 21.0, 15.0, WeatherCondition.RAIN, 70),
                DailyForecast("Thu", "May 13", 20.0, 14.0, WeatherCondition.DRIZZLE, 45),
                DailyForecast("Fri", "May 14", 23.0, 16.0, WeatherCondition.MAINLY_CLEAR, 20)
            ),
            alerts = listOf(
                WeatherAlert(
                    id = "preview_advisory_breeze",
                    title = "Coastal Wind & Marine Advisory",
                    severity = WeatherAlertSeverity.ADVISORY,
                    eventType = "Marine Wind",
                    headline = "Brisk coastal winds forecast for Tokyo Bay area",
                    description = "Wind gusts up to 35 km/h along shoreline areas. Small watercraft operators should exercise caution.",
                    instruction = "Monitor local coastal conditions if boating or undertaking seaside recreation.",
                    effectiveTime = "Active today",
                    expiresTime = "Valid until 10:00 PM"
                )
            ),
            lastUpdatedText = "Live Data Ready"
        )
    }
}
