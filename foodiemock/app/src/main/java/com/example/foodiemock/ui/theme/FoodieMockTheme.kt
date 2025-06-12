package com.example.foodiemock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// PUBLIC_INTERFACE
@Composable
fun FoodieMockTheme(
    primaryColor: Color = Color(0xFFFF5722),
    secondaryColor: Color = Color(0xFFFFFFFF),
    accentColor: Color = Color(0xFF4CAF50),
    content: @Composable () -> Unit
) {
    val colors = lightColors(
        primary = primaryColor,
        primaryVariant = Color(0xFFC41C00),
        secondary = accentColor,
        background = secondaryColor,
        surface = secondaryColor,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onSurface = Color.Black,
        onBackground = Color.Black
    )

    MaterialTheme(
        colors = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
