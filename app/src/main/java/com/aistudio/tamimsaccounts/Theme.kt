package com.aistudio.tamimsaccounts

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

val EmeraldPrimary = Color(0xFF0F5132)
val EmeraldMedium = Color(0xFF198754)
val EmeraldLight = Color(0xFFE8F5E9)
val EmeraldDark = Color(0xFF0A3622)
val AccentTeal = Color(0xFF20C997)

val DebtRed = Color(0xFFDC3545)
val DebtRedLight = Color(0xFFFDE8E8)
val CreditGreen = Color(0xFF198754)
val CreditGreenLight = Color(0xFFDEF7EC)

val BackgroundLight = Color(0xFFF4F6F8)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceSubtle = Color(0xFFF8FAF9)
val BorderSubtle = Color(0xFFE5E7EB)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldDark,
    secondary = EmeraldMedium,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = AccentTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceSubtle,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    error = DebtRed,
    errorContainer = DebtRedLight,
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF20C997),
    onPrimary = Color(0xFF0F5132),
    primaryContainer = Color(0xFF0A3622),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF34D399),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
