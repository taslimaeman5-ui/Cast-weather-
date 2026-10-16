package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TemperatureUnit

/**
 * Modern pill-style toggle switch for Celsius (°C) and Fahrenheit (°F).
 * Supports smooth sliding indicator animation and clear tactile affordance.
 */
@Composable
fun TemperatureUnitToggle(
    currentUnit: TemperatureUnit,
    onUnitChanged: (TemperatureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCelsius = currentUnit == TemperatureUnit.CELSIUS
    val pillWidth = 46.dp
    val totalWidth = 96.dp
    val height = 40.dp

    val offsetAnimation by animateDpAsState(
        targetValue = if (isCelsius) 2.dp else 48.dp,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "pill_offset"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        tonalElevation = 2.dp,
        modifier = modifier
            .testTag("temperature_unit_toggle")
    ) {
        Box(
            modifier = Modifier
                .width(totalWidth)
                .height(height)
                .padding(2.dp)
        ) {
            // Sliding active indicator pill
            Box(
                modifier = Modifier
                    .offset(x = offsetAnimation)
                    .width(pillWidth)
                    .fillMaxHeight()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Labels row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Celsius button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onUnitChanged(TemperatureUnit.CELSIUS) }
                        )
                        .testTag("celsius_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val textColor by animateColorAsState(
                        targetValue = if (isCelsius) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "celsius_text_color"
                    )
                    Text(
                        text = "°C",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = if (isCelsius) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Fahrenheit button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onUnitChanged(TemperatureUnit.FAHRENHEIT) }
                        )
                        .testTag("fahrenheit_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val textColor by animateColorAsState(
                        targetValue = if (!isCelsius) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 200),
                        label = "fahrenheit_text_color"
                    )
                    Text(
                        text = "°F",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = if (!isCelsius) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
