package com.sallie.weathera.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sallie.weathera.domain.model.CurrentWeather
import com.sallie.weathera.domain.model.DailyForecastItem
import com.sallie.weathera.domain.model.HourlyForecastItem
import com.sallie.weathera.domain.model.WeatherDemoData
import com.sallie.weathera.presentation.components.LocationPinIcon
import com.sallie.weathera.presentation.components.PrecipitationIcon
import com.sallie.weathera.presentation.components.RefreshIcon
import com.sallie.weathera.presentation.components.SearchHeaderIcon
import com.sallie.weathera.presentation.components.SectionHeader
import com.sallie.weathera.presentation.components.WeatherCard
import com.sallie.weathera.presentation.components.WeatherCondition
import com.sallie.weathera.presentation.components.WeatherConditionIcon
import com.sallie.weathera.presentation.components.WeatherDetailsSection
import com.sallie.weathera.presentation.components.WeatherErrorState
import com.sallie.weathera.presentation.components.WeatherGradientCard
import com.sallie.weathera.presentation.components.WeatherHouseIllustration
import com.sallie.weathera.presentation.components.WeatherLoadingIndicator
import com.sallie.weathera.presentation.viewmodel.WeatherUiState
import com.sallie.weathera.presentation.viewmodel.WeatherViewModel
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.Secondary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing
import com.sallie.weathera.ui.theme.WeatheraTheme

/**
 * Weathera Complete Home Screen
 * Section 1: Hero Current Weather + Cozy 2.5D House Illustration + 24H Hourly Forecast
 * Section 2: Comprehensive Weather Details Dashboard (Air Quality, UV, Sunrise/Sunset, Wind, Rainfall, Humidity)
 * Section 3: Detailed 7-Day Forecast & Attribution
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateToLocations: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is WeatherUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                WeatherLoadingIndicator(
                    message = "Memuat data cuaca Surakarta..."
                )
            }
        }
        is WeatherUiState.Error -> {
            if (state.previousData != null) {
                Column(modifier = modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(WeatheraSpacing.sm),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                    HomeScreenContent(
                        currentWeather = state.previousData.currentWeather,
                        hourlyForecast = state.previousData.hourlyForecast,
                        dailyForecast = state.previousData.dailyForecast,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateToLocations = onNavigateToLocations,
                        onRefresh = { viewModel.refresh() },
                    )
                }
            } else {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    WeatherErrorState(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                    )
                }
            }
        }
        is WeatherUiState.Success -> {
            HomeScreenContent(
                currentWeather = state.currentWeather,
                hourlyForecast = state.hourlyForecast,
                dailyForecast = state.dailyForecast,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onNavigateToLocations = onNavigateToLocations,
                onRefresh = { viewModel.refresh() },
                modifier = modifier,
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    currentWeather: CurrentWeather = WeatherDemoData.initialCurrentWeather,
    hourlyForecast: List<HourlyForecastItem> = WeatherDemoData.hourlyForecast,
    dailyForecast: List<DailyForecastItem> = WeatherDemoData.dailyForecast,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateToLocations: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = WeatheraSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.xl),
    ) {
        // ==========================================
        // A. HEADER & LOCATION
        // ==========================================
        HomeHeaderSection(
            cityName = currentWeather.cityName,
            country = currentWeather.country,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onSearchClick = onNavigateToLocations,
            onRefreshClick = onRefresh,
            modifier = Modifier.padding(horizontal = WeatheraSpacing.lg),
        )

        // ==========================================
        // B. CURRENT WEATHER (HERO SECTION + HOUSE)
        // ==========================================
        HeroCurrentWeatherCard(
            weather = currentWeather,
            modifier = Modifier.padding(horizontal = WeatheraSpacing.lg),
        )

        // ==========================================
        // C. FORECAST TABS & HOURLY FORECAST (24 HOURS)
        // ==========================================
        HourlyForecastSection(
            hourlyItems = hourlyForecast,
            dailyItems = dailyForecast,
        )

        // ==========================================
        // D. WEATHER DETAILS DASHBOARD (SECTION 2)
        // ==========================================
        WeatherDetailsSection(
            weather = currentWeather,
            dailyForecast = dailyForecast,
            modifier = Modifier.padding(horizontal = WeatheraSpacing.lg),
        )

        // ==========================================
        // E. FOOTER & WEATHER ATTRIBUTION
        // ==========================================
        HomeFooterSection(
            lastUpdated = currentWeather.lastUpdated,
            modifier = Modifier.padding(horizontal = WeatheraSpacing.lg),
        )

        // Bottom space so content is never obscured by floating bottom nav bar
        Spacer(modifier = Modifier.height(84.dp))
    }
}

// -------------------------------------------------------------
// Section A: Header & Location
// -------------------------------------------------------------

@Composable
private fun HomeHeaderSection(
    cityName: String,
    country: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Location Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onSearchClick),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(WeatheraCornerRadius.Medium),
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(WeatheraCornerRadius.Medium),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                LocationPinIcon(color = Primary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(WeatheraSpacing.md))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                    )
                    Text(
                        text = ", $country",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Text(
                    text = "Weathera • Tap untuk ubah kota",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Action Buttons: Theme Toggle, Refresh, Search
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Theme toggle button
            Surface(
                onClick = onToggleTheme,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    WeatherConditionIcon(
                        condition = if (isDarkTheme) WeatherCondition.NIGHT_CLEAR else WeatherCondition.CLEAR_SKY,
                        size = 18.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))

            // Refresh button
            Surface(
                onClick = onRefreshClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    RefreshIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(WeatheraSpacing.xs))

            // Search shortcut button
            Surface(
                onClick = onSearchClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    SearchHeaderIcon(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Section B: Hero Current Weather Card with Cozy House Art
// -------------------------------------------------------------

@Composable
private fun HeroCurrentWeatherCard(
    weather: CurrentWeather,
    modifier: Modifier = Modifier,
) {
    WeatherGradientCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 8.dp,
        contentPadding = WeatheraSpacing.lg,
    ) {
        // Date & Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = weather.dateFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ElectricLavender,
            )

            // Weather condition pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(WeatheraCornerRadius.Small))
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = WeatheraSpacing.md, vertical = 3.dp),
            ) {
                Text(
                    text = weather.condition.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        // Center Big Temperature Display
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "${weather.temperature}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 68.sp,
                    lineHeight = 74.sp,
                    color = Color.White,
                )
                Text(
                    text = "°",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp,
                    color = ElectricLavender,
                )
            }

            Text(
                text = "${weather.condition.displayName} • H:${weather.tempMax}°  L:${weather.tempMin}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        // Cozy Stylized Weather House Illustration
        WeatherHouseIllustration(
            height = 175.dp,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

        // Bottom summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Terasa seperti ${weather.feelsLike}°C",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = "Kelembapan ${weather.humidity}%",
                style = MaterialTheme.typography.bodySmall,
                color = ElectricLavender,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// -------------------------------------------------------------
// Section C: Hourly Forecast & Tabs (Inspired by Screen 1)
// -------------------------------------------------------------

@Composable
private fun HourlyForecastSection(
    hourlyItems: List<HourlyForecastItem>,
    dailyItems: List<DailyForecastItem>,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        // Tab Selector: "Hourly Forecast" | "Weekly Forecast"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WeatheraSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(WeatheraCornerRadius.Medium))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(WeatheraCornerRadius.Medium),
                    )
                    .padding(4.dp),
            ) {
                ForecastTabButton(
                    title = "Hourly Forecast",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                )
                Spacer(modifier = Modifier.width(4.dp))
                ForecastTabButton(
                    title = "Weekly Forecast",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                )
            }

            Text(
                text = if (selectedTab == 0) "24 Jam" else "7 Hari",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        if (selectedTab == 0) {
            // Horizontal 24-Hour LazyRow
            LazyRow(
                contentPadding = PaddingValues(horizontal = WeatheraSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
            ) {
                items(hourlyItems) { item ->
                    HourlyForecastCard(item = item)
                }
            }
        } else {
            // Quick Weekly Preview Horizontal Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = WeatheraSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
            ) {
                items(dailyItems) { item ->
                    WeeklyPreviewCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun ForecastTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(WeatheraCornerRadius.Small))
            .background(if (isSelected) Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = WeatheraSpacing.md, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HourlyForecastCard(
    item: HourlyForecastItem,
    modifier: Modifier = Modifier,
) {
    val isHighlighted = item.isNow

    Box(
        modifier = modifier
            .width(68.dp)
            .shadow(
                elevation = if (isHighlighted) 8.dp else 2.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Primary.copy(alpha = 0.4f),
                spotColor = Primary.copy(alpha = 0.5f),
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (isHighlighted) {
                    Brush.verticalGradient(
                        colors = listOf(Primary, Secondary),
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                        ),
                    )
                },
            )
            .border(
                width = if (isHighlighted) 1.5.dp else 1.dp,
                color = if (isHighlighted) ElectricLavender else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(26.dp),
            )
            .padding(vertical = WeatheraSpacing.md, horizontal = WeatheraSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Time
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

            // Weather Icon
            WeatherConditionIcon(
                condition = item.condition,
                size = 30.dp,
            )

            Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

            // Rain percentage badge
            if (item.rainProbability > 0) {
                Text(
                    text = "${item.rainProbability}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) Color(0xFF93C5FD) else Color(0xFF60A5FA),
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

            // Temperature
            Text(
                text = "${item.temperature}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WeeklyPreviewCard(
    item: DailyForecastItem,
    modifier: Modifier = Modifier,
) {
    val isToday = item.dayName == "Today"

    Box(
        modifier = modifier
            .width(76.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (isToday) {
                    Brush.verticalGradient(listOf(Primary, Secondary))
                } else {
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface),
                    )
                },
            )
            .border(
                width = 1.dp,
                color = if (isToday) ElectricLavender else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(26.dp),
            )
            .padding(vertical = WeatheraSpacing.md, horizontal = WeatheraSpacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (isToday) "Today" else item.dayName.take(3),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

            WeatherConditionIcon(
                condition = item.condition,
                size = 28.dp,
            )

            Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

            Text(
                text = "${item.tempMax}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "${item.tempMin}°",
                style = MaterialTheme.typography.bodySmall,
                color = if (isToday) ElectricLavender else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// -------------------------------------------------------------
// Section E: Footer & Weather Information
// -------------------------------------------------------------

@Composable
private fun HomeFooterSection(
    lastUpdated: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = WeatheraSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Data Cuaca disediakan oleh Open-Meteo",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Text(
            text = "Pembaruan terakhir: $lastUpdated",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

        Text(
            text = "Data saat ini merupakan simulasi demo untuk pratinjau antarmuka pengguna.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(showBackground = true, name = "Home Screen - Dark Mode")
@Composable
fun HomeScreenDarkPreview() {
    WeatheraTheme(darkTheme = true) {
        HomeScreenContent(
            currentWeather = WeatherDemoData.initialCurrentWeather,
            hourlyForecast = WeatherDemoData.hourlyForecast,
            dailyForecast = WeatherDemoData.dailyForecast,
            isDarkTheme = true,
            onToggleTheme = {},
            onNavigateToLocations = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true, name = "Home Screen - Light Mode")
@Composable
fun HomeScreenLightPreview() {
    WeatheraTheme(darkTheme = false) {
        HomeScreenContent(
            currentWeather = WeatherDemoData.initialCurrentWeather,
            hourlyForecast = WeatherDemoData.hourlyForecast,
            dailyForecast = WeatherDemoData.dailyForecast,
            isDarkTheme = false,
            onToggleTheme = {},
            onNavigateToLocations = {},
            onRefresh = {},
        )
    }
}
