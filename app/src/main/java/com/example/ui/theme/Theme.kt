package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = DMLPrimary,
    secondary = DMLSecondary,
    tertiary = DMLTertiary,
    background = DMLSecondary,
    surface = Color(0xFF382A2D),
    onBackground = DMLBackground,
    onSurface = DMLBackground
)

private val LightColorScheme = lightColorScheme(
    primary = DMLPrimary,
    onPrimary = Color.White,
    primaryContainer = DMLPrimaryContainer,
    onPrimaryContainer = DMLPrimary,
    secondary = DMLSecondary,
    onSecondary = Color.White,
    secondaryContainer = DMLSecondaryContainer,
    onSecondaryContainer = DMLSecondary,
    tertiary = DMLTertiary,
    onTertiary = Color.White,
    background = DMLBackground,
    onBackground = DMLSecondary,
    surface = DMLSurface,
    onSurface = DMLSecondary,
    outline = DMLOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to ensure our light pink branding takes absolute precedence!
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
