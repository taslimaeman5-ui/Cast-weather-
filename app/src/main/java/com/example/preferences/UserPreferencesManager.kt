package com.example.preferences

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("skycast_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PREFER_GPS = "key_prefer_gps"
        private const val KEY_DEFAULT_CITY = "key_default_city"
        private const val KEY_TEMP_UNIT = "key_temp_unit"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }

    var isPreferGpsLocation: Boolean
        get() = prefs.getBoolean(KEY_PREFER_GPS, false)
        set(value) = prefs.edit().putBoolean(KEY_PREFER_GPS, value).apply()

    var defaultCity: String
        get() = prefs.getString(KEY_DEFAULT_CITY, "Tokyo") ?: "Tokyo"
        set(value) = prefs.edit().putString(KEY_DEFAULT_CITY, value).apply()

    var savedTemperatureUnit: String
        get() = prefs.getString(KEY_TEMP_UNIT, "CELSIUS") ?: "CELSIUS"
        set(value) = prefs.edit().putString(KEY_TEMP_UNIT, value).apply()

    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            return try {
                AppThemeMode.valueOf(name)
            } catch (_: Exception) {
                AppThemeMode.SYSTEM
            }
        }
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
}
