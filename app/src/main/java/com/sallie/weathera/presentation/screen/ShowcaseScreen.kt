package com.sallie.weathera.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sallie.weathera.presentation.components.HumidityIcon
import com.sallie.weathera.presentation.components.PrecipitationIcon
import com.sallie.weathera.presentation.components.SectionHeader
import com.sallie.weathera.presentation.components.ThermometerIcon
import com.sallie.weathera.presentation.components.WeatherCard
import com.sallie.weathera.presentation.components.WeatherCondition
import com.sallie.weathera.presentation.components.WeatherConditionIcon
import com.sallie.weathera.presentation.components.WeatherEmptyState
import com.sallie.weathera.presentation.components.WeatherErrorState
import com.sallie.weathera.presentation.components.WeatherGradientCard
import com.sallie.weathera.presentation.components.WeatherLoadingButton
import com.sallie.weathera.presentation.components.WeatherLoadingIndicator
import com.sallie.weathera.presentation.components.WeatherMetricItem
import com.sallie.weathera.presentation.components.WeatherOutlinedButton
import com.sallie.weathera.presentation.components.WeatherPrimaryButton
import com.sallie.weathera.presentation.components.WeatherSecondaryButton
import com.sallie.weathera.presentation.components.WindSpeedIcon
import com.sallie.weathera.ui.theme.BackgroundDark
import com.sallie.weathera.ui.theme.BackgroundLight
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Error
import com.sallie.weathera.ui.theme.OutlineLight
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.PrimaryDark
import com.sallie.weathera.ui.theme.PrimaryTextDark
import com.sallie.weathera.ui.theme.PrimaryTextLight
import com.sallie.weathera.ui.theme.Secondary
import com.sallie.weathera.ui.theme.SecondaryTextDark
import com.sallie.weathera.ui.theme.SecondaryTextLight
import com.sallie.weathera.ui.theme.Success
import com.sallie.weathera.ui.theme.SurfaceDark
import com.sallie.weathera.ui.theme.SurfaceLight
import com.sallie.weathera.ui.theme.Warning
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing
import com.sallie.weathera.ui.theme.WeatheraTheme
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShowcaseScreen(
    modifier: Modifier = Modifier,
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }

    WeatheraTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = WeatheraSpacing.lg, vertical = WeatheraSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.xxl),
            ) {
                // ==========================================
                // A. Top Header
                // ==========================================
                ShowcaseHeader(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = it },
                )

                // ==========================================
                // B. Color Palette Preview
                // ==========================================
                ColorPaletteSection()

                // ==========================================
                // C. Typography Preview
                // ==========================================
                TypographySection()

                // ==========================================
                // D. Button Showcase
                // ==========================================
                ButtonShowcaseSection()

                // ==========================================
                // E. Weather Card Showcase
                // ==========================================
                WeatherCardSection()

                // ==========================================
                // F. Weather Metrics Showcase
                // ==========================================
                WeatherMetricsSection()

                // ==========================================
                // G. Component States
                // ==========================================
                ComponentStatesSection()

                Spacer(modifier = Modifier.height(WeatheraSpacing.xxxl))
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-sections
// -------------------------------------------------------------

@Composable
private fun ShowcaseHeader(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
) {
    WeatherCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WeatherConditionIcon(
                        condition = WeatherCondition.CLEAR_SKY,
                        size = 32.dp,
                    )
                    Spacer(modifier = Modifier.width(WeatheraSpacing.sm))
                    Text(
                        text = "Weathera",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
                Text(
                    text = "Design System Showcase",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isDarkTheme) "Dark Mode" else "Light Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onToggleTheme,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = Primary,
                        uncheckedTrackColor = ElectricLavender.copy(alpha = 0.4f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ColorPaletteSection() {
    Column {
        SectionHeader(
            title = "Color Palette",
            subtitle = "Royal Violet, Electric Lavender, & Theme Semantics",
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        val colors = listOf(
            Triple("Primary", "#7652E8", Primary),
            Triple("Primary Dark", "#6340D0", PrimaryDark),
            Triple("Secondary", "#9673F5", Secondary),
            Triple("Lavender", "#B49AFF", ElectricLavender),
            Triple("Bg Light", "#F4F0FF", BackgroundLight),
            Triple("Surface Light", "#FFFFFF", SurfaceLight),
            Triple("Text Light", "#35245F", PrimaryTextLight),
            Triple("Sec Light", "#75698F", SecondaryTextLight),
            Triple("Bg Dark", "#292044", BackgroundDark),
            Triple("Surface Dark", "#382B58", SurfaceDark),
            Triple("Text Dark", "#FFFFFF", PrimaryTextDark),
            Triple("Sec Dark", "#C0B4DA", SecondaryTextDark),
            Triple("Success", "#22C55E", Success),
            Triple("Warning", "#F59E0B", Warning),
            Triple("Error", "#EF4444", Error),
        )

        colors.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
            ) {
                rowItems.forEach { (name, hex, color) ->
                    ColorChip(
                        name = name,
                        hex = hex,
                        color = color,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
        }
    }
}

@Composable
private fun ColorChip(
    name: String,
    hex: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    WeatherCard(
        shape = RoundedCornerShape(WeatheraCornerRadius.Medium),
        contentPadding = WeatheraSpacing.sm,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
            )
            Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = hex,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TypographySection() {
    Column {
        SectionHeader(
            title = "Typography System",
            subtitle = "Plus Jakarta Sans Font Hierarchy",
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        WeatherCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.md)) {
                TypoRow("Display Large (56sp)", "28°C", MaterialTheme.typography.displayLarge)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Display Medium (44sp)", "Cerah Berawan", MaterialTheme.typography.displayMedium)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Headline Large (32sp)", "Jakarta Selatan", MaterialTheme.typography.headlineLarge)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Headline Medium (26sp)", "Prakiraan 7 Hari", MaterialTheme.typography.headlineMedium)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Title Large (22sp)", "Kondisi Hari Ini", MaterialTheme.typography.titleLarge)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Title Medium (18sp)", "Ringkasan Cuaca", MaterialTheme.typography.titleMedium)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Body Large (16sp)", "Kelembapan relatif tinggi dengan angin sepoi.", MaterialTheme.typography.bodyLarge)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Body Medium (14sp)", "Pembaruan otomatis dari Open-Meteo API.", MaterialTheme.typography.bodyMedium)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Body Small (12sp)", "Diperbarui 5 menit yang lalu", MaterialTheme.typography.bodySmall)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Label Large (14sp)", "LIHAT SELENGKAPNYA", MaterialTheme.typography.labelLarge)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                TypoRow("Label Medium (12sp)", "INDEKS UV: 6 (TINGGI)", MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TypoRow(label: String, example: String, style: androidx.compose.ui.text.TextStyle) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
        Text(
            text = example,
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ButtonShowcaseSection() {
    val coroutineScope = rememberCoroutineScope()
    var isSimulatingLoading by remember { mutableStateOf(false) }

    Column {
        SectionHeader(
            title = "Button Showcase",
            subtitle = "Primary, Secondary, Outlined, Disabled, & Loading",
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        WeatherCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.md)) {
                WeatherPrimaryButton(
                    text = "Primary Button (Perbarui Cuaca)",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                WeatherSecondaryButton(
                    text = "Secondary Button (Pilih Kota)",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                WeatherOutlinedButton(
                    text = "Outlined Button (Pengaturan)",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                WeatherPrimaryButton(
                    text = "Disabled Button",
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )

                WeatherLoadingButton(
                    text = if (isSimulatingLoading) "Sedang Memuat..." else "Klik untuk Tes Loading Button",
                    isLoading = isSimulatingLoading,
                    onClick = {
                        isSimulatingLoading = true
                        coroutineScope.launch {
                            delay(2.seconds)
                            isSimulatingLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun WeatherCardSection() {
    Column {
        SectionHeader(
            title = "Weather Card Showcase",
            subtitle = "Hero Weather Card & Standard Surface Card",
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        // Hero Gradient Card
        WeatherGradientCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "Jakarta, Indonesia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
                    Text(
                        text = "Senin, 23 November 2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricLavender,
                    )
                }

                WeatherConditionIcon(
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    size = 54.dp,
                )
            }

            Spacer(modifier = Modifier.height(WeatheraSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "28°",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Sebagian Berawan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tertinggi: 32°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Text(
                        text = "Terendah: 24°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricLavender,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricsSection() {
    Column {
        SectionHeader(
            title = "Weather Metrics Showcase",
            subtitle = "Humidity, Wind Speed, Feels Like, & Precipitation",
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
        ) {
            WeatherMetricItem(
                label = "Kelembapan",
                value = "78",
                unit = "%",
                icon = { HumidityIcon() },
                modifier = Modifier.weight(1f),
            )
            WeatherMetricItem(
                label = "Kecepatan Angin",
                value = "14",
                unit = "km/j",
                icon = { WindSpeedIcon() },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
        ) {
            WeatherMetricItem(
                label = "Terasa Seperti",
                value = "30",
                unit = "°C",
                icon = { ThermometerIcon() },
                modifier = Modifier.weight(1f),
            )
            WeatherMetricItem(
                label = "Curah Hujan",
                value = "12",
                unit = "%",
                icon = { PrecipitationIcon() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ComponentStatesSection() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Loading", "Error", "Empty State")

    Column {
        SectionHeader(
            title = "Component States",
            subtitle = "Loading, Error, & Empty State Displays",
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
        ) {
            tabs.forEachIndexed { index, tab ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(tab) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        WeatherCard(modifier = Modifier.fillMaxWidth()) {
            when (selectedTab) {
                0 -> WeatherLoadingIndicator(
                    message = "Mengambil data prakiraan cuaca terbaru...",
                )
                1 -> WeatherErrorState(
                    title = "Koneksi Terputus",
                    message = "Tidak dapat menghubungi server Open-Meteo. Pastikan perangkat terhubung ke internet.",
                    retryText = "Coba Lagi",
                    onRetry = {},
                )
                2 -> WeatherEmptyState(
                    title = "Belum Ada Kota Dipilih",
                    description = "Cari dan pilih kota favorit Anda untuk mulai memantau ramalan cuaca harian.",
                    actionText = "Cari Kota",
                    onActionClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowcaseScreenLightPreview() {
    WeatheraTheme(darkTheme = false) {
        ShowcaseScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ShowcaseScreenDarkPreview() {
    WeatheraTheme(darkTheme = true) {
        ShowcaseScreen()
    }
}
