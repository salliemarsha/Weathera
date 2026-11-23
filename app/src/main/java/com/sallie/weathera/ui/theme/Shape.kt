package com.sallie.weathera.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object WeatheraCornerRadius {
    val Small = 10.dp
    val Medium = 16.dp
    val Large = 22.dp
    val ExtraLarge = 28.dp
}

val WeatheraShapes = Shapes(
    small = RoundedCornerShape(WeatheraCornerRadius.Small),
    medium = RoundedCornerShape(WeatheraCornerRadius.Medium),
    large = RoundedCornerShape(WeatheraCornerRadius.Large),
    extraLarge = RoundedCornerShape(WeatheraCornerRadius.ExtraLarge),
)

// Alias for default MaterialTheme compatibility
val Shapes = WeatheraShapes
