package com.sallie.weathera.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.Secondary

enum class NavDestination {
    HOME,
    LOCATIONS,
}

/**
 * Curved Floating Bottom Navigation Bar
 * Directly inspired by the reference Dribbble design (Home icon, Center FAB plus, List/Locations icon).
 */
@Composable
fun WeatherBottomNav(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onCenterFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    savedLocationsCount: Int = 0,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Floating pill container with subtle blur/gradient
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
            border = androidx.compose.foundation.BorderStroke(
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
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. Home Tab
                val isHomeSelected = currentDestination == NavDestination.HOME
                NavTabItem(
                    label = "Home",
                    isSelected = isHomeSelected,
                    onClick = { onNavigate(NavDestination.HOME) },
                    icon = { selected ->
                        HomeNavIcon(isSelected = selected)
                    },
                )

                // Placeholder space for the Center Elevated FAB
                Spacer(modifier = Modifier.size(54.dp))

                // 2. Locations Tab
                val isLocationsSelected = currentDestination == NavDestination.LOCATIONS
                NavTabItem(
                    label = "Locations",
                    isSelected = isLocationsSelected,
                    onClick = { onNavigate(NavDestination.LOCATIONS) },
                    badgeCount = savedLocationsCount,
                    icon = { selected ->
                        LocationsNavIcon(isSelected = selected)
                    },
                )
            }
        }

        // Center Floating Action Button with Glow
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(58.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Primary,
                    spotColor = Primary,
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricLavender, Primary, Secondary),
                    ),
                )
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCenterFabClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            PlusNavIcon(color = Color.White)
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
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

        // Stylized house icon
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
            style = if (isSelected) androidx.compose.ui.graphics.drawscope.Fill else Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
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

        // 3 horizontal list bars with dots
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
private fun PlusNavIcon(color: Color = Color.White) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 3.dp.toPx()

        // Horizontal bar
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.5f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        // Vertical bar
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.8f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
