package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SleekColorScheme = lightColorScheme(
    primary = CyberCyan,
    secondary = NeonMint,
    tertiary = CyberGold,
    background = DeepSpaceDark,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onPrimary = DeepSpaceDark,
    onSecondary = DeepSpaceDark,
    onTertiary = DeepSpaceDark,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Sleek Interface is light-themed and clean
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}
