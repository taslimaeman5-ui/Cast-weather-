package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyForecast
import com.example.model.TemperatureUnit

@Composable
fun DailyForecastSection(
    dailyList: List<DailyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_forecast_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val overallMin = dailyList.minOfOrNull { it.minTempC } ?: 0.0
                val overallMax = dailyList.maxOfOrNull { it.maxTempC } ?: 40.0
                val totalSpan = maxOf(1.0, overallMax - overallMin)

                dailyList.forEachIndexed { index, forecast ->
                    DailyForecastRow(
                        forecast = forecast,
                        unit = unit,
                        overallMin = overallMin,
                        overallSpan = totalSpan
                    )
                    if (index < dailyList.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    forecast: DailyForecast,
    unit: TemperatureUnit,
    overallMin: Double,
    overallSpan: Double
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forecast_day_${forecast.dayName}")
    ) {
        // Day name & date
        Column(modifier = Modifier.width(76.dp)) {
            Text(
                text = forecast.dayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = forecast.dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Icon + Rain probability
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(68.dp)
        ) {
            WeatherIcon(
                condition = forecast.condition,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            if (forecast.rainProb > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Rain",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${forecast.rainProb}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Low temperature display
        Text(
            text = unit.format(forecast.minTempC),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(44.dp)
                .testTag("daily_low_${forecast.dayName}")
        )

        // Visual temperature progress bar
        val progress = (((forecast.maxTempC - forecast.minTempC) / overallSpan).toFloat()).coerceIn(0.15f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // High temperature display
        Text(
            text = unit.format(forecast.maxTempC),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .width(44.dp)
                .testTag("daily_high_${forecast.dayName}")
        )
    }
}
