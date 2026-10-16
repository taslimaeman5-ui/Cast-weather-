package com.example.network

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "admin1") val admin1: String? = null
)

data class OpenMeteoResponse(
    @Json(name = "current") val current: CurrentWeatherDto? = null,
    @Json(name = "hourly") val hourly: HourlyWeatherDto? = null,
    @Json(name = "daily") val daily: DailyWeatherDto? = null
)

// National Weather Service / Global alert models
data class NwsAlertsResponse(
    @Json(name = "features") val features: List<NwsAlertFeature>? = null
)

data class NwsAlertFeature(
    @Json(name = "properties") val properties: NwsAlertProperties? = null
)

data class NwsAlertProperties(
    @Json(name = "id") val id: String? = null,
    @Json(name = "event") val event: String? = null,
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "instruction") val instruction: String? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "urgency") val urgency: String? = null,
    @Json(name = "effective") val effective: String? = null,
    @Json(name = "expires") val expires: String? = null
)

interface NwsAlertApiService {
    @GET("alerts/active")
    suspend fun getActiveAlerts(
        @Query("point") point: String
    ): NwsAlertsResponse
}

data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @Json(name = "surface_pressure") val surfacePressure: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null
)

data class HourlyWeatherDto(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "temperature_2m") val temperature2m: List<Double>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null
)

data class DailyWeatherDto(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,surface_pressure,uv_index",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}
