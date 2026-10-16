package com.example.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.model.WeatherCondition
import com.example.ui.util.WeatherIconMapper

/**
 * Reusable WeatherIcon displaying representative Material Icons mapped via [WeatherIconMapper].
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val visualInfo = WeatherIconMapper.getVisualInfo(condition)

    Icon(
        imageVector = visualInfo.icon,
        contentDescription = contentDescription ?: visualInfo.description,
        tint = tint,
        modifier = modifier
    )
}
