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
    primary = MintGreenDark,
    secondary = SecondarySageDark,
    tertiary = TertiaryClayDark,
    background = DarkBg,
    surface = DarkSurface,
    primaryContainer = Color(0xFF1F3517),
    secondaryContainer = Color(0xFF263222),
    tertiaryContainer = Color(0xFF12283A),
    surfaceVariant = Color(0xFF1C2219),
    onPrimary = Color(0xFF0C2405),
    onSecondary = TextLight,
    onTertiary = TextLight,
    onPrimaryContainer = TextLight,
    onSecondaryContainer = TextLight,
    onTertiaryContainer = Color(0xFF9ECEFF),
    onBackground = TextLight,
    onSurface = TextLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondarySage,
    tertiary = TertiaryClay,
    background = LightBg,
    surface = LightSurface,
    primaryContainer = ActionAccent,       // Light Green Accent/Briefing bg (#D7E8CD)
    secondaryContainer = NeutralVariant,   // Soft Sage-grey Info background (#F2F2E7)
    tertiaryContainer = LevelBlue,         // Level badge bg (#E2F3FF)
    surfaceVariant = NavBackground,        // Bottom Navigation/Variant background (#F0F5EB)
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onPrimaryContainer = TextDark,
    onSecondaryContainer = TextDark,
    onTertiaryContainer = OnLevelBlue,     // Level blue text (#006495)
    onBackground = TextDark,
    onSurface = TextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
