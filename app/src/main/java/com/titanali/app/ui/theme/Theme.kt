package com.titanali.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TitanPurple = Color(0xFF6C4AB6)
val TitanPurpleDark = Color(0xFF4A2F8F)
val TitanAmber = Color(0xFFFFB74D)
val TitanTeal = Color(0xFF26A69A)
val TitanBg = Color(0xFFF7F5FB)
val TitanBgDark = Color(0xFF14121B)
val TitanSurfaceDark = Color(0xFF1E1B29)

val LightColors = lightColorScheme(
    primary = TitanPurpleDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4DDF8),
    onPrimaryContainer = Color(0xFF241050),
    secondary = TitanTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEEE9),
    onSecondaryContainer = Color(0xFF003732),
    tertiary = TitanAmber,
    onTertiary = Color(0xFF402A00),
    background = TitanBg,
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF513A8A),
    onPrimaryContainer = Color(0xFFE9DFFB),
    secondary = Color(0xFF8CD7CE),
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF0F514C),
    onSecondaryContainer = Color(0xFFCDEEE9),
    tertiary = Color(0xFFFFDDB3),
    onTertiary = Color(0xFF402A00),
    background = TitanBgDark,
    onBackground = Color(0xFFE6E0E9),
    surface = TitanSurfaceDark,
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun TitanaliTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content,
    )
}
