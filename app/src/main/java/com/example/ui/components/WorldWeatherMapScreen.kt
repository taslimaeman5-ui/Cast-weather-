package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TemperatureUnit
import com.example.model.WeatherCondition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MapProjectionMode {
    GLOBE_3D,
    FLAT_MAP
}

enum class MapWeatherLayer(val label: String, val icon: String) {
    TEMPERATURE("Temperature", "°C"),
    PRECIPITATION("Precipitation", "🌧"),
    WIND("Wind", "💨"),
    SATELLITE("Terrain", "🛰")
}

data class DetailedWorldCity(
    val name: String,
    val country: String,
    val continent: String,
    val latitude: Double,
    val longitude: Double,
    val tempC: Double,
    val condition: WeatherCondition,
    val humidity: Int,
    val windKmh: Double,
    val pressureHpa: Int = 1013,
    val uvIndex: Int = 5,
    val isCapital: Boolean = false
)

val ExtendedWorldCities = listOf(
    DetailedWorldCity("Tokyo", "Japan", "Asia", 35.6762, 139.6503, 22.0, WeatherCondition.SUNNY, 58, 12.0, 1015, 6, true),
    DetailedWorldCity("London", "UK", "Europe", 51.5074, -0.1278, 16.0, WeatherCondition.DRIZZLE, 78, 18.0, 1012, 3, true),
    DetailedWorldCity("New York", "USA", "North America", 40.7128, -74.0060, 19.0, WeatherCondition.PARTLY_CLOUDY, 62, 15.0, 1018, 5),
    DetailedWorldCity("Paris", "France", "Europe", 48.8566, 2.3522, 18.0, WeatherCondition.MAINLY_CLEAR, 64, 11.0, 1016, 4, true),
    DetailedWorldCity("Sydney", "Australia", "Oceania", -33.8688, 151.2093, 21.0, WeatherCondition.CLEAR_SKY, 55, 14.0, 1020, 7),
    DetailedWorldCity("Cairo", "Egypt", "Africa", 30.0444, 31.2357, 31.0, WeatherCondition.SUNNY, 38, 20.0, 1010, 9, true),
    DetailedWorldCity("Dubai", "UAE", "Asia", 25.2048, 55.2708, 33.0, WeatherCondition.SUNNY, 42, 16.0, 1009, 10),
    DetailedWorldCity("Singapore", "Singapore", "Asia", 1.3521, 103.8198, 30.0, WeatherCondition.THUNDERSTORM, 82, 9.0, 1008, 8, true),
    DetailedWorldCity("Rio de Janeiro", "Brazil", "South America", -22.9068, -43.1729, 27.0, WeatherCondition.PARTLY_CLOUDY, 70, 13.0, 1014, 8),
    DetailedWorldCity("San Francisco", "USA", "North America", 37.7749, -122.4194, 17.0, WeatherCondition.FOGGY, 75, 22.0, 1017, 5),
    DetailedWorldCity("Seoul", "South Korea", "Asia", 37.5665, 126.9780, 20.0, WeatherCondition.PARTLY_CLOUDY, 54, 10.0, 1019, 6, true),
    DetailedWorldCity("Reykjavik", "Iceland", "Europe", 64.1466, -21.9426, 7.0, WeatherCondition.WINDY, 80, 32.0, 998, 1, true),
    DetailedWorldCity("New Delhi", "India", "Asia", 28.6139, 77.2090, 32.0, WeatherCondition.SUNNY, 48, 14.0, 1011, 9, true),
    DetailedWorldCity("Mumbai", "India", "Asia", 19.0760, 72.8777, 31.0, WeatherCondition.PARTLY_CLOUDY, 72, 17.0, 1012, 8),
    DetailedWorldCity("Beijing", "China", "Asia", 39.9042, 116.4074, 21.0, WeatherCondition.CLEAR_SKY, 45, 13.0, 1022, 6, true),
    DetailedWorldCity("Shanghai", "China", "Asia", 31.2304, 121.4737, 23.0, WeatherCondition.OVERCAST, 65, 16.0, 1017, 5),
    DetailedWorldCity("Moscow", "Russia", "Europe", 55.7558, 37.6173, 12.0, WeatherCondition.OVERCAST, 72, 15.0, 1015, 3, true),
    DetailedWorldCity("Cape Town", "South Africa", "Africa", -33.9249, 18.4241, 20.0, WeatherCondition.WINDY, 60, 26.0, 1021, 6),
    DetailedWorldCity("Nairobi", "Kenya", "Africa", -1.2921, 36.8219, 24.0, WeatherCondition.MAINLY_CLEAR, 52, 12.0, 1014, 8, true),
    DetailedWorldCity("Buenos Aires", "Argentina", "South America", -34.6037, -58.3816, 21.0, WeatherCondition.MAINLY_CLEAR, 61, 14.0, 1018, 5, true),
    DetailedWorldCity("Toronto", "Canada", "North America", 43.6532, -79.3832, 15.0, WeatherCondition.PARTLY_CLOUDY, 66, 19.0, 1016, 4),
    DetailedWorldCity("Mexico City", "Mexico", "North America", 19.4326, -99.1332, 23.0, WeatherCondition.PARTLY_CLOUDY, 50, 11.0, 1020, 8, true),
    DetailedWorldCity("Bangkok", "Thailand", "Asia", 13.7563, 100.5018, 33.0, WeatherCondition.RAIN, 75, 10.0, 1009, 8, true),
    DetailedWorldCity("Berlin", "Germany", "Europe", 52.5200, 13.4050, 17.0, WeatherCondition.PARTLY_CLOUDY, 62, 14.0, 1017, 4, true),
    DetailedWorldCity("Rome", "Italy", "Europe", 41.9028, 12.4964, 23.0, WeatherCondition.SUNNY, 56, 10.0, 1018, 6, true)
)

val ContinentGeometries: List<List<Pair<Double, Double>>> = listOf(
    // North America
    listOf(
        71.0 to -156.0, 68.0 to -135.0, 60.0 to -90.0, 50.0 to -60.0, 44.0 to -64.0,
        30.0 to -81.0, 25.0 to -80.0, 20.0 to -87.0, 15.0 to -88.0, 9.0 to -79.0,
        15.0 to -93.0, 20.0 to -105.0, 32.0 to -117.0, 40.0 to -124.0, 50.0 to -127.0,
        60.0 to -145.0, 65.0 to -168.0, 71.0 to -156.0
    ),
    // South America
    listOf(
        11.0 to -75.0, 6.0 to -55.0, -5.0 to -35.0, -15.0 to -39.0, -23.0 to -42.0,
        -35.0 to -55.0, -50.0 to -68.0, -55.0 to -67.0, -45.0 to -75.0, -30.0 to -71.0,
        -15.0 to -75.0, -5.0 to -80.0, 8.0 to -77.0, 11.0 to -75.0
    ),
    // Europe
    listOf(
        71.0 to 25.0, 70.0 to 30.0, 60.0 to 30.0, 55.0 to 20.0, 54.0 to 14.0,
        45.0 to 13.0, 36.0 to -5.0, 43.0 to -9.0, 50.0 to -5.0, 58.0 to 6.0,
        63.0 to 10.0, 70.0 to 18.0, 71.0 to 25.0
    ),
    // Africa
    listOf(
        35.0 to -5.0, 37.0 to 10.0, 32.0 to 32.0, 22.0 to 37.0, 12.0 to 44.0,
        -5.0 to 40.0, -25.0 to 35.0, -34.0 to 26.0, -34.0 to 18.0, -20.0 to 12.0,
        -5.0 to 9.0, 5.0 to 2.0, 5.0 to -10.0, 15.0 to -17.0, 28.0 to -13.0,
        35.0 to -5.0
    ),
    // Asia & Northern Eurasia
    listOf(
        75.0 to 40.0, 73.0 to 80.0, 70.0 to 130.0, 65.0 to 170.0, 60.0 to 160.0,
        50.0 to 140.0, 40.0 to 130.0, 30.0 to 122.0, 20.0 to 110.0, 10.0 to 105.0,
        1.0 to 104.0, 8.0 to 77.0, 22.0 to 69.0, 25.0 to 55.0, 15.0 to 45.0,
        30.0 to 35.0, 40.0 to 30.0, 55.0 to 40.0, 60.0 to 60.0, 70.0 to 40.0,
        75.0 to 40.0
    ),
    // Australia
    listOf(
        -12.0 to 132.0, -11.0 to 142.0, -20.0 to 149.0, -30.0 to 153.0, -38.0 to 145.0,
        -35.0 to 117.0, -22.0 to 114.0, -16.0 to 123.0, -12.0 to 132.0
    ),
    // Greenland
    listOf(
        83.0 to -30.0, 81.0 to -18.0, 76.0 to -20.0, 70.0 to -22.0, 60.0 to -43.0,
        65.0 to -53.0, 72.0 to -55.0, 78.0 to -70.0, 82.0 to -60.0, 83.0 to -30.0
    ),
    // United Kingdom & Ireland
    listOf(
        58.5 to -3.0, 56.0 to -2.0, 53.5 to 0.0, 51.0 to 1.5, 50.0 to -5.0,
        52.0 to -5.5, 55.0 to -6.0, 58.5 to -3.0
    ),
    // Japan
    listOf(
        45.0 to 142.0, 43.0 to 145.5, 38.0 to 142.0, 35.0 to 140.0, 31.0 to 131.0,
        33.5 to 130.0, 37.0 to 137.0, 41.5 to 140.5, 45.0 to 142.0
    ),
    // Madagascar
    listOf(
        -12.0 to 49.0, -16.0 to 50.0, -25.0 to 47.0, -25.5 to 45.0, -20.0 to 44.0,
        -13.5 to 48.0, -12.0 to 49.0
    ),
    // New Zealand
    listOf(
        -35.0 to 173.0, -38.0 to 178.0, -41.5 to 175.0, -46.5 to 169.0, -44.0 to 168.0,
        -41.0 to 172.0, -35.0 to 173.0
    ),
    // Antarctica (Ice Shelf outline)
    listOf(
        -70.0 to -180.0, -65.0 to -90.0, -68.0 to 0.0, -65.0 to 90.0, -70.0 to 180.0,
        -85.0 to 180.0, -85.0 to -180.0, -70.0 to -180.0
    )
)

/**
 * 3D Globe & Interactive Google Maps–Style World Weather Explorer
 */
@Composable
fun WorldWeatherMapScreen(
    unit: TemperatureUnit,
    onCitySelected: (String, String, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Projection Mode: 3D Globe vs Detailed Flat Google Maps style
    var projectionMode by remember { mutableStateOf(MapProjectionMode.GLOBE_3D) }
    var activeWeatherLayer by remember { mutableStateOf(MapWeatherLayer.TEMPERATURE) }
    var showLayersMenu by remember { mutableStateOf(false) }

    // Refresh telemetry animation state
    var isMapRefreshing by remember { mutableStateOf(false) }
    val refreshRotation by animateFloatAsState(
        targetValue = if (isMapRefreshing) 360f else 0f,
        animationSpec = tween(700, easing = LinearEasing),
        label = "map_refresh_rot"
    )

    // Momentum physics & velocity tracking for smooth 3D globe fling
    var dragVelocityX by remember { mutableFloatStateOf(0f) }
    var dragVelocityY by remember { mutableFloatStateOf(0f) }
    var lastDragTime by remember { mutableLongStateOf(0L) }
    var inertiaJob by remember { mutableStateOf<Job?>(null) }

    // Globe Camera Angles with smooth spring/tween animations
    val globeRotationX = remember { Animatable(20f) }   // Latitude tilt (-85f..85f)
    val globeRotationY = remember { Animatable(100f) }  // Longitude spin (0..360f)
    val mapZoom = remember { Animatable(1.0f) }         // Zoom multiplier (0.6f..4.0f)

    // Flat Map Pan Offset with Animatable for smooth panning
    val flatPanX = remember { Animatable(0f) }
    val flatPanY = remember { Animatable(0f) }

    // Idle rotation animation state
    var isAutoRotateEnabled by remember { mutableStateOf(false) }
    var isUserInteracting by remember { mutableStateOf(false) }

    // Pulsing animation for selected city pin & radar waves
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Auto-spin 3D globe gently when enabled and not currently dragging
    LaunchedEffect(isAutoRotateEnabled, isUserInteracting, projectionMode) {
        if (isAutoRotateEnabled && !isUserInteracting && projectionMode == MapProjectionMode.GLOBE_3D) {
            while (isActive) {
                val nextY = (globeRotationY.value + 0.35f) % 360f
                globeRotationY.snapTo(nextY)
                kotlinx.coroutines.delay(16)
            }
        }
    }

    // Search and Selected City
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<DetailedWorldCity?>(ExtendedWorldCities.first()) }
    var showCityBottomSheet by remember { mutableStateOf(true) }

    // Filter cities based on search
    val filteredSearchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else ExtendedWorldCities.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    // Camera animation fly-to city
    fun flyToCity(city: DetailedWorldCity) {
        selectedCity = city
        showCityBottomSheet = true
        coroutineScope.launch {
            if (projectionMode == MapProjectionMode.GLOBE_3D) {
                val targetRotY = (city.longitude.toFloat() + 180f) % 360f
                val targetRotX = city.latitude.toFloat().coerceIn(-60f, 60f)
                globeRotationY.animateTo(targetRotY, animationSpec = tween(650, easing = FastOutSlowInEasing))
                globeRotationX.animateTo(targetRotX, animationSpec = tween(650, easing = FastOutSlowInEasing))
            } else {
                mapZoom.animateTo(2.2f, animationSpec = tween(550, easing = FastOutSlowInEasing))
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val globeCenter = Offset(widthPx / 2f, heightPx / 2.2f)
        val globeRadius = (minOf(widthPx, heightPx) * 0.40f) * mapZoom.value

        // Transform gestures state (pinch to zoom)
        val transformableState = rememberTransformableState { zoomChange, _, _ ->
            coroutineScope.launch {
                val newZoom = (mapZoom.value * zoomChange).coerceIn(0.6f, 3.8f)
                mapZoom.snapTo(newZoom)
            }
        }

        // Tap on map detection to select city or dismiss panel
        val tapModifier = Modifier.pointerInput(projectionMode, mapZoom.value, globeRotationX.value, globeRotationY.value) {
            detectTapGestures { tapOffset ->
                var nearestCity: DetailedWorldCity? = null
                var minDistance = 75f // Touch target radius threshold in px

                if (projectionMode == MapProjectionMode.GLOBE_3D) {
                    ExtendedWorldCities.forEach { city ->
                        val pos = projectLatLonTo3DGlobe(
                            lat = city.latitude,
                            lon = city.longitude,
                            rotX = globeRotationX.value,
                            rotY = globeRotationY.value,
                            center = globeCenter,
                            radius = globeRadius
                        )
                        if (pos != null) {
                            val dx = pos.first.x - tapOffset.x
                            val dy = pos.first.y - tapOffset.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < minDistance) {
                                minDistance = dist
                                nearestCity = city
                            }
                        }
                    }
                } else {
                    ExtendedWorldCities.forEach { city ->
                        val pos = projectLatLonToFlatMap(
                            lat = city.latitude,
                            lon = city.longitude,
                            width = widthPx,
                            height = heightPx,
                            panX = flatPanX.value,
                            panY = flatPanY.value,
                            zoom = mapZoom.value
                        )
                        val dx = pos.x - tapOffset.x
                        val dy = pos.y - tapOffset.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < minDistance) {
                            minDistance = dist
                            nearestCity = city
                        }
                    }
                }

                if (nearestCity != null) {
                    flyToCity(nearestCity!!)
                } else {
                    // Tapped outside city pins - keep or toggle sheet
                }
            }
        }

        // Gesture Drag handler for spinning the 3D globe or panning the flat map with momentum inertia
        val dragModifier = Modifier
            .fillMaxSize()
            .pointerInput(projectionMode) {
                detectDragGestures(
                    onDragStart = {
                        isUserInteracting = true
                        inertiaJob?.cancel()
                        dragVelocityX = 0f
                        dragVelocityY = 0f
                        lastDragTime = System.currentTimeMillis()
                    },
                    onDragEnd = {
                        isUserInteracting = false
                        inertiaJob = coroutineScope.launch {
                            var vx = dragVelocityX.coerceIn(-35f, 35f)
                            var vy = dragVelocityY.coerceIn(-25f, 25f)
                            val damping = 0.93f
                            while (kotlin.math.abs(vx) > 0.05f || kotlin.math.abs(vy) > 0.05f) {
                                if (projectionMode == MapProjectionMode.GLOBE_3D) {
                                    val newY = (globeRotationY.value - vx * 0.40f) % 360f
                                    val newX = (globeRotationX.value + vy * 0.40f).coerceIn(-80f, 80f)
                                    globeRotationY.snapTo(if (newY < 0) newY + 360f else newY)
                                    globeRotationX.snapTo(newX)
                                } else {
                                    flatPanX.snapTo(flatPanX.value + vx)
                                    flatPanY.snapTo(flatPanY.value + vy)
                                }
                                vx *= damping
                                vy *= damping
                                delay(16)
                            }
                        }
                    },
                    onDragCancel = {
                        isUserInteracting = false
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val now = System.currentTimeMillis()
                    val dt = (now - lastDragTime).coerceAtLeast(1L).toFloat()
                    lastDragTime = now
                    val instVx = (dragAmount.x / dt) * 16f
                    val instVy = (dragAmount.y / dt) * 16f
                    dragVelocityX = dragVelocityX * 0.3f + instVx * 0.7f
                    dragVelocityY = dragVelocityY * 0.3f + instVy * 0.7f

                    coroutineScope.launch {
                        if (projectionMode == MapProjectionMode.GLOBE_3D) {
                            val newY = (globeRotationY.value - dragAmount.x * 0.45f) % 360f
                            val newX = (globeRotationX.value + dragAmount.y * 0.45f).coerceIn(-80f, 80f)
                            globeRotationY.snapTo(if (newY < 0) newY + 360f else newY)
                            globeRotationX.snapTo(newX)
                        } else {
                            flatPanX.snapTo(flatPanX.value + dragAmount.x)
                            flatPanY.snapTo(flatPanY.value + dragAmount.y)
                        }
                    }
                }
            }
            .transformable(state = transformableState)

        // 1. Interactive Canvas: Renders Realistic 3D Earth Globe or Google Maps flat projection
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(tapModifier)
                .then(dragModifier)
                .testTag("interactive_world_canvas")
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background space / night dark gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712), // Deep space black
                        Color(0xFF0B1329),
                        Color(0xFF101B3B)
                    )
                )
            )

            // Draw decorative stars in space
            drawStarsBackground(canvasWidth, canvasHeight)

            if (projectionMode == MapProjectionMode.GLOBE_3D) {
                // --- 3D EARTH GLOBE RENDERING ---
                draw3DGlobe(
                    center = globeCenter,
                    radius = globeRadius,
                    rotX = globeRotationX.value,
                    rotY = globeRotationY.value,
                    layer = activeWeatherLayer
                )

                // Draw cities plotted on 3D globe surface
                ExtendedWorldCities.forEach { city ->
                    val pos3D = projectLatLonTo3DGlobe(
                        lat = city.latitude,
                        lon = city.longitude,
                        rotX = globeRotationX.value,
                        rotY = globeRotationY.value,
                        center = globeCenter,
                        radius = globeRadius
                    )

                    // Only draw visible cities (on front side of globe, z > 0)
                    if (pos3D != null) {
                        drawCityMarker(
                            screenPos = pos3D.first,
                            city = city,
                            isSelected = selectedCity?.name == city.name,
                            layer = activeWeatherLayer,
                            unit = unit,
                            pulseScale = pulseScale,
                            pulseAlpha = pulseAlpha
                        )
                    }
                }
            } else {
                // --- DETAILED FLAT MAP RENDERING (Google Maps Style) ---
                drawFlatMap(
                    width = canvasWidth,
                    height = canvasHeight,
                    panX = flatPanX.value,
                    panY = flatPanY.value,
                    zoom = mapZoom.value,
                    layer = activeWeatherLayer
                )

                // Draw cities on flat projection
                ExtendedWorldCities.forEach { city ->
                    val flatPos = projectLatLonToFlatMap(
                        lat = city.latitude,
                        lon = city.longitude,
                        width = canvasWidth,
                        height = canvasHeight,
                        panX = flatPanX.value,
                        panY = flatPanY.value,
                        zoom = mapZoom.value
                    )

                    drawCityMarker(
                        screenPos = flatPos,
                        city = city,
                        isSelected = selectedCity?.name == city.name,
                        layer = activeWeatherLayer,
                        unit = unit,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha
                    )
                }
            }
        }

        // 2. Top App Controls (Google Maps Search Bar, Category Chips, 3D/2D Toggle, Layers)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Google Maps Search Bar
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map_search_bar")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search world",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search city, country or place...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 2D / 3D Toggle Pill Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (projectionMode == MapProjectionMode.GLOBE_3D) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clickable {
                                projectionMode = if (projectionMode == MapProjectionMode.GLOBE_3D) MapProjectionMode.FLAT_MAP else MapProjectionMode.GLOBE_3D
                            }
                            .testTag("toggle_3d_globe_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (projectionMode == MapProjectionMode.GLOBE_3D) Icons.Default.Public else Icons.Default.Map,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (projectionMode == MapProjectionMode.GLOBE_3D) "3D Globe" else "2D Map",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Search autocomplete results drop panel
            if (filteredSearchResults.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        filteredSearchResults.take(5).forEach { city ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = ""
                                        flyToCity(city)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${city.name}, ${city.country}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${city.condition.displayName} • ${unit.format(city.tempC)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Fly to city",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        val capital = ExtendedWorldCities.firstOrNull { it.isCapital }
                        capital?.let { flyToCity(it) }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🏛 Capitals", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        val warmest = ExtendedWorldCities.maxByOrNull { it.tempC }
                        warmest?.let { flyToCity(it) }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🔥 Warmest Spot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        val rainy = ExtendedWorldCities.firstOrNull { it.condition == WeatherCondition.THUNDERSTORM || it.condition == WeatherCondition.DRIZZLE }
                        rainy?.let { flyToCity(it) }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🌧 Rain Radar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        val tokyo = ExtendedWorldCities.firstOrNull { it.name == "Tokyo" }
                        tokyo?.let { flyToCity(it) }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🌏 Asia", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable {
                        val london = ExtendedWorldCities.firstOrNull { it.name == "London" }
                        london?.let { flyToCity(it) }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("🌍 Europe", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Realistic Cartography Color Legend Pill (Green = Land, Blue = Ocean)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier.testTag("map_cartography_legend")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Green: Land",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E40AF))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Blue: Water",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. Floating Action Controls (Right Edge: Layers, Refresh, Auto-Rotate, Zoom In, Zoom Out, Reset Center)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weather Layers Selector
            Box {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { showLayersMenu = !showLayersMenu }
                        .testTag("map_layers_fab")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Weather layers",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showLayersMenu,
                    onDismissRequest = { showLayersMenu = false }
                ) {
                    MapWeatherLayer.values().forEach { layer ->
                        DropdownMenuItem(
                            text = { Text("${layer.icon} ${layer.label}") },
                            onClick = {
                                activeWeatherLayer = layer
                                showLayersMenu = false
                            },
                            trailingIcon = {
                                if (activeWeatherLayer == layer) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Refresh World Weather Telemetry Button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        if (!isMapRefreshing) {
                            coroutineScope.launch {
                                isMapRefreshing = true
                                delay(750)
                                isMapRefreshing = false
                                Toast.makeText(context, "World weather map refreshed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .testTag("map_refresh_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh world telemetry",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(refreshRotation)
                    )
                }
            }

            // Auto-Rotate Earth Button (3D Globe Mode only)
            if (projectionMode == MapProjectionMode.GLOBE_3D) {
                Surface(
                    shape = CircleShape,
                    color = if (isAutoRotateEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { isAutoRotateEnabled = !isAutoRotateEnabled }
                        .testTag("map_auto_rotate_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Auto Rotate Globe",
                            tint = if (isAutoRotateEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Zoom In Button (+)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        coroutineScope.launch {
                            val newZoom = (mapZoom.value + 0.35f).coerceAtMost(3.8f)
                            mapZoom.animateTo(newZoom, animationSpec = tween(300, easing = FastOutSlowInEasing))
                        }
                    }
                    .testTag("map_zoom_in_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Zoom Out Button (-)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        coroutineScope.launch {
                            val newZoom = (mapZoom.value - 0.35f).coerceAtLeast(0.6f)
                            mapZoom.animateTo(newZoom, animationSpec = tween(300, easing = FastOutSlowInEasing))
                        }
                    }
                    .testTag("map_zoom_out_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Reset Rotation / Center on Earth
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        coroutineScope.launch {
                            globeRotationX.animateTo(20f, animationSpec = tween(500))
                            globeRotationY.animateTo(100f, animationSpec = tween(500))
                            mapZoom.animateTo(1.0f, animationSpec = tween(500))
                            flatPanX.animateTo(0f, animationSpec = tween(500))
                            flatPanY.animateTo(0f, animationSpec = tween(500))
                        }
                    }
                    .testTag("map_reset_center_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter World",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Google Maps Style City Weather Bottom Card
        AnimatedVisibility(
            visible = selectedCity != null && showCityBottomSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedCity?.let { city ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_city_weather_sheet")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WeatherIcon(
                                condition = city.condition,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${city.name}, ${city.country}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${city.condition.displayName} • ${city.continent}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = unit.format(city.tempC),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { showCityBottomSheet = false },
                                        modifier = Modifier.size(28.dp).padding(start = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss info sheet",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick telemetry pills: Humidity, Wind, Pressure, UV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            QuickStatPill("💧 Humidity", "${city.humidity}%")
                            QuickStatPill("💨 Wind", "${city.windKmh.toInt()} km/h")
                            QuickStatPill("☀️ UV Index", "${city.uvIndex}")
                            QuickStatPill("🧭 Pressure", "${city.pressureHpa} hPa")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Open Full City Forecast
                        Button(
                            onClick = {
                                onCitySelected(city.name, city.country, city.latitude, city.longitude)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("view_full_forecast_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Full Forecast & Timeline",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatPill(title: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Draws background starry space particles
 */
private fun DrawScope.drawStarsBackground(width: Float, height: Float) {
    val randomSeeds = listOf(
        0.12f to 0.15f, 0.28f to 0.08f, 0.45f to 0.18f, 0.62f to 0.05f, 0.85f to 0.12f,
        0.08f to 0.45f, 0.22f to 0.62f, 0.78f to 0.55f, 0.92f to 0.42f, 0.15f to 0.82f,
        0.35f to 0.88f, 0.58f to 0.85f, 0.72f to 0.78f, 0.88f to 0.92f, 0.52f to 0.35f
    )
    randomSeeds.forEachIndexed { index, (rx, ry) ->
        val starRadius = if (index % 3 == 0) 1.8f else 1.1f
        val alpha = if (index % 2 == 0) 0.6f else 0.35f
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = starRadius,
            center = Offset(rx * width, ry * height)
        )
    }
}

/**
 * 3D Earth Globe Renderer:
 * - Spherical shaded oceanic sphere with atmosphere halo
 * - Latitude / Longitude graticule grid projected onto sphere
 * - Accurate 3D continent terrain polygons rotated around 3D sphere
 * - Atmosphere glow edge
 */
private fun DrawScope.draw3DGlobe(
    center: Offset,
    radius: Float,
    rotX: Float,
    rotY: Float,
    layer: MapWeatherLayer
) {
    if (radius <= 0f) return

    // 1. Atmosphere Outer Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x4038BDF8),
                Color(0x200EA5E9),
                Color(0x080284C7),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.18f
        ),
        radius = radius * 1.18f,
        center = center
    )

    // 2. Earth Ocean Base (Spherical radial gradient with sun specular highlight in vibrant blues)
    val oceanBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1D4ED8), // Sun specular highlight in the tropics (vibrant ocean blue)
            Color(0xFF1E3A8A), // Mid-depth Atlantic/Pacific sapphire blue
            Color(0xFF0F2653), // Deep sea
            Color(0xFF071228)  // Night limb shadow
        ),
        center = Offset(center.x - radius * 0.32f, center.y - radius * 0.32f),
        radius = radius * 1.25f
    )
    drawCircle(brush = oceanBrush, radius = radius, center = center)

    // 3. Spherical Graticule (Equator, Meridians, Parallels in soft ice-cyan)
    val gridColor = Color(0x187DD3FC)
    val radX = Math.toRadians(rotX.toDouble())
    val radY = Math.toRadians(rotY.toDouble())

    // Draw Parallels (Latitudes: -60, -30, 0, 30, 60)
    for (lat in listOf(-60.0, -30.0, 0.0, 30.0, 60.0)) {
        val latRad = Math.toRadians(lat)
        val path = Path()
        var first = true

        for (lon in 0..360 step 8) {
            val lonRad = Math.toRadians(lon.toDouble())
            val pt = projectSpherical(latRad, lonRad, radX, radY, radius, center)
            if (pt != null) {
                if (first) {
                    path.moveTo(pt.x, pt.y)
                    first = false
                } else {
                    path.lineTo(pt.x, pt.y)
                }
            } else {
                first = true
            }
        }
        drawPath(path, color = gridColor, style = Stroke(width = if (lat == 0.0) 1.5f else 0.8f))
    }

    // Draw Meridians (Longitudes: 0, 60, 120, 180, 240, 300)
    for (lon in 0 until 360 step 60) {
        val lonRad = Math.toRadians(lon.toDouble())
        val path = Path()
        var first = true

        for (lat in -85..85 step 5) {
            val latRad = Math.toRadians(lat.toDouble())
            val pt = projectSpherical(latRad, lonRad, radX, radY, radius, center)
            if (pt != null) {
                if (first) {
                    path.moveTo(pt.x, pt.y)
                    first = false
                } else {
                    path.lineTo(pt.x, pt.y)
                }
            } else {
                first = true
            }
        }
        drawPath(path, color = gridColor, style = Stroke(width = 0.8f))
    }

    // 4. Draw Continents projected onto 3D sphere (Green represents Land, Blue represents Ocean)
    val continentFillColor = when (layer) {
        MapWeatherLayer.TEMPERATURE -> Color(0xFF2E7D32) // Lush emerald terrain green
        MapWeatherLayer.PRECIPITATION -> Color(0xFF1B5E20) // Deep rainforest green
        MapWeatherLayer.WIND -> Color(0xFF236932) // Forest green
        MapWeatherLayer.SATELLITE -> Color(0xFF2E7D32) // Realistic Earth green
    }
    val continentStrokeColor = Color(0xFF4ADE80).copy(alpha = 0.85f)
    val coastalShelfColor = Color(0x650284C7) // Shallow coastal water shelf azure

    ContinentGeometries.forEach { poly ->
        val path = Path()
        var started = false

        poly.forEach { (lat, lon) ->
            val latRad = Math.toRadians(lat)
            val lonRad = Math.toRadians(lon)
            val pt = projectSpherical(latRad, lonRad, radX, radY, radius, center)

            if (pt != null) {
                if (!started) {
                    path.moveTo(pt.x, pt.y)
                    started = true
                } else {
                    path.lineTo(pt.x, pt.y)
                }
            }
        }

        if (started) {
            val isPolar = poly.any { it.first < -60.0 }
            val polyFill = if (isPolar) Color(0xFFE0F2FE) else continentFillColor
            val polyBorder = if (isPolar) Color(0xFFBAE6FD) else continentStrokeColor

            // 1. Shallow turquoise/azure coastal shelf glow around land
            if (!isPolar) {
                drawPath(path = path, color = coastalShelfColor, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
            }
            // 2. Realistic Land Fill (Green for continents, Glacial pale cyan for Antarctica)
            drawPath(path = path, color = polyFill)
            // 3. Clear crisp land contour edge
            drawPath(path = path, color = polyBorder, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
        }
    }

    // 5. Atmosphere Rim / Fresnel Edge Lighting
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x1038BDF8),
                Color(0x3538BDF8),
                Color(0x7538BDF8)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // 6. Globe border outline
    drawCircle(
        color = Color(0x8060A5FA),
        radius = radius,
        center = center,
        style = Stroke(width = 1.6f)
    )
}

/**
 * Detailed Flat Map Renderer (Google Maps style Mercator projection)
 */
private fun DrawScope.drawFlatMap(
    width: Float,
    height: Float,
    panX: Float,
    panY: Float,
    zoom: Float,
    layer: MapWeatherLayer
) {
    // Ocean: Rich multi-stop vibrant oceanic sapphire blue
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF091B36), // Cold ocean waters
                Color(0xFF0F3562), // Equatorial deep sapphire blue
                Color(0xFF091B36)
            )
        )
    )

    // Map Grid Lines
    val gridColor = Color(0x1860A5FA)
    val stepX = (width / 6f) * zoom
    val stepY = (height / 6f) * zoom

    var x = (panX % stepX)
    while (x < width) {
        drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
        x += stepX
    }

    var y = (panY % stepY)
    while (y < height) {
        drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
        y += stepY
    }

    // Flat Continent Polygons (Green for Land, Glacial Ice for Antarctica)
    ContinentGeometries.forEach { poly ->
        val path = Path()
        var first = true

        poly.forEach { (lat, lon) ->
            val pt = projectLatLonToFlatMap(lat, lon, width, height, panX, panY, zoom)
            if (first) {
                path.moveTo(pt.x, pt.y)
                first = false
            } else {
                path.lineTo(pt.x, pt.y)
            }
        }

        val isPolar = poly.any { it.first < -60.0 }
        val polyColor = if (isPolar) Color(0xFFE0F2FE) else Color(0xFF2E7D32) // Emerald Green Land!
        val borderColor = if (isPolar) Color(0xFFBAE6FD) else Color(0xFF4ADE80).copy(alpha = 0.85f)

        // Coastal shelf around land
        if (!isPolar) {
            drawPath(path, color = Color(0x600284C7), style = Stroke(width = 5.0f, cap = StrokeCap.Round))
        }
        drawPath(path, color = polyColor)
        drawPath(path, color = borderColor, style = Stroke(width = 1.4f))
    }
}

/**
 * Draw Interactive City Marker Pin on map/globe with radar pulse animation
 */
private fun DrawScope.drawCityMarker(
    screenPos: Offset,
    city: DetailedWorldCity,
    isSelected: Boolean,
    layer: MapWeatherLayer,
    unit: TemperatureUnit,
    pulseScale: Float = 1.0f,
    pulseAlpha: Float = 0.5f
) {
    val pinColor = when (layer) {
        MapWeatherLayer.TEMPERATURE -> {
            if (city.tempC >= 28.0) Color(0xFFEF4444)
            else if (city.tempC >= 18.0) Color(0xFFF59E0B)
            else if (city.tempC >= 10.0) Color(0xFF10B981)
            else Color(0xFF06B6D4)
        }
        MapWeatherLayer.PRECIPITATION -> {
            if (city.humidity >= 70) Color(0xFF3B82F6) else Color(0xFF94A3B8)
        }
        MapWeatherLayer.WIND -> {
            if (city.windKmh >= 20) Color(0xFFA855F7) else Color(0xFF38BDF8)
        }
        MapWeatherLayer.SATELLITE -> Color(0xFFFBBF24)
    }

    // Animated Radar Pulse Ring for selected city
    if (isSelected) {
        drawCircle(
            color = pinColor.copy(alpha = pulseAlpha),
            radius = 12f * pulseScale,
            center = screenPos
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = 16f,
            center = screenPos
        )
    }

    // Outer Pin Circle
    drawCircle(
        color = Color.White,
        radius = if (isSelected) 8.5f else 6.5f,
        center = screenPos
    )

    // Inner Color Dot
    drawCircle(
        color = pinColor,
        radius = if (isSelected) 6.5f else 4.5f,
        center = screenPos
    )
}

/**
 * Projects Lat / Lon to 3D Globe screen point
 * Returns Pair(Offset, z) or null if point is on the back side of Earth
 */
private fun projectLatLonTo3DGlobe(
    lat: Double,
    lon: Double,
    rotX: Float,
    rotY: Float,
    center: Offset,
    radius: Float
): Pair<Offset, Double>? {
    val latRad = Math.toRadians(lat)
    val lonRad = Math.toRadians(lon)
    val radX = Math.toRadians(rotX.toDouble())
    val radY = Math.toRadians(rotY.toDouble())

    // 3D sphere coordinate
    val x0 = cos(latRad) * sin(lonRad)
    val y0 = -sin(latRad)
    val z0 = cos(latRad) * cos(lonRad)

    // Rotate around Y axis (longitude spinning)
    val x1 = x0 * cos(radY) + z0 * sin(radY)
    val y1 = y0
    val z1 = -x0 * sin(radY) + z0 * cos(radY)

    // Rotate around X axis (latitude tilt)
    val x2 = x1
    val y2 = y1 * cos(radX) - z1 * sin(radX)
    val z2 = y1 * sin(radX) + z1 * cos(radX)

    // Back-face culling: only return if facing camera (z2 > 0)
    if (z2 <= 0.05) return null

    val screenX = center.x + (x2 * radius).toFloat()
    val screenY = center.y + (y2 * radius).toFloat()
    return Pair(Offset(screenX, screenY), z2)
}

private fun projectSpherical(
    latRad: Double,
    lonRad: Double,
    radX: Double,
    radY: Double,
    radius: Float,
    center: Offset
): Offset? {
    val x0 = cos(latRad) * sin(lonRad)
    val y0 = -sin(latRad)
    val z0 = cos(latRad) * cos(lonRad)

    val x1 = x0 * cos(radY) + z0 * sin(radY)
    val y1 = y0
    val z1 = -x0 * sin(radY) + z0 * cos(radY)

    val x2 = x1
    val y2 = y1 * cos(radX) - z1 * sin(radX)
    val z2 = y1 * sin(radX) + z1 * cos(radX)

    if (z2 <= 0.0) return null

    val sx = center.x + (x2 * radius).toFloat()
    val sy = center.y + (y2 * radius).toFloat()
    return Offset(sx, sy)
}

/**
 * Projects Lat / Lon to Flat 2D Map (Mercator style)
 */
private fun projectLatLonToFlatMap(
    lat: Double,
    lon: Double,
    width: Float,
    height: Float,
    panX: Float,
    panY: Float,
    zoom: Float
): Offset {
    val normX = ((lon + 180.0) / 360.0).toFloat()
    val normY = ((90.0 - lat) / 180.0).toFloat()

    val sx = (normX * width * zoom) + panX
    val sy = (normY * height * zoom) + panY
    return Offset(sx, sy)
}
