package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiWeatherService
import com.example.data.WeatherRepository
import com.example.db.RecentCityEntity
import com.example.db.RecentCityRepository
import com.example.db.SkyCastDatabase
import com.example.model.TemperatureUnit
import com.example.model.WeatherData
import com.example.model.WeatherNavTab
import com.example.network.GeocodingResult
import com.example.preferences.AppThemeMode
import com.example.preferences.UserPreferencesManager
import com.example.security.HiddenAppsVaultRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val weatherData: WeatherData? = null,
    val currentTab: WeatherNavTab = WeatherNavTab.HOME,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isLoading: Boolean = false,
    val isGpsLocating: Boolean = false,
    val isPreferGpsLocation: Boolean = false,
    val searchQuery: String = "",
    val searchSuggestions: List<GeocodingResult> = emptyList(),
    val recentCities: List<RecentCityEntity> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val isUsingGpsLocation: Boolean = false,
    // Gemini Weather Q&A state
    val geminiQuestion: String = "",
    val geminiAnswer: String? = null,
    val isGeminiLoading: Boolean = false,
    val popularCities: List<String> = listOf("London", "New York", "Tokyo", "Paris", "Sydney", "Dubai"),
    // Firebase Cloud & Auth state
    val userEmail: String? = null,
    val userDisplayName: String? = null,
    val isUserSignedIn: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null
)

class WeatherViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val geminiService = GeminiWeatherService()
    private val preferencesManager = UserPreferencesManager(application)
    private val recentCityRepo = RecentCityRepository(SkyCastDatabase.getDatabase(application).recentCityDao())
    val hiddenAppsVaultRepository = HiddenAppsVaultRepository(application)
    val authManager = com.example.firebase.FirebaseAuthManager(application)
    val firestoreRepo = com.example.firebase.FirestoreWeatherRepository(application)

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            weatherData = repository.getInitialPreviewData(),
            themeMode = preferencesManager.themeMode,
            isPreferGpsLocation = preferencesManager.isPreferGpsLocation,
            temperatureUnit = try {
                TemperatureUnit.valueOf(preferencesManager.savedTemperatureUnit)
            } catch (_: Exception) {
                TemperatureUnit.CELSIUS
            }
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastGpsCoordinates: Pair<Double, Double>? = null

    init {
        // Observe Firebase Auth state
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        isUserSignedIn = user != null,
                        userEmail = user?.email,
                        userDisplayName = user?.displayName
                    )
                }
                if (user != null) {
                    syncPreferencesToFirestore()
                }
            }
        }

        viewModelScope.launch {
            authManager.isAuthenticating.collect { isAuth ->
                _uiState.update { it.copy(isAuthenticating = isAuth) }
            }
        }

        viewModelScope.launch {
            authManager.authError.collect { err ->
                _uiState.update { it.copy(authError = err) }
            }
        }

        // Try silent Google Sign-In on startup
        viewModelScope.launch {
            authManager.trySilentSignIn()
        }
        // Collect last 5 searched cities from Room database
        viewModelScope.launch {
            recentCityRepo.recentCities.collect { cities ->
                _uiState.update { it.copy(recentCities = cities) }
            }
        }

        // If user preference is to not prefer GPS or waiting for GPS trigger, load default city
        val initialCity = preferencesManager.defaultCity
        searchAndSelectFirst(initialCity)
    }

    fun setNavTab(tab: WeatherNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    /**
     * Ask Gemini a weather-related question using the active city's real-time forecast context.
     */
    fun askGeminiWeatherQuestion(question: String) {
        if (question.isBlank()) return
        _uiState.update { it.copy(geminiQuestion = question, isGeminiLoading = true, geminiAnswer = null) }
        viewModelScope.launch {
            val answer = geminiService.askWeatherQuestion(question.trim(), _uiState.value.weatherData)
            _uiState.update { it.copy(geminiAnswer = answer, isGeminiLoading = false) }
        }
    }

    fun onGeminiQuestionChanged(question: String) {
        _uiState.update { it.copy(geminiQuestion = question) }
    }

    fun clearGeminiAnswer() {
        _uiState.update { it.copy(geminiAnswer = null, geminiQuestion = "") }
    }

    /**
     * Cycles theme between System, Light, and Dark mode and persists to SharedPreferences.
     */
    fun toggleThemeMode() {
        val nextMode = when (_uiState.value.themeMode) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferencesManager.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    /**
     * Toggles the user's preference to prioritize GPS location as the primary weather source.
     */
    fun setPreferGpsLocation(preferGps: Boolean) {
        preferencesManager.isPreferGpsLocation = preferGps
        _uiState.update { it.copy(isPreferGpsLocation = preferGps) }
    }

    /**
     * Toggles between Celsius and Fahrenheit.
     */
    fun toggleTemperatureUnit() {
        val nextUnit = if (_uiState.value.temperatureUnit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
        setTemperatureUnit(nextUnit)
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        preferencesManager.savedTemperatureUnit = unit.name
        _uiState.update { it.copy(temperatureUnit = unit) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }
        searchJob?.cancel()
        if (query.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                delay(350) // debounce
                _uiState.update { it.copy(isSearching = true) }
                try {
                    val results = repository.searchCities(query)
                    _uiState.update { it.copy(searchSuggestions = results, isSearching = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(searchSuggestions = emptyList(), isSearching = false) }
                }
            }
        } else {
            _uiState.update { it.copy(searchSuggestions = emptyList(), isSearching = false) }
        }
    }

    fun selectCity(result: GeocodingResult) {
        val locationLabel = listOfNotNull(result.name, result.admin1).joinToString(", ")
        preferencesManager.defaultCity = result.name
        _uiState.update {
            it.copy(
                searchQuery = locationLabel,
                searchSuggestions = emptyList(),
                isSearching = false,
                isUsingGpsLocation = false
            )
        }
        fetchWeatherForCity(result.name, result.country, result.latitude, result.longitude)
    }

    fun selectRecentCity(city: RecentCityEntity) {
        preferencesManager.defaultCity = city.cityName
        _uiState.update {
            it.copy(
                searchQuery = city.cityName,
                searchSuggestions = emptyList(),
                isSearching = false,
                isUsingGpsLocation = false
            )
        }
        fetchWeatherForCity(city.cityName, city.country, city.latitude, city.longitude)
    }

    fun removeRecentCity(cityName: String) {
        viewModelScope.launch {
            recentCityRepo.removeCity(cityName)
        }
    }

    fun clearRecentCities() {
        viewModelScope.launch {
            recentCityRepo.clearHistory()
        }
    }

    fun searchAndSelectFirst(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    searchSuggestions = emptyList(),
                    isUsingGpsLocation = false
                )
            }
            try {
                val results = repository.searchCities(query.trim())
                if (results.isNotEmpty()) {
                    val first = results.first()
                    preferencesManager.defaultCity = first.name
                    val data = repository.fetchWeather(first.name, first.country, first.latitude, first.longitude)
                    recentCityRepo.recordCitySearch(first.name, first.country, first.latitude, first.longitude)
                    _uiState.update {
                        it.copy(
                            weatherData = data,
                            isLoading = false,
                            searchQuery = first.name,
                            isUsingGpsLocation = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "City '$query' not found. Please try another name."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not load weather for '$query': ${e.localizedMessage ?: "Network error"}"
                    )
                }
            }
        }
    }

    fun refreshWeather(onGpsRefreshNeeded: (() -> Unit)? = null) {
        if (_uiState.value.isUsingGpsLocation || _uiState.value.isPreferGpsLocation) {
            val coords = lastGpsCoordinates
            if (coords != null) {
                loadWeatherFromGpsCoordinates(coords.first, coords.second)
            } else if (onGpsRefreshNeeded != null) {
                onGpsRefreshNeeded()
            } else {
                val current = _uiState.value.weatherData ?: return
                searchAndSelectFirst(current.cityName)
            }
        } else {
            val current = _uiState.value.weatherData ?: return
            searchAndSelectFirst(current.cityName)
        }
    }

    fun setGpsLocating(locating: Boolean) {
        _uiState.update { it.copy(isGpsLocating = locating) }
    }

    fun loadWeatherFromGpsCoordinates(lat: Double, lon: Double) {
        lastGpsCoordinates = Pair(lat, lon)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isGpsLocating = true, errorMessage = null) }
            try {
                val (city, country) = repository.reverseGeocode(lat, lon)
                val data = repository.fetchWeather(city, country, lat, lon)
                _uiState.update {
                    it.copy(
                        weatherData = data,
                        isLoading = false,
                        isGpsLocating = false,
                        isUsingGpsLocation = true,
                        searchQuery = city
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isGpsLocating = false,
                        errorMessage = "Could not fetch GPS location weather: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun setGpsLocationError(error: String) {
        _uiState.update {
            it.copy(
                isGpsLocating = false,
                errorMessage = error
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fetchWeatherForCity(cityName: String, country: String?, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val data = repository.fetchWeather(cityName, country, lat, lon)
                recentCityRepo.recordCitySearch(cityName, country, lat, lon)
                _uiState.update { it.copy(weatherData = data, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to update weather: ${e.localizedMessage ?: "Check connection"}"
                    )
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authManager.signInWithGoogle()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    private fun syncPreferencesToFirestore() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                firestoreRepo.syncUserProfile(
                    displayName = state.userDisplayName ?: "User",
                    email = state.userEmail ?: "",
                    temperatureUnit = state.temperatureUnit.name,
                    themeMode = state.themeMode.name,
                    preferGps = state.isPreferGpsLocation
                )
            } catch (e: Exception) {
                // Ignore sync errors gracefully
            }
        }
    }
}
