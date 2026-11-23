package com.sallie.weathera.presentation.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sallie.weathera.ui.theme.StatusError
import com.sallie.weathera.ui.theme.WeatheraSpacing

@Composable
fun WeatherErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Gagal Memuat Data",
    retryText: String? = "Coba Lagi",
    onRetry: (() -> Unit)? = null,
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
                .size(64.dp)
                .background(color = StatusError.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val w = size.width
                val h = size.height
                // Exclamation mark
                drawLine(
                    color = StatusError,
                    start = Offset(w / 2f, h * 0.15f),
                    end = Offset(w / 2f, h * 0.65f),
                    strokeWidth = w * 0.14f,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = StatusError,
                    radius = w * 0.08f,
                    center = Offset(w / 2f, h * 0.85f),
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
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (retryText != null && onRetry != null) {
            Spacer(modifier = Modifier.height(WeatheraSpacing.lg))
            WeatherPrimaryButton(
                text = retryText,
                onClick = onRetry,
            )
        }
    }
}
