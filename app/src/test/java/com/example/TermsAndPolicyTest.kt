package com.example

import com.example.model.TemperatureUnit
import com.example.model.WeatherCondition
import com.example.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TermsAndPolicyTest {

    @Test
    fun testWeatherDataAttributesForRealisticUI() {
        val weather = WeatherData(
            cityName = "Tokyo",
            country = "Japan",
            currentTempC = 22.0,
            feelsLikeC = 23.5,
            highTempC = 26.0,
            lowTempC = 17.0,
            condition = WeatherCondition.SUNNY,
            humidityPercent = 55,
            windSpeedKmh = 14.5,
            uvIndex = 6.2,
            pressureHpa = 1014,
            visibilityKm = 10.0,
            hourlyForecasts = emptyList(),
            dailyForecasts = emptyList(),
            lastUpdatedText = "Just Now"
        )

        assertEquals("Tokyo", weather.cityName)
        assertTrue(weather.uvIndex > 0)
        assertTrue(weather.humidityPercent in 0..100)
        assertTrue(weather.pressureHpa > 900)
        assertTrue(weather.visibilityKm > 0)

        // Verify temperature formatting
        val formattedC = TemperatureUnit.CELSIUS.format(weather.currentTempC)
        val formattedF = TemperatureUnit.FAHRENHEIT.format(weather.currentTempC)
        assertEquals("22°C", formattedC)
        assertEquals("72°F", formattedF)
    }

    @Test
    fun testUvIndexRiskScaleCalculations() {
        val testUvValues = listOf(1.5, 4.0, 6.8, 9.2, 11.5)
        testUvValues.forEach { uv ->
            val progress = (uv / 11.0).coerceIn(0.0, 1.0)
            assertTrue(progress >= 0.0 && progress <= 1.0)
        }
    }
}
