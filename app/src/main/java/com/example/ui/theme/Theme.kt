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

private val DarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = Color(0xFF031A33),
    primaryContainer = CyanContainer,
    onPrimaryContainer = OnCyanContainer,
    secondary = VioletPrimary,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = VioletContainer,
    onSecondaryContainer = OnVioletContainer,
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFE9D5FF),
    background = NavyDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = NavyDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NavyDarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = NavyDarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = RosePriorityHigh,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF6B21A8),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF64748B),
    outline = LightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = RosePriorityHigh,
    onError = Color.White
)

@Composable
fun StudyFlowTheme(
    darkTheme: Boolean = true, // Default to sleek premium dark navy mode
    dynamicColor: Boolean = false, // Keep intentional brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards-compatible alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    StudyFlowTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
