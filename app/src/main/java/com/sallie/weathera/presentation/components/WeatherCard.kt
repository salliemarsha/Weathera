package com.sallie.weathera.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.Secondary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(WeatheraCornerRadius.Large),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    contentPadding: Dp = WeatheraSpacing.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(borderWidth, borderColor),
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content,
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(borderWidth, borderColor),
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content,
            )
        }
    }
}

/**
 * Hero Weather Card with Royal Violet gradient background.
 */
@Composable
fun WeatherGradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(WeatheraCornerRadius.ExtraLarge),
    gradientBrush: Brush = Brush.linearGradient(
        colors = listOf(Primary, Secondary),
    ),
    elevation: Dp = 4.dp,
    contentPadding: Dp = WeatheraSpacing.xxl,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(contentPadding),
        ) {
            Column(content = content)
        }
    }
}
