package com.sallie.weathera.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallie.weathera.R
import com.sallie.weathera.domain.model.CurrentWeather
import com.sallie.weathera.domain.model.DailyForecastItem
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing
import kotlin.math.cos
import kotlin.math.sin

/**
 * Detailed Weather Dashboard (Section 2 of Home Screen)
 */
@Composable
fun WeatherDetailsSection(
    weather: CurrentWeather,
    dailyForecast: List<DailyForecastItem>,
    modifier: Modifier = Modifier,
    onAirQualityClick: () -> Unit = {},
    onUvClick: () -> Unit = {},
    onWindClick: () -> Unit = {},
    onPrecipitationClick: () -> Unit = {},
    onFeelsLikeClick: () -> Unit = {},
    onHumidityClick: () -> Unit = {},
    onSunriseSunsetClick: () -> Unit = {},
    onDayClick: (DailyForecastItem) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
    ) {
        SectionHeader(
            title = stringResource(R.string.weather_details),
            subtitle = stringResource(R.string.weather_details_subtitle),
        )

        // 1. Air Quality Card (Full Width)
        AirQualityCard(weather = weather, onClick = onAirQualityClick)

        // 2. Row: UV Index & Sunrise/Sunset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
        ) {
            UvIndexCard(
                uvIndex = weather.uvIndex,
                onClick = onUvClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            SunriseSunsetCard(
                sunrise = weather.sunriseSunset.sunrise,
                sunset = weather.sunriseSunset.sunset,
                progress = weather.sunriseSunset.sunProgress,
                onClick = onSunriseSunsetClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        // 3. Row: Wind & Rainfall
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
        ) {
            WindCompassCard(
                speedKmh = weather.windInfo.speedKmh,
                direction = weather.windInfo.direction,
                angleDegree = weather.windInfo.angleDegree,
                onClick = onWindClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            RainfallCard(
                lastHourMm = weather.rainfallInfo.lastHourMm,
                expectedNext24hMm = weather.rainfallInfo.expectedNext24hMm,
                onClick = onPrecipitationClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        // 4. Row: Feels Like & Humidity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
        ) {
            FeelsLikeCard(
                feelsLike = weather.feelsLike,
                actualTemp = weather.temperature,
                onClick = onFeelsLikeClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            HumidityGaugeCard(
                humidity = weather.humidity,
                onClick = onHumidityClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        // 5. Detailed 7-Day Forecast Card
        DetailedWeeklyForecastCard(
            dailyForecast = dailyForecast,
            onDayClick = onDayClick,
        )
    }
}

// -------------------------------------------------------------------
// Component A: Air Quality Card
// -------------------------------------------------------------------

@Composable
fun AirQualityCard(
    weather: CurrentWeather,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    WeatherCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.air_quality_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
            Text(
                text = stringResource(R.string.see_details),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Text(
            text = "${weather.airQuality.index} - ${stringResource(R.string.uv_low)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        // Multi-color gradient indicator bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = weather.airQuality.progress.coerceIn(0.1f, 1f))
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF60A5FA),
                                Color(0xFF34D399),
                                Color(0xFFFBBF24),
                                Color(0xFFF87171),
                            ),
                        ),
                    ),
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Text(
            text = stringResource(R.string.air_quality_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// -------------------------------------------------------------------
// Component B: UV Index Card
// -------------------------------------------------------------------

@Composable
fun UvIndexCard(
    uvIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val categoryText = getUvCategoryText(uvIndex)

    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UvIndexIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.uv_index),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        Text(
            text = "$uvIndex",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = categoryText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        val uvProgress = (uvIndex / 11f).coerceIn(0.1f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = uvProgress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Primary, ElectricLavender),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun getUvCategoryText(uvIndex: Int): String {
    return when {
        uvIndex <= 2 -> stringResource(R.string.uv_low)
        uvIndex <= 5 -> stringResource(R.string.uv_moderate)
        uvIndex <= 7 -> stringResource(R.string.uv_high)
        uvIndex <= 10 -> stringResource(R.string.uv_very_high)
        else -> stringResource(R.string.uv_extreme)
    }
}

// -------------------------------------------------------------------
// Component C: Sunrise & Sunset Card
// -------------------------------------------------------------------

@Composable
fun SunriseSunsetCard(
    sunrise: String,
    sunset: String,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(color = Color(0xFFFBBF24), radius = size.width * 0.35f, center = Offset(size.width / 2f, size.height / 2f))
            }
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.sunrise),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Text(
            text = sunrise,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            val w = size.width
            val h = size.height

            drawLine(
                color = Color(0xFF6B4FB0).copy(alpha = 0.35f),
                start = Offset(0f, h * 0.85f),
                end = Offset(w, h * 0.85f),
                strokeWidth = 2f,
            )

            val arcPath = Path().apply {
                moveTo(w * 0.1f, h * 0.85f)
                cubicTo(w * 0.25f, h * 0.15f, w * 0.75f, h * 0.15f, w * 0.9f, h * 0.85f)
            }
            drawPath(
                path = arcPath,
                color = Color(0xFFB49AFF).copy(alpha = 0.7f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round),
            )

            val sunX = w * (0.1f + 0.8f * progress.coerceIn(0f, 1f))
            val sunY = h * 0.85f - (4f * (progress) * (1f - progress) * (h * 0.70f))
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = 5.dp.toPx(),
                center = Offset(sunX, sunY),
            )
            drawCircle(
                color = Color(0xFFFDE68A).copy(alpha = 0.5f),
                radius = 9.dp.toPx(),
                center = Offset(sunX, sunY),
            )
        }

        Text(
            text = "${stringResource(R.string.sunset)}: $sunset",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// -------------------------------------------------------------------
// Component D: Wind Compass Card
// -------------------------------------------------------------------

@Composable
fun WindCompassCard(
    speedKmh: Float,
    direction: String,
    angleDegree: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WindSpeedIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.wind_speed),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                val w = size.width
                val r = w / 2f
                val cx = r
                val cy = r

                drawCircle(
                    color = Color(0xFF8B6CDE).copy(alpha = 0.35f),
                    radius = r - 2f,
                    style = Stroke(width = 1.5f),
                )

                for (i in 0 until 8) {
                    val angle = (i * 45f) * (Math.PI / 180f)
                    val isCardinal = (i % 2 == 0)
                    val innerLen = if (isCardinal) r * 0.78f else r * 0.85f
                    val outerLen = r - 4f
                    val startX = (cx + innerLen * cos(angle)).toFloat()
                    val startY = (cy + innerLen * sin(angle)).toFloat()
                    val endX = (cx + outerLen * cos(angle)).toFloat()
                    val endY = (cy + outerLen * sin(angle)).toFloat()
                    drawLine(
                        color = if (isCardinal) Color(0xFFB49AFF) else Color(0xFF75698F),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isCardinal) 2f else 1f,
                        cap = StrokeCap.Round,
                    )
                }

                val rad = (angleDegree - 90f) * (Math.PI / 180f)
                val arrowTipX = (cx + (r * 0.65f) * cos(rad)).toFloat()
                val arrowTipY = (cy + (r * 0.65f) * sin(rad)).toFloat()
                drawCircle(
                    color = Color(0xFF7652E8),
                    radius = 3.5.dp.toPx(),
                    center = Offset(arrowTipX, arrowTipY),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$speedKmh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "km/h $direction",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// -------------------------------------------------------------------
// Component E: Rainfall Card
// -------------------------------------------------------------------

@Composable
fun RainfallCard(
    lastHourMm: Float,
    expectedNext24hMm: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrecipitationIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.precipitation),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$lastHourMm",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = "mm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }

        Text(
            text = stringResource(R.string.precipitation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// -------------------------------------------------------------------
// Component F: Feels Like Card
// -------------------------------------------------------------------

@Composable
fun FeelsLikeCard(
    feelsLike: Int,
    actualTemp: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ThermometerIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.feels_like),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Text(
            text = "$feelsLike°",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Text(
            text = stringResource(R.string.feels_like_temp, feelsLike),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp,
        )
    }
}

// -------------------------------------------------------------------
// Component G: Humidity Gauge Card
// -------------------------------------------------------------------

@Composable
fun HumidityGaugeCard(
    humidity: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    WeatherCard(
        modifier = modifier,
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HumidityIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.humidity),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Text(
            text = "$humidity%",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Text(
            text = stringResource(R.string.humidity_val, humidity),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp,
        )
    }
}

// -------------------------------------------------------------------
// Component H: Detailed 7-Day Forecast Card
// -------------------------------------------------------------------

@Composable
fun DetailedWeeklyForecastCard(
    dailyForecast: List<DailyForecastItem>,
    modifier: Modifier = Modifier,
    onDayClick: (DailyForecastItem) -> Unit = {},
) {
    WeatherCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        contentPadding = WeatheraSpacing.lg,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.daily_forecast),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        Column {
            dailyForecast.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDayClick(item) }
                        .padding(vertical = WeatheraSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val formattedDay = formatDayName(item.dayName)
                    val isToday = (item.dayName.equals("Today", ignoreCase = true) || item.dayName.equals("Hari Ini", ignoreCase = true))

                    Text(
                        text = formattedDay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(105.dp),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        WeatherConditionIcon(
                            condition = item.condition,
                            size = 26.dp,
                        )
                        Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
                        if (item.rainProbability > 0) {
                            Text(
                                text = "${item.rainProbability}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))
                        }
                        Text(
                            text = item.condition.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = "${item.tempMin}°",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.width(WeatheraSpacing.xs))

                        Box(
                            modifier = Modifier
                                .width(46.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF818CF8),
                                            Color(0xFFF472B6),
                                            Color(0xFFFBBF24),
                                        ),
                                    ),
                                ),
                        )

                        Spacer(modifier = Modifier.width(WeatheraSpacing.xs))

                        Text(
                            text = "${item.tempMax}°",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (index < dailyForecast.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.7.dp,
                        modifier = Modifier.padding(vertical = WeatheraSpacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
fun formatDayName(dayName: String): String {
    return when (dayName.lowercase()) {
        "today", "hari ini" -> stringResource(R.string.day_today)
        "monday", "senin" -> stringResource(R.string.day_monday)
        "tuesday", "selasa" -> stringResource(R.string.day_tuesday)
        "wednesday", "rabu" -> stringResource(R.string.day_wednesday)
        "thursday", "kamis" -> stringResource(R.string.day_thursday)
        "friday", "jumat" -> stringResource(R.string.day_friday)
        "saturday", "sabtu" -> stringResource(R.string.day_saturday)
        "sunday", "minggu" -> stringResource(R.string.day_sunday)
        else -> dayName
    }
}
