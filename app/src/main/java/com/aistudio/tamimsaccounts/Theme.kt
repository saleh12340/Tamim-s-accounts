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

val IndigoPrimary = Color(0xFF3F51B5)
val IndigoDark = Color(0xFF303F9F)
val IndigoLight = Color(0xFFE8EAF6)
val PinkAccent = Color(0xFFFF4081)

val DebtRed = Color(0xFFD32F2F)
val DebtRedLight = Color(0xFFFFEBEE)
val CreditGreen = Color(0xFF388E3C)
val CreditGreenLight = Color(0xFFE8F5E9)

val EmeraldPrimary = Color(0xFF10B981)
val EmeraldMedium = Color(0xFF059669)
val EmeraldLight = Color(0xFFD1FAE5)

val BackgroundLight = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceSubtle = Color(0xFFF5F5F5)
val BorderSubtle = Color(0xFFE0E0E0)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoLight,
    onPrimaryContainer = IndigoDark,
    secondary = PinkAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC5CAE9),
    onSecondaryContainer = Color(0xFF1A237E),
    tertiary = Color(0xFF1A237E),
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceSubtle,
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    error = DebtRed,
    errorContainer = DebtRedLight,
    onErrorContainer = Color(0xFFB71C1C)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    onPrimary = Color(0xFF1A237E),
    primaryContainer = Color(0xFF283593),
    onPrimaryContainer = Color(0xFFC5CAE9),
    secondary = Color(0xFFFF80AB),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onBackground = Color(0xFFE1E1E1),
    onSurface = Color(0xFFE1E1E1),
    error = Color(0xFFCF6679),
    errorContainer = Color(0xFFB00020)
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
