package com.sallie.weathera.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 2.5D Stylized Cozy Weather House & Night Sky Landscape
 * Inspired by the reference Dribbble design (purple house on winter hill with stars & warm lights).
 */
@Composable
fun WeatherHouseIllustration(
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val w = size.width
        val h = size.height

        // 1. Starry sparkles in the upper atmosphere
        val stars = listOf(
            Offset(w * 0.12f, h * 0.15f) to 2.5f,
            Offset(w * 0.22f, h * 0.28f) to 1.8f,
            Offset(w * 0.35f, h * 0.12f) to 2.2f,
            Offset(w * 0.72f, h * 0.18f) to 2.0f,
            Offset(w * 0.85f, h * 0.25f) to 2.8f,
            Offset(w * 0.90f, h * 0.10f) to 1.6f,
            Offset(w * 0.48f, h * 0.08f) to 3.0f,
            Offset(w * 0.62f, h * 0.22f) to 2.4f,
        )
        for ((pos, r) in stars) {
            drawCircle(
                color = Color(0xFFEDE7FF).copy(alpha = 0.85f),
                radius = r,
                center = pos,
            )
            // Tiny glow halo
            drawCircle(
                color = Color(0xFFB49AFF).copy(alpha = 0.3f),
                radius = r * 2.2f,
                center = pos,
            )
        }

        // 2. Rolling Hill / Snow Platform (Curved Ground)
        val groundPath = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.25f, h * 0.68f, w * 0.75f, h * 0.70f, w, h * 0.75f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = groundPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF553896).copy(alpha = 0.9f),
                    Color(0xFF38256A),
                ),
                startY = h * 0.65f,
                endY = h,
            ),
        )

        // Subtle hill highlight rim
        val rimPath = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.25f, h * 0.68f, w * 0.75f, h * 0.70f, w, h * 0.75f)
        }
        drawPath(
            path = rimPath,
            color = Color(0xFFB49AFF).copy(alpha = 0.45f),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round),
        )

        // 3. House Dimensions & Placement
        val houseW = w * 0.38f
        val houseH = h * 0.44f
        val houseX = (w - houseW) / 2f
        val houseY = h * 0.36f

        // 3a. Chimney with glowing smoke puffs
        val chimneyW = houseW * 0.16f
        val chimneyH = houseH * 0.32f
        val chimneyX = houseX + houseW * 0.75f
        val chimneyY = houseY - chimneyH * 0.6f

        drawRoundRect(
            color = Color(0xFF6B4FB0),
            topLeft = Offset(chimneyX, chimneyY),
            size = Size(chimneyW, chimneyH),
            cornerRadius = CornerRadius(4f, 4f),
        )
        // Chimney top rim
        drawRoundRect(
            color = Color(0xFFEDE7FF),
            topLeft = Offset(chimneyX - 2f, chimneyY - 4f),
            size = Size(chimneyW + 4f, 6f),
            cornerRadius = CornerRadius(3f, 3f),
        )
        // Smoke puffs
        drawCircle(
            color = Color(0xFFD6C8FF).copy(alpha = 0.5f),
            radius = chimneyW * 0.4f,
            center = Offset(chimneyX + chimneyW * 0.5f, chimneyY - 14f),
        )
        drawCircle(
            color = Color(0xFFD6C8FF).copy(alpha = 0.35f),
            radius = chimneyW * 0.6f,
            center = Offset(chimneyX + chimneyW * 0.8f, chimneyY - 30f),
        )

        // 3b. Main House Body (Mauve / Royal Violet)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF8665DC), Color(0xFF5E3FAF)),
                startY = houseY + houseH * 0.2f,
                endY = houseY + houseH,
            ),
            topLeft = Offset(houseX, houseY + houseH * 0.25f),
            size = Size(houseW, houseH * 0.75f),
            cornerRadius = CornerRadius(14f, 14f),
        )

        // 3c. Cozy Pitched Roof (Pastel Lavender with snow trim)
        val roofPath = Path().apply {
            moveTo(houseX - houseW * 0.12f, houseY + houseH * 0.32f)
            lineTo(houseX + houseW * 0.5f, houseY - houseH * 0.14f)
            lineTo(houseX + houseW * 1.12f, houseY + houseH * 0.32f)
            close()
        }
        drawPath(
            path = roofPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE4DAFF), Color(0xFFBCA6FF)),
                startY = houseY - houseH * 0.14f,
                endY = houseY + houseH * 0.32f,
            ),
        )

        // Roof overhang trim shadow
        drawLine(
            color = Color(0xFF382375).copy(alpha = 0.4f),
            start = Offset(houseX - houseW * 0.10f, houseY + houseH * 0.33f),
            end = Offset(houseX + houseW * 1.10f, houseY + houseH * 0.33f),
            strokeWidth = 3f,
        )

        // 3d. Warm Glowing Windows (Left & Right)
        val windowW = houseW * 0.24f
        val windowH = houseH * 0.26f
        val leftWinX = houseX + houseW * 0.15f
        val rightWinX = houseX + houseW * 0.61f
        val winY = houseY + houseH * 0.44f

        // Warm Yellow Glow behind windows
        drawRoundRect(
            color = Color(0xFFFDE68A),
            topLeft = Offset(leftWinX, winY),
            size = Size(windowW, windowH),
            cornerRadius = CornerRadius(6f, 6f),
        )
        drawRoundRect(
            color = Color(0xFFFDE68A),
            topLeft = Offset(rightWinX, winY),
            size = Size(windowW, windowH),
            cornerRadius = CornerRadius(6f, 6f),
        )

        // Window Frame Grid Lines (Warm Orange/Red borders)
        val frameColor = Color(0xFFD97706)
        // Left window cross
        drawLine(
            color = frameColor,
            start = Offset(leftWinX + windowW / 2f, winY),
            end = Offset(leftWinX + windowW / 2f, winY + windowH),
            strokeWidth = 2f,
        )
        drawLine(
            color = frameColor,
            start = Offset(leftWinX, winY + windowH / 2f),
            end = Offset(leftWinX + windowW, winY + windowH / 2f),
            strokeWidth = 2f,
        )
        // Right window cross
        drawLine(
            color = frameColor,
            start = Offset(rightWinX + windowW / 2f, winY),
            end = Offset(rightWinX + windowW / 2f, winY + windowH),
            strokeWidth = 2f,
        )
        drawLine(
            color = frameColor,
            start = Offset(rightWinX, winY + windowH / 2f),
            end = Offset(rightWinX + windowW, winY + windowH / 2f),
            strokeWidth = 2f,
        )

        // 3e. Round Attic Window in Roof
        val atticR = houseW * 0.08f
        val atticCenter = Offset(houseX + houseW * 0.5f, houseY + houseH * 0.12f)
        drawCircle(
            color = Color(0xFFFDE68A),
            radius = atticR,
            center = atticCenter,
        )
        drawCircle(
            color = Color(0xFFD97706),
            radius = atticR,
            center = atticCenter,
            style = Stroke(width = 2.5f),
        )

        // 3f. Front Entrance Door (Centered)
        val doorW = houseW * 0.18f
        val doorH = houseH * 0.28f
        val doorX = houseX + (houseW - doorW) / 2f
        val doorY = houseY + houseH - doorH + 2f

        drawRoundRect(
            color = Color(0xFF3C2673),
            topLeft = Offset(doorX, doorY),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(6f, 6f),
        )
        // Door knob
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 2.5f,
            center = Offset(doorX + doorW * 0.78f, doorY + doorH * 0.52f),
        )

        // 4. Little decorative winter trees on side of the house
        drawDecorativeTree(
            center = Offset(houseX - houseW * 0.28f, h * 0.74f),
            scale = 0.9f,
        )
        drawDecorativeTree(
            center = Offset(houseX + houseW * 1.30f, h * 0.76f),
            scale = 1.1f,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDecorativeTree(
    center: Offset,
    scale: Float,
) {
    val treeW = 22f * scale
    val treeH = 38f * scale

    // Trunk
    drawLine(
        color = Color(0xFF4A3282),
        start = Offset(center.x, center.y),
        end = Offset(center.x, center.y + 12f * scale),
        strokeWidth = 4f * scale,
        cap = StrokeCap.Round,
    )

    // Foliage Cone Layers
    val path = Path().apply {
        moveTo(center.x, center.y - treeH)
        lineTo(center.x - treeW, center.y)
        lineTo(center.x + treeW, center.y)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF8B6CDE), Color(0xFF5535A3)),
            startY = center.y - treeH,
            endY = center.y,
        ),
    )
}
