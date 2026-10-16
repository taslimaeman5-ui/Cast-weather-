package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.WeatherCondition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val blinkSpeed: Float,
    val phaseOffset: Float
)

private data class RainParticle(
    val initialX: Float,
    val initialY: Float,
    val length: Float,
    val speed: Float,
    val alpha: Float
)

private data class CloudParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radiusRatio: Float,
    val speed: Float,
    val opacity: Float
)

/**
 * Photorealistic dynamic atmospheric background for SkyCast Weather.
 * Features true-to-life sky gradients, atmospheric light scattering,
 * star fields, realistic cloud volume, rain streaks, and lightning glows.
 */
@Composable
fun DynamicWeatherBackground(
    condition: WeatherCondition,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim_transition")

    // Continuous progress loop [0f..1f] for particle movement
    val linearProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "linear_progress"
    )

    // Sun rays rotation progress [0f..360f]
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    // Atmospheric sun & light pulse
    val atmosphericPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "atmospheric_pulse"
    )

    // Lightning flash pulse for thunderstorms
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightning_flash"
    )

    // Pre-generate stars for night / clear sky
    val stars = remember {
        val rand = Random(77)
        List(50) {
            StarParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.70f,
                radius = 1.0f + rand.nextFloat() * 2.2f,
                blinkSpeed = 1.5f + rand.nextFloat() * 2.5f,
                phaseOffset = rand.nextFloat() * 2 * PI.toFloat()
            )
        }
    }

    // Pre-generate realistic rain particles
    val rainParticles = remember {
        val rand = Random(42)
        List(60) {
            RainParticle(
                initialX = rand.nextFloat(),
                initialY = rand.nextFloat(),
                length = 18f + rand.nextFloat() * 22f,
                speed = 0.9f + rand.nextFloat() * 0.6f,
                alpha = 0.35f + rand.nextFloat() * 0.45f
            )
        }
    }

    // Pre-generate soft atmospheric clouds
    val clouds = remember {
        val rand = Random(99)
        List(6) {
            CloudParticle(
                xRatio = rand.nextFloat(),
                yRatio = 0.06f + rand.nextFloat() * 0.22f,
                radiusRatio = 0.25f + rand.nextFloat() * 0.25f,
                speed = 0.08f + rand.nextFloat() * 0.12f,
                opacity = 0.15f + rand.nextFloat() * 0.20f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw photorealistic atmospheric gradient
        drawRealisticAtmosphere(condition, isDarkTheme, w, h)

        // 2. Night stars if dark theme or clear night
        if (isDarkTheme && condition in listOf(WeatherCondition.CLEAR_SKY, WeatherCondition.SUNNY, WeatherCondition.MAINLY_CLEAR)) {
            drawStarField(stars, linearProgress, w, h)
            drawRealisticMoon(w, h)
        }

        // 3. Condition-specific photorealistic weather phenomena
        when (condition) {
            WeatherCondition.SUNNY,
            WeatherCondition.CLEAR_SKY -> {
                if (!isDarkTheme) {
                    drawRealisticSunHalo(sunRotation, atmosphericPulse, w, h)
                }
            }

            WeatherCondition.MAINLY_CLEAR,
            WeatherCondition.PARTLY_CLOUDY -> {
                if (!isDarkTheme) {
                    drawSoftSunHaze(atmosphericPulse, w, h)
                }
                drawVolumetricClouds(clouds, linearProgress, isDarkTheme, w, h)
            }

            WeatherCondition.OVERCAST,
            WeatherCondition.FOGGY -> {
                drawVolumetricClouds(clouds, linearProgress, isDarkTheme, w, h)
                drawMistyFogLayers(atmosphericPulse, isDarkTheme, w, h)
            }

            WeatherCondition.DRIZZLE,
            WeatherCondition.RAIN,
            WeatherCondition.HEAVY_RAIN -> {
                drawVolumetricClouds(clouds, linearProgress, isDarkTheme, w, h)
                drawRealisticRain(rainParticles, linearProgress, condition == WeatherCondition.HEAVY_RAIN, w, h)
                drawGroundMist(w, h, isDarkTheme)
            }

            WeatherCondition.THUNDERSTORM -> {
                drawVolumetricClouds(clouds, linearProgress, isDarkTheme, w, h)
                drawRealisticRain(rainParticles, linearProgress, true, w, h)
                drawLightningFlash(lightningFlash, w, h)
            }

            WeatherCondition.SNOW -> {
                drawVolumetricClouds(clouds, linearProgress, isDarkTheme, w, h)
                drawRealisticSnow(rainParticles, linearProgress, isDarkTheme, w, h)
            }

            WeatherCondition.WINDY -> {
                drawWindCurrents(linearProgress, isDarkTheme, w, h)
            }
        }
    }
}

/**
 * Photorealistic multi-stop gradient simulating Rayleigh scattering in Earth's atmosphere.
 */
private fun DrawScope.drawRealisticAtmosphere(
    condition: WeatherCondition,
    isDarkTheme: Boolean,
    w: Float,
    h: Float
) {
    val gradientColors = if (isDarkTheme) {
        when (condition) {
            WeatherCondition.SUNNY,
            WeatherCondition.CLEAR_SKY,
            WeatherCondition.MAINLY_CLEAR -> listOf(
                Color(0xFF090D1A), // Deep cosmic zenith
                Color(0xFF0F1B2F), // Atmospheric midnight blue
                Color(0xFF14243B)  // Horizon glow
            )
            WeatherCondition.PARTLY_CLOUDY -> listOf(
                Color(0xFF0C1322),
                Color(0xFF162133),
                Color(0xFF1B2A3E)
            )
            WeatherCondition.OVERCAST,
            WeatherCondition.FOGGY -> listOf(
                Color(0xFF111822),
                Color(0xFF1A232E),
                Color(0xFF222C38)
            )
            WeatherCondition.DRIZZLE,
            WeatherCondition.RAIN,
            WeatherCondition.HEAVY_RAIN -> listOf(
                Color(0xFF0C141F),
                Color(0xFF152230),
                Color(0xFF1C2D3E)
            )
            WeatherCondition.THUNDERSTORM -> listOf(
                Color(0xFF130E24), // Dark electric indigo
                Color(0xFF1B1533),
                Color(0xFF221C3D)
            )
            WeatherCondition.SNOW -> listOf(
                Color(0xFF0E1724),
                Color(0xFF162335),
                Color(0xFF223247)
            )
            WeatherCondition.WINDY -> listOf(
                Color(0xFF0E1A29),
                Color(0xFF18283D),
                Color(0xFF20344D)
            )
        }
    } else {
        when (condition) {
            WeatherCondition.SUNNY,
            WeatherCondition.CLEAR_SKY -> listOf(
                Color(0xFF1A73E8), // Radiant sky blue zenith
                Color(0xFF3EA6FF), // Luminous azure
                Color(0xFF90CAF9), // Mid atmosphere
                Color(0xFFE3F2FD)  // Warm horizon haze
            )
            WeatherCondition.MAINLY_CLEAR,
            WeatherCondition.PARTLY_CLOUDY -> listOf(
                Color(0xFF2B7DE9),
                Color(0xFF5BA3F5),
                Color(0xFF9DC7F8),
                Color(0xFFE8F1FC)
            )
            WeatherCondition.OVERCAST,
            WeatherCondition.FOGGY -> listOf(
                Color(0xFF4A6572),
                Color(0xFF6B828F),
                Color(0xFF90A4AE),
                Color(0xFFCFD8DC)
            )
            WeatherCondition.DRIZZLE,
            WeatherCondition.RAIN -> listOf(
                Color(0xFF2C3E50),
                Color(0xFF4B6584),
                Color(0xFF778CA3),
                Color(0xFFD1D8E0)
            )
            WeatherCondition.HEAVY_RAIN -> listOf(
                Color(0xFF1E272E),
                Color(0xFF34495E),
                Color(0xFF485460),
                Color(0xFF808E9B)
            )
            WeatherCondition.THUNDERSTORM -> listOf(
                Color(0xFF2D1B4E),
                Color(0xFF433267),
                Color(0xFF5C4782),
                Color(0xFF8A77AE)
            )
            WeatherCondition.SNOW -> listOf(
                Color(0xFF536976),
                Color(0xFF758A99),
                Color(0xFF9DB0BC),
                Color(0xFFECEFF1)
            )
            WeatherCondition.WINDY -> listOf(
                Color(0xFF3A6073),
                Color(0xFF5C8296),
                Color(0xFF84A6B8),
                Color(0xFFDFE9EE)
            )
        }
    }

    drawRect(
        brush = Brush.verticalGradient(
            colors = gradientColors,
            startY = 0f,
            endY = h
        )
    )
}

/**
 * Realistic solar corona, halo, and ambient bloom.
 */
private fun DrawScope.drawRealisticSunHalo(
    rotation: Float,
    pulse: Float,
    w: Float,
    h: Float
) {
    val sunCenter = Offset(w * 0.82f, h * 0.10f)
    val sunBaseRadius = 54f * pulse

    // Soft outer atmospheric bloom (3x radius)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFEE58).copy(alpha = 0.40f),
                Color(0xFFFFCA28).copy(alpha = 0.22f),
                Color(0xFFFFB300).copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = sunCenter,
            radius = sunBaseRadius * 4.2f
        ),
        radius = sunBaseRadius * 4.2f,
        center = sunCenter
    )

    // Inner radiant corona
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF9C4).copy(alpha = 0.95f),
                Color(0xFFFFD54F).copy(alpha = 0.80f),
                Color(0xFFFFB300).copy(alpha = 0.50f)
            ),
            center = sunCenter,
            radius = sunBaseRadius * 0.90f
        ),
        radius = sunBaseRadius * 0.90f,
        center = sunCenter
    )

    // Rotating delicate light rays
    rotate(degrees = rotation, pivot = sunCenter) {
        val numRays = 16
        val innerRay = sunBaseRadius * 1.05f
        val outerRay = sunBaseRadius * 2.6f
        for (i in 0 until numRays) {
            val angle = (i * (2 * PI / numRays)).toFloat()
            val start = Offset(
                sunCenter.x + cos(angle) * innerRay,
                sunCenter.y + sin(angle) * innerRay
            )
            val end = Offset(
                sunCenter.x + cos(angle) * outerRay,
                sunCenter.y + sin(angle) * outerRay
            )
            drawLine(
                color = Color(0xFFFFE082).copy(alpha = 0.30f),
                start = start,
                end = end,
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawSoftSunHaze(pulse: Float, w: Float, h: Float) {
    val sunCenter = Offset(w * 0.78f, h * 0.12f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD54F).copy(alpha = 0.25f * pulse),
                Color(0xFFFFCA28).copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = sunCenter,
            radius = 160f * pulse
        ),
        radius = 160f * pulse,
        center = sunCenter
    )
}

/**
 * Realistic starry night field with variable twinkling.
 */
private fun DrawScope.drawStarField(
    stars: List<StarParticle>,
    progress: Float,
    w: Float,
    h: Float
) {
    stars.forEach { star ->
        val twinkle = (sin(progress * 2 * PI * star.blinkSpeed + star.phaseOffset) + 1f) / 2f
        val alpha = 0.25f + twinkle * 0.65f
        drawCircle(
            color = Color.White.copy(alpha = alpha.toFloat()),
            radius = star.radius * (0.8f + twinkle.toFloat() * 0.4f),
            center = Offset(star.xRatio * w, star.yRatio * h)
        )
    }
}

/**
 * Photorealistic moon with soft lunar corona and gentle crescent shading.
 */
private fun DrawScope.drawRealisticMoon(w: Float, h: Float) {
    val moonCenter = Offset(w * 0.82f, h * 0.12f)
    val moonRadius = 32f

    // Lunar atmospheric glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE0E7FF).copy(alpha = 0.25f),
                Color(0xFFC7D2FE).copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = moonCenter,
            radius = moonRadius * 3f
        ),
        radius = moonRadius * 3f,
        center = moonCenter
    )

    // Moon disk
    drawCircle(
        color = Color(0xFFF1F5F9),
        radius = moonRadius,
        center = moonCenter
    )

    // Subtle dark crescent overlay
    drawCircle(
        color = Color(0xFF0F1B2F).copy(alpha = 0.85f),
        radius = moonRadius * 0.85f,
        center = Offset(moonCenter.x + moonRadius * 0.45f, moonCenter.y - moonRadius * 0.2f)
    )
}

/**
 * Volumetric drifting cloud puffs with soft diffuse shading.
 */
private fun DrawScope.drawVolumetricClouds(
    clouds: List<CloudParticle>,
    progress: Float,
    isDarkTheme: Boolean,
    w: Float,
    h: Float
) {
    val cloudColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    clouds.forEach { cloud ->
        val currentX = ((cloud.xRatio + progress * cloud.speed) % 1.3f - 0.15f) * w
        val centerY = cloud.yRatio * h
        val radius = cloud.radiusRatio * w

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cloudColor.copy(alpha = cloud.opacity),
                    cloudColor.copy(alpha = cloud.opacity * 0.5f),
                    Color.Transparent
                ),
                center = Offset(currentX, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(currentX, centerY)
        )
    }
}

/**
 * High-fidelity rain simulation with variable streaks and splash mist.
 */
private fun DrawScope.drawRealisticRain(
    rainParticles: List<RainParticle>,
    progress: Float,
    isHeavy: Boolean,
    w: Float,
    h: Float
) {
    val slant = 0.18f // Wind slant
    val rainColor = Color(0xFFE2E8F0)

    rainParticles.forEach { particle ->
        val currentY = ((particle.initialY + progress * particle.speed * (if (isHeavy) 1.8f else 1.3f)) % 1f) * h
        val currentX = (particle.initialX * w + currentY * slant) % w

        drawLine(
            color = rainColor.copy(alpha = particle.alpha * (if (isHeavy) 0.65f else 0.45f)),
            start = Offset(currentX, currentY),
            end = Offset(currentX + particle.length * slant, currentY + particle.length),
            strokeWidth = if (isHeavy) 2.2f else 1.4f
        )
    }
}

/**
 * Realistic lightning flash that illuminates clouds dynamically.
 */
private fun DrawScope.drawLightningFlash(flashProgress: Float, w: Float, h: Float) {
    // Sharp momentary flash between progress 0.60 and 0.64
    val isFlashing = flashProgress in 0.60f..0.63f || flashProgress in 0.64f..0.66f
    if (isFlashing) {
        val intensity = if (flashProgress in 0.64f..0.66f) 0.40f else 0.70f
        drawRect(
            color = Color(0xFF818CF8).copy(alpha = intensity * 0.35f),
            size = Size(w, h)
        )
        // Upper atmosphere bolt glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = intensity * 0.8f),
                    Color(0xFFC7D2FE).copy(alpha = intensity * 0.4f),
                    Color.Transparent
                ),
                center = Offset(w * 0.45f, h * 0.15f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.45f, h * 0.15f)
        )
    }
}

/**
 * Falling snowflakes with horizontal drift and soft crystal appearance.
 */
private fun DrawScope.drawRealisticSnow(
    snowParticles: List<RainParticle>,
    progress: Float,
    isDarkTheme: Boolean,
    w: Float,
    h: Float
) {
    val snowColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color.White

    snowParticles.forEach { particle ->
        val currentY = ((particle.initialY + progress * particle.speed * 0.4f) % 1f) * h
        val sway = sin(currentY * 0.05f + particle.initialX * 10f) * 15f
        val currentX = (particle.initialX * w + sway) % w

        drawCircle(
            color = snowColor.copy(alpha = particle.alpha * 0.8f),
            radius = particle.length * 0.18f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawMistyFogLayers(pulse: Float, isDarkTheme: Boolean, w: Float, h: Float) {
    val fogColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFECEFF1)
    for (i in 1..3) {
        val y = h * (0.25f + i * 0.20f)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    fogColor.copy(alpha = 0.12f * pulse),
                    Color.Transparent
                ),
                startY = y - 50f,
                endY = y + 50f
            ),
            topLeft = Offset(0f, y - 50f),
            size = Size(w, 100f)
        )
    }
}

private fun DrawScope.drawGroundMist(w: Float, h: Float, isDarkTheme: Boolean) {
    val mistColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFE2E8F0)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                mistColor.copy(alpha = 0.25f)
            ),
            startY = h * 0.75f,
            endY = h
        ),
        topLeft = Offset(0f, h * 0.75f),
        size = Size(w, h * 0.25f)
    )
}

private fun DrawScope.drawWindCurrents(progress: Float, isDarkTheme: Boolean, w: Float, h: Float) {
    val windColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    for (i in 0..4) {
        val y = h * (0.15f + i * 0.16f)
        val startX = ((i * 0.2f + progress * 0.8f) % 1.4f - 0.2f) * w
        drawLine(
            color = windColor.copy(alpha = 0.22f),
            start = Offset(startX, y),
            end = Offset(startX + w * 0.35f, y + sin(startX * 0.02f) * 8f),
            strokeWidth = 1.8f
        )
    }
}
