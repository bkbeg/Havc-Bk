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

import androidx.compose.ui.graphics.Color

private val BentoLightColorScheme =
  lightColorScheme(
    primary = BentoCardPurpleDark,
    secondary = BentoCardPurpleLight,
    tertiary = BentoCardPink,
    background = BentoBg,
    surface = BentoSurface,
    onPrimary = Color.White,
    onSecondary = BentoCardPurpleOnContainer,
    onTertiary = Color(0xFF31111D),
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorder
  )

private val BentoDarkColorScheme =
  darkColorScheme(
    primary = BentoCardPurpleLight,
    secondary = BentoCardPurpleDark,
    tertiary = BentoCardPink,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2D2A30),
    onPrimary = Color(0xFF21005D),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = BentoBg,
    onSurface = BentoBg,
    onSurfaceVariant = BentoCardGray,
    outline = BentoBorder
  )


@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors defaults exactly to Bento style
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) BentoDarkColorScheme else BentoLightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

