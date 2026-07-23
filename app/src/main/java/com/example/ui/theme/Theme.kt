package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElderlyPrimaryContainer,
    onPrimary = ElderlyOnPrimaryContainer,
    primaryContainer = ElderlyPrimary,
    onPrimaryContainer = ElderlyOnPrimary,
    secondary = ElderlySecondaryContainer,
    onSecondary = ElderlyOnSecondaryContainer,
    secondaryContainer = ElderlySecondary,
    onSecondaryContainer = ElderlyOnSecondary,
    background = ElderlyOnSurface,
    surface = ElderlyOnSurfaceVariant,
    onBackground = ElderlyBackground,
    onSurface = ElderlySurface,
    error = ElderlyErrorContainer,
    onError = ElderlyOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ElderlyPrimary,
    onPrimary = ElderlyOnPrimary,
    primaryContainer = ElderlyPrimaryContainer,
    onPrimaryContainer = ElderlyOnPrimaryContainer,
    secondary = ElderlySecondary,
    onSecondary = ElderlyOnSecondary,
    secondaryContainer = ElderlySecondaryContainer,
    onSecondaryContainer = ElderlyOnSecondaryContainer,
    background = ElderlyBackground,
    surface = ElderlySurface,
    onBackground = ElderlyOnSurface,
    onSurface = ElderlyOnSurface,
    onSurfaceVariant = ElderlyOnSurfaceVariant,
    error = ElderlyError,
    onError = ElderlyOnError,
    errorContainer = ElderlyErrorContainer,
    onErrorContainer = ElderlyOnErrorContainer
)

@Composable
fun MedReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep high contrast brand palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

