package com.example.dailycost.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = White,
    secondary = AccentGreen,
    onSecondary = White,
    background = White,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = LightGray,
    onSurfaceVariant = TextSecondary,
    outline = MediumGray,
    error = DeleteRed
)

@Composable
fun DailyCostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = DailyCostTypography,
        content = content
    )
}
