package com.sallie.weathera.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sallie.weathera.domain.model.CityWeather
import com.sallie.weathera.presentation.components.SearchHeaderIcon
import com.sallie.weathera.presentation.components.SectionHeader
import com.sallie.weathera.presentation.components.WeatherConditionIcon
import com.sallie.weathera.presentation.components.WeatherEmptyState
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.Secondary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing

/**
 * Locations Screen (Search & Saved Locations)
 * Directly inspired by Screen 3 in the reference image (slanted/curved violet weather cards).
 */
@Composable
fun LocationsScreen(
    savedCities: List<CityWeather>,
    allDemoCities: List<CityWeather>,
    activeCity: CityWeather,
    onSelectCity: (CityWeather) -> Unit,
    onToggleSaveCity: (CityWeather) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val searchResults = remember(searchQuery, allDemoCities) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allDemoCities.filter {
                it.cityName.contains(searchQuery, ignoreCase = true) ||
                    it.country.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = WeatheraSpacing.lg)
            .padding(top = WeatheraSpacing.lg),
    ) {
        // 1. Header
        LocationsHeader(
            onBackClick = onBackToHome,
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        // 2. Search City Bar
        LocationSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClear = {
                searchQuery = ""
                focusManager.clearFocus()
            },
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.lg))

        // 3. Main List Content (Search Results OR Saved Locations)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.md),
        ) {
            // Case A: User is searching
            if (searchQuery.isNotBlank()) {
                item {
                    SectionHeader(
                        title = "Hasil Pencarian",
                        subtitle = "${searchResults.size} kota ditemukan untuk \"$searchQuery\"",
                    )
                }

                if (searchResults.isEmpty()) {
                    item {
                        WeatherEmptyState(
                            title = "Kota Tidak Ditemukan",
                            description = "Coba gunakan nama kota demo lain seperti Surakarta, Jakarta, Kathmandu, atau Denpasar.",
                        )
                    }
                } else {
                    items(searchResults, key = { it.id }) { city ->
                        val isSaved = savedCities.any { it.id == city.id }
                        val isSelected = city.id == activeCity.id

                        SavedLocationCard(
                            city = city.copy(isSaved = isSaved),
                            isActive = isSelected,
                            onCardClick = {
                                onSelectCity(city)
                                onBackToHome()
                            },
                            onToggleSave = { onToggleSaveCity(city) },
                        )
                    }
                }
            } else {
                // Case B: Saved Locations List
                item {
                    SectionHeader(
                        title = "Lokasi Tersimpan",
                        subtitle = "${savedCities.size} kota dalam pantauan cuaca Anda",
                    )
                }

                if (savedCities.isEmpty()) {
                    item {
                        WeatherEmptyState(
                            title = "Belum Ada Lokasi Tersimpan",
                            description = "Gunakan kolom pencarian di atas untuk menemukan kota favorit dan simpan di sini.",
                        )
                    }
                } else {
                    items(savedCities, key = { it.id }) { city ->
                        val isSelected = city.id == activeCity.id
                        SavedLocationCard(
                            city = city,
                            isActive = isSelected,
                            onCardClick = {
                                onSelectCity(city)
                                onBackToHome()
                            },
                            onToggleSave = { onToggleSaveCity(city) },
                        )
                    }
                }
            }

            // Bottom space for floating navigation bar
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// Component A: Locations Header
// -------------------------------------------------------------

@Composable
private fun LocationsHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onBackClick),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "‹",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 26.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WeatheraSpacing.sm))

            Column {
                Text(
                    text = "Weather Locations",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Kelola kota & pantauan cuaca",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            ),
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "•••",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component B: Location Search Bar
// -------------------------------------------------------------

@Composable
fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
        placeholder = {
            Text(
                text = "Search for a city or airport...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        },
        leadingIcon = {
            SearchHeaderIcon(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

// -------------------------------------------------------------
// Component C: Saved Location Card (Inspired by Reference 3)
// -------------------------------------------------------------

@Composable
fun SavedLocationCard(
    city: CityWeather,
    isActive: Boolean,
    onCardClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Unique rounded card with rich violet gradient inspired by Kathmandu & Montreal cards
    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isActive) 10.dp else 4.dp,
                shape = RoundedCornerShape(WeatheraCornerRadius.ExtraLarge),
                ambientColor = Primary.copy(alpha = 0.35f),
                spotColor = Primary.copy(alpha = 0.45f),
            ),
        shape = RoundedCornerShape(WeatheraCornerRadius.ExtraLarge),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (isActive) {
            androidx.compose.foundation.BorderStroke(2.dp, ElectricLavender)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (isActive) {
                            listOf(Color(0xFF6B42DC), Color(0xFF8B64EE))
                        } else {
                            listOf(Color(0xFF4B328C), Color(0xFF6444B6))
                        },
                    ),
                )
                .padding(WeatheraSpacing.xl),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left info: Temperature, High/Low, City, Country
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${city.temperature}°",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 48.sp,
                            lineHeight = 52.sp,
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(WeatheraSpacing.sm))
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(WeatheraCornerRadius.Small))
                                    .background(ElectricLavender.copy(alpha = 0.35f))
                                    .padding(horizontal = WeatheraSpacing.sm, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "Aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

                    Text(
                        text = "H:${city.tempMax}°  L:${city.tempMin}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

                    Text(
                        text = "${city.cityName}, ${city.country}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Right info: Weather Condition Icon & Save/Bookmark Toggle
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Save / Bookmark action button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable(onClick = onToggleSave),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (city.isSaved) "★" else "☆",
                            fontSize = 16.sp,
                            color = if (city.isSaved) Color(0xFFFBBF24) else Color.White,
                        )
                    }

                    Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

                    WeatherConditionIcon(
                        condition = city.condition,
                        size = 56.dp,
                    )

                    Spacer(modifier = Modifier.height(WeatheraSpacing.xs))

                    Text(
                        text = city.condition.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = ElectricLavender,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
