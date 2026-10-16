package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.WeatherCondition

/**
 * Data representation for weather visual styling.
 */
data class WeatherVisualInfo(
    val icon: ImageVector,
    val defaultColor: Color,
    val description: String,
    val isPrecipitation: Boolean = false
)

/**
 * Helper mapper object for weather icons and visual tokens using Material Icons.
 */
object WeatherIconMapper {

    /**
     * Maps a [WeatherCondition] to its representative Material Icon, semantic color, and description.
     */
    fun getVisualInfo(condition: WeatherCondition): WeatherVisualInfo {
        return when (condition) {
            WeatherCondition.SUNNY,
            WeatherCondition.CLEAR_SKY -> WeatherVisualInfo(
                icon = Icons.Default.WbSunny,
                defaultColor = Color(0xFFFFA000), // Amber / Warm Sun
                description = "Clear Sunny Sky"
            )

            WeatherCondition.MAINLY_CLEAR,
            WeatherCondition.PARTLY_CLOUDY -> WeatherVisualInfo(
                icon = Icons.Default.CloudQueue,
                defaultColor = Color(0xFF5C6BC0), // Indigo / Soft Cloudy
                description = "Partly Cloudy"
            )

            WeatherCondition.OVERCAST -> WeatherVisualInfo(
                icon = Icons.Default.WbCloudy,
                defaultColor = Color(0xFF78909C), // Blue Grey
                description = "Overcast Clouds"
            )

            WeatherCondition.FOGGY -> WeatherVisualInfo(
                icon = Icons.Default.Cloud,
                defaultColor = Color(0xFF90A4AE), // Misty Grey
                description = "Fog and Mist"
            )

            WeatherCondition.DRIZZLE -> WeatherVisualInfo(
                icon = Icons.Default.Grain,
                defaultColor = Color(0xFF42A5F5), // Light Blue
                description = "Light Drizzle",
                isPrecipitation = true
            )

            WeatherCondition.RAIN -> WeatherVisualInfo(
                icon = Icons.Default.WaterDrop,
                defaultColor = Color(0xFF1E88E5), // Vivid Blue
                description = "Rain Showers",
                isPrecipitation = true
            )

            WeatherCondition.HEAVY_RAIN -> WeatherVisualInfo(
                icon = Icons.Default.WaterDrop,
                defaultColor = Color(0xFF1565C0), // Deep Rain Blue
                description = "Heavy Rain",
                isPrecipitation = true
            )

            WeatherCondition.THUNDERSTORM -> WeatherVisualInfo(
                icon = Icons.Default.Thunderstorm,
                defaultColor = Color(0xFF5E35B1), // Deep Violet Thunder
                description = "Thunderstorm with Lightning",
                isPrecipitation = true
            )

            WeatherCondition.SNOW -> WeatherVisualInfo(
                icon = Icons.Default.AcUnit,
                defaultColor = Color(0xFF4DD0E1), // Cyan / Snow Ice
                description = "Snow Flurries",
                isPrecipitation = true
            )

            WeatherCondition.WINDY -> WeatherVisualInfo(
                icon = Icons.Default.Air,
                defaultColor = Color(0xFF26A69A), // Teal Wind
                description = "Breezy Wind"
            )
        }
    }

    /**
     * Direct helper to get the Material ImageVector for a given condition.
     */
    fun getIcon(condition: WeatherCondition): ImageVector = getVisualInfo(condition).icon

    /**
     * Helper to get the Material ImageVector from a raw WMO weather code.
     */
    fun getIconFromWmoCode(wmoCode: Int): ImageVector =
        getIcon(WeatherCondition.fromWmoCode(wmoCode))
}
