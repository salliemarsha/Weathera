package com.sallie.weathera.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.WeatheraSpacing

@Composable
fun WeatherEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(WeatheraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = Primary.copy(alpha = 0.10f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                icon()
            } else {
                WeatherConditionIcon(
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    size = 40.dp,
                )
            }
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(WeatheraSpacing.lg))
            WeatherOutlinedButton(
                text = actionText,
                onClick = onActionClick,
            )
        }
    }
}
