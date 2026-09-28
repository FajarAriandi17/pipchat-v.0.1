package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PipCustomColors(
    val gold: Color,
    val buy: Color,
    val sell: Color,
    val surface2: Color,
    val line: Color,
    val textMuted: Color
)

val LocalPipCustomColors = staticCompositionLocalOf {
    PipCustomColors(
        gold = PipGold,
        buy = PipBuy,
        sell = PipSell,
        surface2 = PipDarkSurface2,
        line = PipDarkLine,
        textMuted = PipDarkMuted
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = PipGold,
    onPrimary = PipDarkBg,
    primaryContainer = PipDarkSurface2,
    onPrimaryContainer = PipGold,
    secondary = PipBuy,
    onSecondary = Color.White,
    error = PipSell,
    onError = Color.White,
    background = PipDarkBg,
    onBackground = PipDarkText,
    surface = PipDarkSurface,
    onSurface = PipDarkText,
    surfaceVariant = PipDarkSurface2,
    onSurfaceVariant = PipDarkMuted,
    outline = PipDarkLine
)

private val LightColorScheme = lightColorScheme(
    primary = PipGoldDark,
    onPrimary = Color.White,
    primaryContainer = PipLightSurface2,
    onPrimaryContainer = PipGoldDark,
    secondary = PipBuyDark,
    onSecondary = Color.White,
    error = PipSellDark,
    onError = Color.White,
    background = PipLightBg,
    onBackground = PipLightText,
    surface = PipLightSurface,
    onSurface = PipLightText,
    surfaceVariant = PipLightSurface2,
    onSurfaceVariant = PipLightMuted,
    outline = PipLightLine
)

@Composable
fun PipChatTheme(
    darkTheme: Boolean = true, // Dark theme is default per PRD Section 6.1
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        PipCustomColors(
            gold = PipGold,
            buy = PipBuy,
            sell = PipSell,
            surface2 = PipDarkSurface2,
            line = PipDarkLine,
            textMuted = PipDarkMuted
        )
    } else {
        PipCustomColors(
            gold = PipGoldDark,
            buy = PipBuyDark,
            sell = PipSellDark,
            surface2 = PipLightSurface2,
            line = PipLightLine,
            textMuted = PipLightMuted
        )
    }

    CompositionLocalProvider(LocalPipCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
