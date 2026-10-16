package com.example.model

enum class TemperatureUnit(val symbol: String, val label: String) {
    CELSIUS("°C", "Celsius"),
    FAHRENHEIT("°F", "Fahrenheit");

    fun convertFromCelsius(tempC: Double): Double {
        return when (this) {
            CELSIUS -> tempC
            FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
        }
    }

    fun format(tempC: Double, showSymbol: Boolean = true): String {
        val converted = convertFromCelsius(tempC)
        val rounded = Math.round(converted).toInt()
        return if (showSymbol) "$rounded$symbol" else "$rounded"
    }
}

enum class WeatherCondition(val displayName: String) {
    CLEAR_SKY("Clear Sky"),
    SUNNY("Sunny"),
    MAINLY_CLEAR("Mainly Clear"),
    PARTLY_CLOUDY("Partly Cloudy"),
    OVERCAST("Overcast"),
    FOGGY("Foggy"),
    DRIZZLE("Light Drizzle"),
    RAIN("Rain"),
    HEAVY_RAIN("Heavy Rain"),
    THUNDERSTORM("Thunderstorm"),
    SNOW("Snow"),
    WINDY("Windy");

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition {
            return when (code) {
                0 -> SUNNY
                1 -> MAINLY_CLEAR
                2 -> PARTLY_CLOUDY
                3 -> OVERCAST
                45, 48 -> FOGGY
                51, 53, 55, 56, 57 -> DRIZZLE
                61, 63, 65, 80, 81, 82 -> RAIN
                66, 67, 71, 73, 75, 77, 85, 86 -> SNOW
                95, 96, 99 -> THUNDERSTORM
                else -> PARTLY_CLOUDY
            }
        }
    }
}

data class HourlyForecast(
    val timeLabel: String,
    val tempCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProb: Int
)

data class DailyForecast(
    val dayName: String,
    val dateText: String,
    val maxTempC: Double,
    val minTempC: Double,
    val condition: WeatherCondition,
    val rainProb: Int
)

enum class WeatherAlertSeverity(val level: String) {
    ADVISORY("Advisory"),
    WATCH("Watch"),
    WARNING("Warning"),
    CRITICAL("Severe Hazard")
}

data class WeatherAlert(
    val id: String,
    val title: String,
    val severity: WeatherAlertSeverity,
    val eventType: String,
    val headline: String,
    val description: String,
    val instruction: String,
    val effectiveTime: String,
    val expiresTime: String
)

data class WeatherData(
    val cityName: String,
    val country: String,
    val currentTempC: Double,
    val feelsLikeC: Double,
    val highTempC: Double,
    val lowTempC: Double,
    val condition: WeatherCondition,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val uvIndex: Double,
    val pressureHpa: Int,
    val visibilityKm: Double,
    val hourlyForecasts: List<HourlyForecast>,
    val dailyForecasts: List<DailyForecast>,
    val alerts: List<WeatherAlert> = emptyList(),
    val lastUpdatedText: String
)
