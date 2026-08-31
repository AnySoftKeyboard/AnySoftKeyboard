package com.anysoftkeyboard.addon.apk.ui

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

private val AskPurple = Color(0xFF663399)
private val AskPurpleLight = Color(0xFFCC99FF)
private val AskPurpleDark = Color(0xFF9966CC)
private val AskLink = Color(0xFF0099CC)

private val LightColorScheme =
    lightColorScheme(
        primary = AskPurple,
        secondary = AskPurpleDark,
        tertiary = AskLink,
        primaryContainer = AskPurpleLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = AskPurpleLight,
        secondary = AskPurpleDark,
        tertiary = AskLink,
        primaryContainer = AskPurple,
    )

@Composable
fun AddOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

  MaterialTheme(
      colorScheme = colorScheme,
      content = content,
  )
}
