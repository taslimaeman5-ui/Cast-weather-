package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TemperatureUnit
import com.example.model.WeatherCondition
import com.example.model.WeatherData

/**
 * Ultra-realistic, modern weather hero card inspired by top-tier weather applications.
 * Features frosted glassmorphic styling, ambient weather gradient,
 * high-precision typography, interactive UV circular gauge, and GPS trigger.
 */
@Composable
fun CurrentWeatherHeroCard(
    weather: WeatherData,
    unit: TemperatureUnit,
    onGpsLocationClick: () -> Unit = {},
    isGpsLocating: Boolean = false,
    isUsingGpsLocation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")

    // Gentle floating motion for weather icon
    val iconFloatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_float"
    )

    // Spinner for locating
    val locatingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "locating_rotation"
    )

    // Dynamic gradient reflecting the current condition
    val heroGradient = getConditionHeroGradient(weather.condition)

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        border = BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_weather_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Location Row with clickable GPS Target button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (weather.country.isNotEmpty()) "${weather.cityName}, ${weather.country}" else weather.cityName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("current_city_text")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // GPS Target Icon Button
                    Surface(
                        shape = CircleShape,
                        color = if (isUsingGpsLocation)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        border = BorderStroke(
                            1.dp,
                            if (isUsingGpsLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .size(34.dp)
                            .clickable(enabled = !isGpsLocating) {
                                onGpsLocationClick()
                            }
                            .testTag("city_gps_target_icon_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isGpsLocating) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = if (isUsingGpsLocation) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(locatingRotation)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Detect current GPS location",
                                    tint = if (isUsingGpsLocation) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Large Weather Icon & Massive Realistic Temperature
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        WeatherIcon(
                            condition = weather.condition,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(82.dp)
                                .scale(1f + iconFloatOffset * 0.015f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = unit.format(weather.currentTempC),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-2).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("current_temp_display")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Condition Pill with Frosted Glass Look
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = weather.condition.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("current_condition_text")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High / Low temperatures and Feels Like badge
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // High Temp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "High",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit.format(weather.highTempC),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("high_temp_display")
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Low Temp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Low",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit.format(weather.lowTempC),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("low_temp_display")
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Feels Like
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Feels like ${unit.format(weather.feelsLikeC)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("feels_like_display")
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Atmospheric Highlights Bar with Circular UV Index Gauge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Circular UV Index Meter
                    HeroUvCircularGauge(uvIndex = weather.uvIndex)

                    // Wind Quick Stat
                    HeroAtmosphericStatPill(
                        label = "Wind",
                        value = "${String.format("%.1f", weather.windSpeedKmh)} km/h",
                        detail = "Gentle Air"
                    )

                    // Humidity Quick Stat
                    HeroAtmosphericStatPill(
                        label = "Humidity",
                        value = "${weather.humidityPercent}%",
                        detail = if (weather.humidityPercent > 65) "Humid" else "Optimal"
                    )
                }
            }
        }
    }
}

/**
 * Circular progress indicator to visualize the current UV index data in the main weather dashboard.
 */
@Composable
fun HeroUvCircularGauge(uvIndex: Double) {
    val progress = (uvIndex / 11.0).coerceIn(0.0, 1.0).toFloat()
    val uvColor = when {
        uvIndex <= 2.5 -> Color(0xFF10B981) // Green (Low)
        uvIndex <= 5.5 -> Color(0xFFF59E0B) // Amber (Moderate)
        uvIndex <= 7.5 -> Color(0xFFF97316) // Orange (High)
        uvIndex <= 10.5 -> Color(0xFFEF4444) // Red (Very High)
        else -> Color(0xFF8B5CF6) // Violet (Extreme)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag("circular_uv_index_gauge")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(46.dp)
        ) {
            Canvas(modifier = Modifier.size(44.dp)) {
                // Background track
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                // Active arc
                drawArc(
                    color = uvColor,
                    startAngle = 140f,
                    sweepAngle = 260f * progress,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = uvColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "UV ${String.format("%.1f", uvIndex)}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    uvIndex <= 2.5 -> "Low"
                    uvIndex <= 5.5 -> "Moderate"
                    uvIndex <= 7.5 -> "High"
                    uvIndex <= 10.5 -> "Very High"
                    else -> "Extreme"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = uvColor
            )
        }
    }
}

@Composable
private fun HeroAtmosphericStatPill(
    label: String,
    value: String,
    detail: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = detail,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun getConditionHeroGradient(condition: WeatherCondition): Brush {
    return when (condition) {
        WeatherCondition.SUNNY,
        WeatherCondition.CLEAR_SKY -> Brush.verticalGradient(
            listOf(
                Color(0xFFFFB300).copy(alpha = 0.18f),
                Color(0xFF0284C7).copy(alpha = 0.10f)
            )
        )
        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.HEAVY_RAIN -> Brush.verticalGradient(
            listOf(
                Color(0xFF0284C7).copy(alpha = 0.20f),
                Color(0xFF334155).copy(alpha = 0.12f)
            )
        )
        WeatherCondition.THUNDERSTORM -> Brush.verticalGradient(
            listOf(
                Color(0xFF6366F1).copy(alpha = 0.25f),
                Color(0xFF1E1B4B).copy(alpha = 0.15f)
            )
        )
        WeatherCondition.SNOW -> Brush.verticalGradient(
            listOf(
                Color(0xFF38BDF8).copy(alpha = 0.20f),
                Color(0xFFF1F5F9).copy(alpha = 0.10f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFF64748B).copy(alpha = 0.15f),
                Color(0xFF94A3B8).copy(alpha = 0.08f)
            )
        )
    }
}
