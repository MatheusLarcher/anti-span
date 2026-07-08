package com.larchertech.antispam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerLight,
    error = Red40,
)

private val DarkColors = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo40,
    primaryContainer = IndigoContainerDark,
    error = Red40,
)

@Composable
fun AntiSpamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AntiSpamTypography,
        content = content,
    )
}
