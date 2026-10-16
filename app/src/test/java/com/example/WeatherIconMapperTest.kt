package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import com.example.model.WeatherCondition
import com.example.ui.util.WeatherIconMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WeatherIconMapperTest {

    @Test
    fun testWeatherIconMappings() {
        // Sunny / Clear sky maps to sun icon
        val sunVisual = WeatherIconMapper.getVisualInfo(WeatherCondition.SUNNY)
        assertEquals(Icons.Default.WbSunny, sunVisual.icon)

        // Rain maps to water drop icon
        val rainVisual = WeatherIconMapper.getVisualInfo(WeatherCondition.RAIN)
        assertEquals(Icons.Default.WaterDrop, rainVisual.icon)
        assertEquals(true, rainVisual.isPrecipitation)

        // Snow maps to snowflake icon
        val snowVisual = WeatherIconMapper.getVisualInfo(WeatherCondition.SNOW)
        assertEquals(Icons.Default.AcUnit, snowVisual.icon)
        assertEquals(true, snowVisual.isPrecipitation)

        // Overcast maps to cloudy icon
        val cloudVisual = WeatherIconMapper.getVisualInfo(WeatherCondition.OVERCAST)
        assertEquals(Icons.Default.WbCloudy, cloudVisual.icon)

        // Partly cloudy
        val partlyCloudyVisual = WeatherIconMapper.getVisualInfo(WeatherCondition.PARTLY_CLOUDY)
        assertEquals(Icons.Default.CloudQueue, partlyCloudyVisual.icon)

        // Windy
        val windyVisual = WeatherIconMapper.getVisualInfo(WeatherCondition.WINDY)
        assertEquals(Icons.Default.Air, windyVisual.icon)

        // WMO code mapping
        assertEquals(Icons.Default.WbSunny, WeatherIconMapper.getIconFromWmoCode(0))
        assertEquals(Icons.Default.WaterDrop, WeatherIconMapper.getIconFromWmoCode(61))
    }
}
