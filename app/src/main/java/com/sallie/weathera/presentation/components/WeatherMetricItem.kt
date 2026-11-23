package com.sallie.weathera.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing

@Composable
fun WeatherMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    icon: (@Composable () -> Unit)? = null,
) {
    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Medium),
        contentPadding = WeatheraSpacing.md,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = Primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(WeatheraCornerRadius.Small),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    icon()
                }
                Spacer(modifier = Modifier.width(WeatheraSpacing.md))
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
