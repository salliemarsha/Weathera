package com.sallie.weathera.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallie.weathera.R
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary

enum class NavDestination {
    HOME,
    LOCATIONS,
    SETTINGS,
}

/**
 * Weathera 3-Tab Bottom Navigation Bar (Home, Locations, Settings)
 */
@Composable
fun WeatherBottomNav(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    savedLocationsCount: Int = 0,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = Primary.copy(alpha = 0.4f),
                    spotColor = Primary.copy(alpha = 0.5f),
                ),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        ElectricLavender.copy(alpha = 0.6f),
                        Primary.copy(alpha = 0.2f),
                    ),
                ),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. Home Tab
                NavTabItem(
                    label = stringResource(R.string.nav_home),
                    isSelected = (currentDestination == NavDestination.HOME),
                    onClick = { onNavigate(NavDestination.HOME) },
                    icon = { selected ->
                        HomeNavIcon(isSelected = selected)
                    },
                )

                // 2. Locations Tab
                NavTabItem(
                    label = stringResource(R.string.nav_locations),
                    isSelected = (currentDestination == NavDestination.LOCATIONS),
                    onClick = { onNavigate(NavDestination.LOCATIONS) },
                    badgeCount = savedLocationsCount,
                    icon = { selected ->
                        LocationsNavIcon(isSelected = selected)
                    },
                )

                // 3. Settings Tab
                NavTabItem(
                    label = stringResource(R.string.nav_settings),
                    isSelected = (currentDestination == NavDestination.SETTINGS),
                    onClick = { onNavigate(NavDestination.SETTINGS) },
                    icon = { selected ->
                        SettingsNavIcon(isSelected = selected)
                    },
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    icon: @Composable (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            icon(isSelected)
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$badgeCount",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Primary),
            )
        }
    }
}

// -------------------------------------------------------------
// Vector Navigation Icons
// -------------------------------------------------------------

@Composable
private fun HomeNavIcon(isSelected: Boolean) {
    val activeColor = Primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val color = if (isSelected) activeColor else inactiveColor

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.9f, h * 0.50f)
            lineTo(w * 0.78f, h * 0.50f)
            lineTo(w * 0.78f, h * 0.88f)
            lineTo(w * 0.22f, h * 0.88f)
            lineTo(w * 0.22f, h * 0.50f)
            lineTo(w * 0.1f, h * 0.50f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = if (isSelected) Fill else Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun LocationsNavIcon(isSelected: Boolean) {
    val activeColor = Primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val color = if (isSelected) activeColor else inactiveColor

        val barH = 2.5f
        for (i in 0..2) {
            val y = h * (0.28f + i * 0.24f)
            drawCircle(
                color = color,
                radius = 2.5.dp.toPx(),
                center = Offset(w * 0.22f, y),
            )
            drawLine(
                color = color,
                start = Offset(w * 0.42f, y),
                end = Offset(w * 0.88f, y),
                strokeWidth = barH.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SettingsNavIcon(isSelected: Boolean) {
    val activeColor = Primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val color = if (isSelected) activeColor else inactiveColor
        val center = Offset(w / 2f, h / 2f)
        val outerR = w * 0.38f
        val innerR = w * 0.18f

        drawCircle(
            color = color,
            radius = innerR,
            center = center,
            style = if (isSelected) Fill else Stroke(width = 2.dp.toPx()),
        )
        drawCircle(
            color = color,
            radius = outerR,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
        for (i in 0 until 6) {
            val angle = Math.toRadians(i * 60.0)
            val startX = (center.x + innerR * Math.cos(angle)).toFloat()
            val startY = (center.y + innerR * Math.sin(angle)).toFloat()
            val endX = (center.x + (outerR + 3.dp.toPx()) * Math.cos(angle)).toFloat()
            val endY = (center.y + (outerR + 3.dp.toPx()) * Math.sin(angle)).toFloat()
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}
