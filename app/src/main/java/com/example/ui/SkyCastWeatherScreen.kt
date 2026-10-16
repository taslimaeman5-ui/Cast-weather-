package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.location.LocationProvider
import com.example.location.LocationResultState
import com.example.model.WeatherCondition
import com.example.model.WeatherNavTab
import com.example.preferences.AppThemeMode
import com.example.ui.components.CitySearchBar
import com.example.ui.components.CurrentWeatherHeroCard
import com.example.ui.components.DailyForecastSection
import com.example.ui.components.DynamicWeatherBackground
import com.example.ui.components.GeminiSearchCategoryScreen
import com.example.ui.components.GeminiWeatherSearchBar
import com.example.ui.components.HiddenAppsVaultDialog
import com.example.ui.components.HourlyForecastSection
import com.example.ui.components.LocationPermissionCard
import com.example.ui.components.LocationPermissionRationaleDialog
import com.example.ui.components.LocationServicesDisabledDialog
import com.example.ui.components.RecentSearchesSection
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.SmoothWeatherRefreshHeader
import com.example.ui.components.TemperatureUnitToggle
import com.example.ui.components.WeatherAlertsSection
import com.example.ui.components.WeatherDetailsGrid
import com.example.ui.components.WorldWeatherMapScreen
import com.example.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyCastWeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationProvider = LocationProvider(context)
    val pullToRefreshState = rememberPullToRefreshState()

    // Handle back button when on Map or Gemini tab
    BackHandler(enabled = state.currentTab != WeatherNavTab.HOME) {
        viewModel.setNavTab(WeatherNavTab.HOME)
    }

    val isDark = when (state.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    var showPermissionRationale by remember { mutableStateOf(false) }
    var showLocationServicesDisabledDialog by remember { mutableStateOf(false) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showHiddenAppsVaultDialog by remember { mutableStateOf(false) }

    fun checkHasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    var hasPermission by remember { mutableStateOf(checkHasLocationPermission()) }

    fun requestGpsLocation() {
        if (state.isGpsLocating) return // Prevent duplicate requests
        viewModel.setGpsLocating(true)
        coroutineScope.launch {
            when (val result = locationProvider.getAccurateCurrentCoordinates()) {
                is LocationResultState.Success -> {
                    viewModel.loadWeatherFromGpsCoordinates(
                        result.coordinates.latitude,
                        result.coordinates.longitude
                    )
                }
                is LocationResultState.Failure.LocationServicesDisabled -> {
                    viewModel.setGpsLocating(false)
                    showLocationServicesDisabledDialog = true
                }
                is LocationResultState.Failure.PermissionDenied -> {
                    viewModel.setGpsLocating(false)
                    showPermissionRationale = true
                }
                is LocationResultState.Failure.Timeout -> {
                    viewModel.setGpsLocationError(result.userMessage)
                }
                is LocationResultState.Failure.Unavailable -> {
                    viewModel.setGpsLocationError(result.userMessage)
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            hasPermission = true
            showPermissionRationale = false
            requestGpsLocation()
        } else {
            hasPermission = false
            showPermissionRationale = true
            isPermanentlyDenied = true
            viewModel.setGpsLocating(false)
        }
    }

    fun triggerPermissionFlow() {
        if (state.isGpsLocating) return // Prevent duplicate requests
        if (checkHasLocationPermission()) {
            hasPermission = true
            requestGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-fetch GPS weather upon start if user has preferred GPS
    LaunchedEffect(state.isPreferGpsLocation) {
        if (state.isPreferGpsLocation) {
            if (checkHasLocationPermission()) {
                requestGpsLocation()
            } else {
                triggerPermissionFlow()
            }
        }
    }

    if (showPermissionRationale) {
        LocationPermissionRationaleDialog(
            isPermanentlyDenied = isPermanentlyDenied,
            onConfirm = {
                showPermissionRationale = false
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismiss = {
                showPermissionRationale = false
            }
        )
    }

    if (showLocationServicesDisabledDialog) {
        LocationServicesDisabledDialog(
            onDismiss = {
                showLocationServicesDisabledDialog = false
            }
        )
    }

    // Settings Modal Bottom Sheet (contains Location Preference, Units, Theme Mode, and Hidden Apps Vault)
    SettingsBottomSheet(
        isOpen = showSettingsSheet,
        onDismiss = { showSettingsSheet = false },
        isPreferGps = state.isPreferGpsLocation,
        onPreferGpsToggled = { prefer ->
            viewModel.setPreferGpsLocation(prefer)
            if (prefer) {
                triggerPermissionFlow()
            }
        },
        currentCity = state.weatherData?.cityName ?: "Custom City",
        temperatureUnit = state.temperatureUnit,
        onTemperatureUnitChanged = { viewModel.setTemperatureUnit(it) },
        themeMode = state.themeMode,
        onThemeModeChanged = { viewModel.setThemeMode(it) },
        vaultRepository = viewModel.hiddenAppsVaultRepository,
        onOpenHiddenAppsVault = {
            showSettingsSheet = false
            showHiddenAppsVaultDialog = true
        },
        isUserSignedIn = state.isUserSignedIn,
        userEmail = state.userEmail,
        userDisplayName = state.userDisplayName,
        isAuthenticating = state.isAuthenticating,
        onSignInWithGoogle = { viewModel.signInWithGoogle() },
        onSignOut = { viewModel.signOut() }
    )

    // Hidden Apps Vault Dialog (protected by AES-256 GCM, Face Unlock, and Fingerprint Sensor)
    HiddenAppsVaultDialog(
        isOpen = showHiddenAppsVaultDialog,
        onDismiss = { showHiddenAppsVaultDialog = false },
        vaultRepository = viewModel.hiddenAppsVaultRepository
    )

    // Outer container holding the background canvas and the interactive Scaffold
    Box(modifier = modifier.fillMaxSize()) {
        // Background canvas placed underneath
        val currentCondition = state.weatherData?.condition ?: WeatherCondition.PARTLY_CLOUDY
        DynamicWeatherBackground(
            condition = currentCondition,
            isDarkTheme = isDark,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SkyCast",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                state.weatherData?.lastUpdatedText?.let { updated ->
                                    Text(
                                        text = if (state.isUsingGpsLocation) "GPS Location • $updated" else updated,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Temperature unit quick toggle (°C / °F)
                        TemperatureUnitToggle(
                            currentUnit = state.temperatureUnit,
                            onUnitChanged = { viewModel.setTemperatureUnit(it) },
                            modifier = Modifier.padding(end = 2.dp)
                        )

                        // Quick GPS Location button
                        IconButton(
                            onClick = { triggerPermissionFlow() },
                            enabled = !state.isGpsLocating,
                            modifier = Modifier.testTag("appbar_gps_button")
                        ) {
                            if (state.isGpsLocating) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Detect current GPS location",
                                    tint = if (state.isUsingGpsLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Refresh Button
                        IconButton(
                            onClick = {
                                viewModel.refreshWeather(onGpsRefreshNeeded = { triggerPermissionFlow() })
                            },
                            modifier = Modifier.testTag("refresh_weather_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh weather",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier.testTag("appbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                // Category Bottom Navigation Bar (Home, Map, Gemini Search Bar)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = state.currentTab == WeatherNavTab.HOME,
                        onClick = { viewModel.setNavTab(WeatherNavTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (state.currentTab == WeatherNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    NavigationBarItem(
                        selected = state.currentTab == WeatherNavTab.MAP,
                        onClick = { viewModel.setNavTab(WeatherNavTab.MAP) },
                        icon = {
                            Icon(
                                imageVector = if (state.currentTab == WeatherNavTab.MAP) Icons.Filled.Language else Icons.Outlined.Language,
                                contentDescription = "World Map"
                            )
                        },
                        label = { Text("Map") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_map")
                    )

                    NavigationBarItem(
                        selected = state.currentTab == WeatherNavTab.AI_SEARCH,
                        onClick = { viewModel.setNavTab(WeatherNavTab.AI_SEARCH) },
                        icon = {
                            Icon(
                                imageVector = if (state.currentTab == WeatherNavTab.AI_SEARCH) Icons.Filled.AutoAwesome else Icons.Outlined.Psychology,
                                contentDescription = "Gemini Search"
                            )
                        },
                        label = { Text("Gemini Search") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_gemini")
                    )
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (state.currentTab) {
                    WeatherNavTab.HOME -> {
                        PullToRefreshBox(
                            isRefreshing = state.isLoading,
                            onRefresh = {
                                viewModel.refreshWeather(onGpsRefreshNeeded = { triggerPermissionFlow() })
                            },
                            state = pullToRefreshState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("main_pull_to_refresh_box")
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                        .widthIn(max = 640.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    // Smooth Weather Refresh Banner with spring motion and satellite sync confirmation
                                    SmoothWeatherRefreshHeader(
                                        isRefreshing = state.isLoading,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // If location permission not yet enabled, display polite invitation card
                                    if (!hasPermission && !state.isUsingGpsLocation) {
                                        LocationPermissionCard(
                                            onRequestPermission = { triggerPermissionFlow() },
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }

                                    // City Search Input Bar with integrated GPS action and suggestions
                                    CitySearchBar(
                                        query = state.searchQuery,
                                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                        onSearchTriggered = { viewModel.searchAndSelectFirst(it) },
                                        suggestions = state.searchSuggestions,
                                        isSearching = state.isSearching,
                                        onSuggestionSelected = { viewModel.selectCity(it) },
                                        onGpsLocationRequested = { triggerPermissionFlow() },
                                        isGpsLocating = state.isGpsLocating,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Room Database Recent Searches Section (Last 5 searched cities)
                                    if (state.recentCities.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        RecentSearchesSection(
                                            recentCities = state.recentCities,
                                            activeCityName = state.weatherData?.cityName,
                                            onCitySelected = { viewModel.selectRecentCity(it) },
                                            onClearAll = { viewModel.clearRecentCities() },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Quick City Chips row with Current Location Chip
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = state.isUsingGpsLocation,
                                            onClick = { triggerPermissionFlow() },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.MyLocation,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            label = { Text("My Location", fontSize = 13.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.testTag("my_location_chip")
                                        )

                                        state.popularCities.forEach { city ->
                                            val isSelected = !state.isUsingGpsLocation && state.weatherData?.cityName?.equals(city, ignoreCase = true) == true
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.searchAndSelectFirst(city) },
                                                label = { Text(city, fontSize = 13.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                modifier = Modifier.testTag("city_chip_$city")
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    state.weatherData?.let { data ->
                                        // Severe Weather Alerts & Warnings Section (prominent at top when present)
                                        if (data.alerts.isNotEmpty()) {
                                            WeatherAlertsSection(
                                                alerts = data.alerts,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }

                                        // Current Weather Hero Card
                                        CurrentWeatherHeroCard(
                                            weather = data,
                                            unit = state.temperatureUnit,
                                            onGpsLocationClick = { triggerPermissionFlow() },
                                            isGpsLocating = state.isGpsLocating,
                                            isUsingGpsLocation = state.isUsingGpsLocation,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Hourly Timeline
                                        HourlyForecastSection(
                                            hourlyList = data.hourlyForecasts,
                                            unit = state.temperatureUnit,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // 5-Day Forecast
                                        DailyForecastSection(
                                            dailyList = data.dailyForecasts,
                                            unit = state.temperatureUnit,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Details Grid (Humidity, Wind, Feels like, UV, etc.)
                                        Text(
                                            text = "Weather Details",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        WeatherDetailsGrid(
                                            weather = data,
                                            unit = state.temperatureUnit,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(28.dp))
                                    }
                                }
                            }
                        }
                    }

                    WeatherNavTab.MAP -> {
                        WorldWeatherMapScreen(
                            unit = state.temperatureUnit,
                            onCitySelected = { cityName, country, lat, lon ->
                                viewModel.selectCity(
                                    com.example.network.GeocodingResult(
                                        name = cityName,
                                        country = country,
                                        latitude = lat,
                                        longitude = lon
                                    )
                                )
                                viewModel.setNavTab(WeatherNavTab.HOME)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    WeatherNavTab.AI_SEARCH -> {
                        GeminiSearchCategoryScreen(
                            question = state.geminiQuestion,
                            onQuestionChanged = { viewModel.onGeminiQuestionChanged(it) },
                            onAskTriggered = { viewModel.askGeminiWeatherQuestion(it) },
                            answer = state.geminiAnswer,
                            isLoading = state.isGeminiLoading,
                            onClearAnswer = { viewModel.clearGeminiAnswer() },
                            weatherData = state.weatherData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Error banner/snackbar if any
                state.errorMessage?.let { error ->
                    Snackbar(
                        action = {
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .testTag("error_snackbar")
                    ) {
                        Text(text = error)
                    }
                }
            }
        }
    }
}
