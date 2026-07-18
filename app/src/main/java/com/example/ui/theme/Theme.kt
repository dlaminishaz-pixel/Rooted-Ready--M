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

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandGold,
    secondary = PrimaryGreen,
    tertiary = GoldLight,
    background = DarkCharcoal,
    surface = Color(0xFF2D2D2D),
    onPrimary = DarkCharcoal,
    onSecondary = BrandCream,
    onTertiary = DarkCharcoal,
    onBackground = BrandCream,
    onSurface = BrandCream,
    surfaceVariant = Color(0xFF3D3D3D),
    onSurfaceVariant = BrandCream
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    secondary = BrandGold,
    tertiary = DarkCharcoal,
    background = BrandCream,
    surface = BrandWhite,
    onPrimary = BrandWhite,
    onSecondary = DarkCharcoal,
    onTertiary = BrandWhite,
    onBackground = DarkCharcoal,
    onSurface = DarkCharcoal,
    surfaceVariant = BrandCream,
    onSurfaceVariant = MutedCharcoal,
    outline = BrandGold
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to preserve strict custom corporate branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
