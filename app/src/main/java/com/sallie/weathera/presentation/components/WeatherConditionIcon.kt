package com.sallie.weathera.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.Secondary
import kotlin.math.cos
import kotlin.math.sin

enum class WeatherCondition(val displayName: String) {
    CLEAR_SKY("Cerah"),
    NIGHT_CLEAR("Malam Cerah"),
    PARTLY_CLOUDY("Sebagian Berawan"),
    CLOUDY("Berawan"),
    FOG("Berkabut"),
    DRIZZLE("Gerimis"),
    RAIN("Hujan"),
    THUNDERSTORM("Badai Petir"),
    SNOW("Salju");

    companion object {
        fun fromWmoCode(code: Int, isDay: Boolean = true): WeatherCondition {
            return when (code) {
                0 -> if (isDay) CLEAR_SKY else NIGHT_CLEAR
                1, 2 -> PARTLY_CLOUDY
                3 -> CLOUDY
                45, 48 -> FOG
                51, 53, 55, 56, 57 -> DRIZZLE
                61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
                71, 73, 75, 77, 85, 86 -> SNOW
                95, 96, 99 -> THUNDERSTORM
                else -> PARTLY_CLOUDY
            }
        }
    }
}

@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null,
    ) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (condition) {
            WeatherCondition.CLEAR_SKY -> drawSun(tint ?: Color(0xFFF59E0B))
            WeatherCondition.NIGHT_CLEAR -> drawMoon(tint ?: Color(0xFFFDE68A))
            WeatherCondition.PARTLY_CLOUDY -> {
                drawSun(
                    color = Color(0xFFF59E0B),
                    centerOffset = Offset(w * 0.7f, h * 0.35f),
                    radiusFactor = 0.22f,
                )
                drawCloud(tint ?: Color(0xFFEDE7FF), offset = Offset(0f, h * 0.15f), scale = 0.85f)
            }
            WeatherCondition.CLOUDY -> {
                drawCloud(Color(0xFFD1C4E9), offset = Offset(-w * 0.1f, -h * 0.05f), scale = 0.8f)
                drawCloud(tint ?: Color(0xFFEDE7FF), offset = Offset(0f, h * 0.1f), scale = 0.9f)
            }
            WeatherCondition.FOG -> {
                drawCloud(tint ?: Color(0xFFEDE7FF), offset = Offset(0f, 0f), scale = 0.8f)
                drawFogLines(Color(0xFFB49AFF))
            }
            WeatherCondition.DRIZZLE -> {
                drawCloud(tint ?: Color(0xFFD1C4E9), offset = Offset(0f, -h * 0.08f), scale = 0.85f)
                drawRainDrops(Color(0xFF93C5FD), heavy = false)
            }
            WeatherCondition.RAIN -> {
                drawCloud(tint ?: Color(0xFF7652E8), offset = Offset(0f, -h * 0.08f), scale = 0.85f)
                drawRainDrops(Color(0xFF60A5FA), heavy = true)
            }
            WeatherCondition.THUNDERSTORM -> {
                drawCloud(Color(0xFF4C309B), offset = Offset(0f, -h * 0.08f), scale = 0.85f)
                drawLightning(Color(0xFFFBBF24))
            }
            WeatherCondition.SNOW -> {
                drawCloud(tint ?: Color(0xFFEDE7FF), offset = Offset(0f, -h * 0.08f), scale = 0.85f)
                drawSnowFlakes(Color(0xFF93C5FD))
            }
        }
    }
}

private fun DrawScope.drawSun(
    color: Color,
    centerOffset: Offset = Offset(size.width / 2f, size.height / 2f),
    radiusFactor: Float = 0.28f,
) {
    val r = size.width * radiusFactor
    drawCircle(color = color, radius = r, center = centerOffset)

    val rayCount = 8
    val innerR = r * 1.35f
    val outerR = r * 1.75f
    for (i in 0 until rayCount) {
        val angle = (i * (360f / rayCount)) * (Math.PI / 180f)
        val startX = centerOffset.x + (innerR * cos(angle)).toFloat()
        val startY = centerOffset.y + (innerR * sin(angle)).toFloat()
        val endX = centerOffset.x + (outerR * cos(angle)).toFloat()
        val endY = centerOffset.y + (outerR * sin(angle)).toFloat()
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = size.width * 0.05f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawCloud(
    color: Color,
    offset: Offset = Offset.Zero,
    scale: Float = 1f,
) {
    val cx = (size.width / 2f) + offset.x
    val cy = (size.height / 2f) + offset.y
    val r = (size.width * 0.18f) * scale

    drawCircle(color = color, radius = r * 1.3f, center = Offset(cx, cy - r * 0.2f))
    drawCircle(color = color, radius = r, center = Offset(cx - r * 1.1f, cy + r * 0.2f))
    drawCircle(color = color, radius = r * 1.1f, center = Offset(cx + r * 1.1f, cy + r * 0.2f))
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - r * 1.2f, cy - r * 0.1f),
        size = Size(r * 2.4f, r * 1.4f),
        cornerRadius = CornerRadius(r * 0.7f, r * 0.7f),
    )
}

private fun DrawScope.drawRainDrops(color: Color, heavy: Boolean) {
    val dropCount = if (heavy) 4 else 3
    val yStart = size.height * 0.72f
    val len = size.height * (if (heavy) 0.18f else 0.12f)
    val spacing = size.width * 0.18f
    val startX = (size.width / 2f) - ((dropCount - 1) * spacing / 2f)

    for (i in 0 until dropCount) {
        val x = startX + (i * spacing)
        drawLine(
            color = color,
            start = Offset(x - 3f, yStart),
            end = Offset(x + 3f, yStart + len),
            strokeWidth = size.width * 0.05f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawLightning(color: Color) {
    val path = Path().apply {
        moveTo(size.width * 0.52f, size.height * 0.58f)
        lineTo(size.width * 0.42f, size.height * 0.75f)
        lineTo(size.width * 0.54f, size.height * 0.75f)
        lineTo(size.width * 0.44f, size.height * 0.96f)
        lineTo(size.width * 0.62f, size.height * 0.70f)
        lineTo(size.width * 0.50f, size.height * 0.70f)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}

private fun DrawScope.drawFogLines(color: Color) {
    val y1 = size.height * 0.76f
    val y2 = size.height * 0.88f
    drawLine(
        color = color,
        start = Offset(size.width * 0.22f, y1),
        end = Offset(size.width * 0.78f, y1),
        strokeWidth = size.width * 0.05f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.32f, y2),
        end = Offset(size.width * 0.68f, y2),
        strokeWidth = size.width * 0.05f,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawSnowFlakes(color: Color) {
    val y = size.height * 0.78f
    val xs = listOf(size.width * 0.32f, size.width * 0.5f, size.width * 0.68f)
    for (x in xs) {
        drawCircle(color = color, radius = size.width * 0.045f, center = Offset(x, y))
    }
}

private fun DrawScope.drawMoon(color: Color) {
    val cx = size.width * 0.46f
    val cy = size.height * 0.48f
    val r = size.width * 0.30f
    val moonPath = Path().apply {
        moveTo(cx + r * 0.45f, cy - r * 0.88f)
        cubicTo(cx - r * 0.7f, cy - r * 0.7f, cx - r * 0.7f, cy + r * 0.7f, cx + r * 0.45f, cy + r * 0.88f)
        cubicTo(cx - r * 0.05f, cy + r * 0.5f, cx - r * 0.05f, cy - r * 0.5f, cx + r * 0.45f, cy - r * 0.88f)
        close()
    }
    drawPath(path = moonPath, color = color)
    drawCircle(
        color = Color(0xFFFDE68A),
        radius = size.width * 0.04f,
        center = Offset(size.width * 0.74f, size.height * 0.28f),
    )
    drawCircle(
        color = Color(0xFFFDE68A).copy(alpha = 0.8f),
        radius = size.width * 0.025f,
        center = Offset(size.width * 0.84f, size.height * 0.42f),
    )
}

// ==========================================
// Vector Icons for Weather Metrics
// ==========================================

@Composable
fun HumidityIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            cubicTo(w * 0.85f, h * 0.55f, w * 0.85f, h * 0.85f, w * 0.5f, h * 0.9f)
            cubicTo(w * 0.15f, h * 0.85f, w * 0.15f, h * 0.55f, w * 0.5f, h * 0.1f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun WindSpeedIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.12f, cap = StrokeCap.Round)

        // Line 1 with swirl
        val path1 = Path().apply {
            moveTo(w * 0.1f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.35f)
            cubicTo(w * 0.85f, h * 0.35f, w * 0.85f, h * 0.1f, w * 0.65f, h * 0.1f)
        }
        drawPath(path = path1, color = color, style = stroke)

        // Line 2 with swirl
        val path2 = Path().apply {
            moveTo(w * 0.15f, h * 0.65f)
            lineTo(w * 0.55f, h * 0.65f)
            cubicTo(w * 0.75f, h * 0.65f, w * 0.75f, h * 0.9f, w * 0.55f, h * 0.9f)
        }
        drawPath(path = path2, color = color, style = stroke)
    }
}

@Composable
fun ThermometerIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stemW = w * 0.2f
        val bulbR = w * 0.24f

        drawRoundRect(
            color = color,
            topLeft = Offset((w - stemW) / 2f, h * 0.12f),
            size = Size(stemW, h * 0.55f),
            cornerRadius = CornerRadius(stemW / 2f, stemW / 2f),
        )
        drawCircle(
            color = color,
            radius = bulbR,
            center = Offset(w / 2f, h * 0.75f),
        )
    }
}

@Composable
fun PrecipitationIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        for (i in 0..2) {
            val x = w * (0.25f + i * 0.25f)
            drawLine(
                color = color,
                start = Offset(x - 2f, h * 0.25f),
                end = Offset(x + 2f, h * 0.75f),
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun UvIndexIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val r = w * 0.22f
        drawCircle(color = color, radius = r, center = Offset(w / 2f, h / 2f))
        val count = 8
        for (i in 0 until count) {
            val angle = i * (Math.PI * 2 / count)
            val startX = (w / 2f + r * 1.35f * cos(angle)).toFloat()
            val startY = (h / 2f + r * 1.35f * sin(angle)).toFloat()
            val endX = (w / 2f + r * 1.85f * cos(angle)).toFloat()
            val endY = (h / 2f + r * 1.85f * sin(angle)).toFloat()
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = w * 0.1f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun LocationPinIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.95f)
            cubicTo(w * 0.2f, h * 0.65f, w * 0.15f, h * 0.45f, w * 0.15f, h * 0.35f)
            cubicTo(w * 0.15f, h * 0.15f, w * 0.3f, h * 0.05f, w * 0.5f, h * 0.05f)
            cubicTo(w * 0.7f, h * 0.05f, w * 0.85f, h * 0.15f, w * 0.85f, h * 0.35f)
            cubicTo(w * 0.85f, h * 0.45f, w * 0.8f, h * 0.65f, w * 0.5f, h * 0.95f)
            close()
        }
        drawPath(path = path, color = color)
        drawCircle(color = Color.White, radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.35f))
    }
}

@Composable
fun SearchHeaderIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.13f, cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = w * 0.30f,
            center = Offset(w * 0.42f, h * 0.42f),
            style = stroke,
        )
        drawLine(
            color = color,
            start = Offset(w * 0.64f, h * 0.64f),
            end = Offset(w * 0.88f, h * 0.88f),
            strokeWidth = w * 0.13f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun RefreshIcon(modifier: Modifier = Modifier, color: Color = Primary) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.12f, cap = StrokeCap.Round)
        val r = w * 0.34f
        val cx = w * 0.5f
        val cy = h * 0.5f

        // Draw 270 degree circular arc
        drawArc(
            color = color,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = stroke,
        )

        // Arrow head at start of arc (around top-right)
        val arrowAngle = (45f) * (Math.PI / 180f)
        val ax = cx + (r * cos(arrowAngle)).toFloat()
        val ay = cy + (r * sin(arrowAngle)).toFloat()
        val path = Path().apply {
            moveTo(ax, ay - w * 0.18f)
            lineTo(ax, ay + w * 0.05f)
            lineTo(ax - w * 0.18f, ay)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}
