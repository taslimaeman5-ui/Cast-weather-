package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.preferences.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = DarkSkyPrimary,
    onPrimary = Color(0xFF00354E),
    primaryContainer = Color(0xFF004D6F),
    onPrimaryContainer = Color(0xFFC6E7FF),
    secondary = Color(0xFFB3C8D8),
    onSecondary = Color(0xFF1E3240),
    secondaryContainer = Color(0xFF344957),
    onSecondaryContainer = Color(0xFFCFE4F4),
    tertiary = Color(0xFFC7C2EA),
    background = DarkSkyBackground,
    onBackground = Color(0xFFE1E2E5),
    surface = DarkSkySurface,
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = DarkSkySurfaceVariant,
    onSurfaceVariant = Color(0xFFC2C7CF),
    outline = Color(0xFF8C9199)
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBE6FF),
    onPrimaryContainer = Color(0xFF001E2E),
    secondary = Color(0xFF4F606E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E5F5),
    onSecondaryContainer = Color(0xFF0B1D29),
    tertiary = Color(0xFF63597C),
    background = LightSkyBackground,
    onBackground = Color(0xFF191C1E),
    surface = LightSkySurface,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = LightSkySurfaceVariant,
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72777F)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
