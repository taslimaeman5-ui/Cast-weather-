package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TemperatureUnit
import com.example.model.WeatherData

/**
 * Realistic modular weather details grid inspired by Apple Weather and Pixel Weather.
 * Features visual gauges: Circular UV Arc, Wind Compass Rose, Humidity Moisture Bar,
 * Pressure Barometer Gauge, and Visibility Rating.
 */
@Composable
fun WeatherDetailsGrid(
    weather: WeatherData,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: UV Index & Wind Speed with visual meters
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // UV Index Card with Circular Gauge
            DetailedUvIndexCard(
                uvIndex = weather.uvIndex,
                modifier = Modifier.weight(1f)
            )

            // Wind Speed Card with Compass Rose
            DetailedWindCard(
                windSpeedKmh = weather.windSpeedKmh,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Humidity & Air Pressure
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Humidity Card with Moisture Bar
            DetailedHumidityCard(
                humidityPercent = weather.humidityPercent,
                tempC = weather.currentTempC,
                modifier = Modifier.weight(1f)
            )

            // Air Pressure Card with Barometer Gauge
            DetailedPressureCard(
                pressureHpa = weather.pressureHpa,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Feels Like & Visibility
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Feels Like Temperature
            DetailedFeelsLikeCard(
                feelsLikeC = weather.feelsLikeC,
                actualTempC = weather.currentTempC,
                unit = unit,
                modifier = Modifier.weight(1f)
            )

            // Visibility Card
            DetailedVisibilityCard(
                visibilityKm = weather.visibilityKm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * UV Index Card with a prominent circular arc meter.
 */
@Composable
private fun DetailedUvIndexCard(
    uvIndex: Double,
    modifier: Modifier = Modifier
) {
    val progress = (uvIndex / 11.0).coerceIn(0.0, 1.0).toFloat()
    val uvColor = when {
        uvIndex <= 2.5 -> Color(0xFF10B981) // Green (Low)
        uvIndex <= 5.5 -> Color(0xFFF59E0B) // Amber (Moderate)
        uvIndex <= 7.5 -> Color(0xFFF97316) // Orange (High)
        uvIndex <= 10.5 -> Color(0xFFEF4444) // Red (Very High)
        else -> Color(0xFF8B5CF6) // Violet (Extreme)
    }

    val riskLevel = when {
        uvIndex <= 2.5 -> "Low"
        uvIndex <= 5.5 -> "Moderate"
        uvIndex <= 7.5 -> "High"
        uvIndex <= 10.5 -> "Very High"
        else -> "Extreme"
    }

    val recommendation = when {
        uvIndex <= 2.5 -> "Minimal sun protection needed"
        uvIndex <= 5.5 -> "Cover up, use SPF 30 sunscreen"
        uvIndex <= 7.5 -> "Seek shade during midday hours"
        uvIndex <= 10.5 -> "Avoid sun exposure 11:00-15:00"
        else -> "Take all precautions, stay indoors"
    }

    MetricCardContainer(
        title = "UV Index",
        icon = Icons.Default.WbSunny,
        modifier = modifier.testTag("metric_card_uv_index")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%.1f", uvIndex),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = riskLevel,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = uvColor
                )
            }

            // Circular Arc Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(52.dp)
            ) {
                Canvas(modifier = Modifier.size(48.dp)) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = uvColor,
                        startAngle = 140f,
                        sweepAngle = 260f * progress,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = uvColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recommendation,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            lineHeight = 15.sp
        )
    }
}

/**
 * Wind Card with Compass Rose direction needle.
 */
@Composable
private fun DetailedWindCard(
    windSpeedKmh: Double,
    modifier: Modifier = Modifier
) {
    val beaufortDescription = when {
        windSpeedKmh < 5.0 -> "Calm air"
        windSpeedKmh < 12.0 -> "Light breeze"
        windSpeedKmh < 20.0 -> "Gentle breeze"
        windSpeedKmh < 29.0 -> "Moderate breeze"
        windSpeedKmh < 39.0 -> "Fresh breeze"
        else -> "Strong gusts"
    }

    MetricCardContainer(
        title = "Wind",
        icon = Icons.Default.Air,
        modifier = modifier.testTag("metric_card_wind")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${String.format("%.1f", windSpeedKmh)} km/h",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = beaufortDescription,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Compass Rose
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Wind Direction",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(45f) // NW vector
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gusts up to ${String.format("%.1f", windSpeedKmh * 1.35)} km/h • Direction NW",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

/**
 * Humidity Card with moisture bar and calculated dew point.
 */
@Composable
private fun DetailedHumidityCard(
    humidityPercent: Int,
    tempC: Double,
    modifier: Modifier = Modifier
) {
    // Magnus-Tetens formula approximation for dew point
    val a = 17.27
    val b = 237.7
    val alpha = ((a * tempC) / (b + tempC)) + Math.log(humidityPercent / 100.0)
    val dewPoint = (b * alpha) / (a - alpha)

    val humidityStatus = when {
        humidityPercent < 35 -> "Dry"
        humidityPercent <= 65 -> "Comfortable"
        else -> "High Humidity"
    }

    MetricCardContainer(
        title = "Humidity",
        icon = Icons.Default.WaterDrop,
        modifier = modifier.testTag("metric_card_humidity")
    ) {
        Text(
            text = "$humidityPercent%",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Moisture Level Bar
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (humidityPercent / 100f).coerceIn(0f, 1f))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF0284C7)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The dew point is ${String.format("%.1f", dewPoint)}°C • $humidityStatus",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

/**
 * Air Pressure Card with Barometer Scale and trend.
 */
@Composable
private fun DetailedPressureCard(
    pressureHpa: Int,
    modifier: Modifier = Modifier
) {
    val pressureStatus = when {
        pressureHpa < 1005 -> "Low Pressure"
        pressureHpa <= 1020 -> "Normal"
        else -> "High Pressure"
    }

    MetricCardContainer(
        title = "Air Pressure",
        icon = Icons.Default.Compress,
        modifier = modifier.testTag("metric_card_pressure")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$pressureHpa hPa",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = pressureStatus,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Dial representation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp)
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Indicator position based on 980 - 1040 hPa range
                    val fraction = ((pressureHpa - 980f) / 60f).coerceIn(0f, 1f)
                    drawArc(
                        color = Color(0xFF0284C7),
                        startAngle = 180f,
                        sweepAngle = 180f * fraction,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Atmospheric barometric pressure is steady",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

/**
 * Feels Like Temperature Card.
 */
@Composable
private fun DetailedFeelsLikeCard(
    feelsLikeC: Double,
    actualTempC: Double,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    val diff = feelsLikeC - actualTempC
    val diffText = when {
        diff > 1.0 -> "Warmer than actual due to humidity"
        diff < -1.0 -> "Cooler than actual due to wind chill"
        else -> "Similar to actual temperature"
    }

    MetricCardContainer(
        title = "Feels Like",
        icon = Icons.Default.Thermostat,
        modifier = modifier.testTag("metric_card_feels_like")
    ) {
        Text(
            text = unit.format(feelsLikeC),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Actual: ${unit.format(actualTempC)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = diffText,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

/**
 * Visibility Card.
 */
@Composable
private fun DetailedVisibilityCard(
    visibilityKm: Double,
    modifier: Modifier = Modifier
) {
    val visibilityRating = when {
        visibilityKm >= 10.0 -> "Clear View"
        visibilityKm >= 5.0 -> "Moderate View"
        visibilityKm >= 2.0 -> "Hazy View"
        else -> "Foggy / Low View"
    }

    MetricCardContainer(
        title = "Visibility",
        icon = Icons.Default.Visibility,
        modifier = modifier.testTag("metric_card_visibility")
    ) {
        Text(
            text = "${String.format("%.1f", visibilityKm)} km",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = visibilityRating,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (visibilityKm >= 10.0) "Perfect atmospheric horizon clarity" else "Expect reduced distance view",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

/**
 * Reusable frosted container card for metric items.
 */
@Composable
private fun MetricCardContainer(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
