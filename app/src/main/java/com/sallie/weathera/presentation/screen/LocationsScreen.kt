package com.sallie.weathera.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sallie.weathera.R
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.domain.model.SearchResultLocation
import com.sallie.weathera.presentation.components.LocationPinIcon
import com.sallie.weathera.presentation.components.SearchHeaderIcon
import com.sallie.weathera.presentation.components.WeatherCard
import com.sallie.weathera.presentation.components.WeatherEmptyState
import com.sallie.weathera.presentation.components.WeatherLoadingIndicator
import com.sallie.weathera.presentation.components.WeatherPrimaryButton
import com.sallie.weathera.presentation.viewmodel.LocationsViewModel
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing

@Composable
fun LocationsScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationsViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    LocationsScreenContent(
        searchQuery = searchQuery,
        isSearching = isSearching,
        searchResults = searchResults,
        searchError = searchError,
        preferences = preferences,
        onQueryChange = { viewModel.onQueryChanged(it) },
        onSearchSubmit = { viewModel.onSearchClicked() },
        onClear = { viewModel.onClearQuery() },
        onSelectLocation = { location ->
            viewModel.selectLocation(location) {
                onNavigateToHome()
            }
        },
        modifier = modifier,
    )
}

@Composable
fun LocationsScreenContent(
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<SearchResultLocation>,
    searchError: String?,
    preferences: UserPreferences,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClear: () -> Unit,
    onSelectLocation: (SearchResultLocation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val popularPresetCities = listOf(
        SearchResultLocation("p1", "Surakarta", "Indonesia", "Jawa Tengah", -7.5755, 110.8243, "Asia/Jakarta"),
        SearchResultLocation("p2", "Jakarta", "Indonesia", "DKI Jakarta", -6.2088, 106.8456, "Asia/Jakarta"),
        SearchResultLocation("p3", "Yogyakarta", "Indonesia", "DIY", -7.7956, 110.3695, "Asia/Jakarta"),
        SearchResultLocation("p4", "Bandung", "Indonesia", "Jawa Barat", -6.9175, 107.6191, "Asia/Jakarta"),
        SearchResultLocation("p5", "Surabaya", "Indonesia", "Jawa Timur", -7.2575, 112.7521, "Asia/Jakarta"),
        SearchResultLocation("p6", "Denpasar", "Indonesia", "Bali", -8.6705, 115.2126, "Asia/Makassar"),
        SearchResultLocation("p7", "Kathmandu", "Nepal", "Bagmati", 27.7172, 85.3240, "Asia/Kathmandu"),
        SearchResultLocation("p8", "Montreal", "Canada", "Quebec", 45.5017, -73.5673, "America/Toronto"),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = WeatheraSpacing.lg)
            .padding(top = WeatheraSpacing.lg),
    ) {
        // Header & Title
        Column {
            Text(
                text = stringResource(R.string.locations_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.locations_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(WeatheraSpacing.md))

        // Search Bar
        LocationSearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            onSearchSubmit = {
                onSearchSubmit()
                focusManager.clearFocus()
            },
            onClear = {
                onClear()
                focusManager.clearFocus()
            },
        )

        Spacer(modifier = Modifier.height(WeatheraSpacing.lg))

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.lg),
            ) {
                // Left Column: Active Location Card
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(bottom = 84.dp),
                ) {
                    ActiveLocationCard(preferences = preferences)
                }

                // Right Column: Search Results / Popular Preset Cities
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(bottom = 84.dp),
                ) {
                    LocationResultsList(
                        searchQuery = searchQuery,
                        isSearching = isSearching,
                        searchResults = searchResults,
                        searchError = searchError,
                        popularPresetCities = popularPresetCities,
                        activeCityName = preferences.activeCityName,
                        onSelectLocation = onSelectLocation,
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                ActiveLocationCard(preferences = preferences)

                Spacer(modifier = Modifier.height(WeatheraSpacing.md))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 84.dp),
                ) {
                    LocationResultsList(
                        searchQuery = searchQuery,
                        isSearching = isSearching,
                        searchResults = searchResults,
                        searchError = searchError,
                        popularPresetCities = popularPresetCities,
                        activeCityName = preferences.activeCityName,
                        onSelectLocation = onSelectLocation,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveLocationCard(preferences: UserPreferences) {
    WeatherCard(
        borderColor = Primary.copy(alpha = 0.8f),
        borderWidth = 1.5.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Primary.copy(alpha = 0.12f), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    LocationPinIcon(color = Primary, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(WeatheraSpacing.md))

                Column {
                    Text(
                        text = stringResource(R.string.active_location_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${preferences.activeCityName}, ${preferences.activeCountry}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Primary,
            ) {
                Text(
                    text = stringResource(R.string.currently_active),
                    style = MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = WeatheraSpacing.md, vertical = WeatheraSpacing.xs),
                )
            }
        }
    }
}

@Composable
private fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            SearchHeaderIcon(color = Primary, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Surface(
                    onClick = onClear,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(24.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
        shape = RoundedCornerShape(WeatheraCornerRadius.Medium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun LocationResultsList(
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<SearchResultLocation>,
    searchError: String?,
    popularPresetCities: List<SearchResultLocation>,
    activeCityName: String,
    onSelectLocation: (SearchResultLocation) -> Unit,
) {
    when {
        isSearching -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                WeatherLoadingIndicator(
                    message = stringResource(R.string.searching_location),
                )
            }
        }
        searchQuery.isNotBlank() -> {
            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.search_results_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = WeatheraSpacing.xs),
                        )
                    }
                    items(searchResults, key = { it.id }) { item ->
                        SearchResultItemCard(
                            location = item,
                            isActive = (item.cityName.equals(activeCityName, ignoreCase = true)),
                            onSelect = { onSelectLocation(item) },
                        )
                    }
                }
            } else {
                WeatherEmptyState(
                    title = "Kota Tidak Ditemukan",
                    description = stringResource(R.string.no_results_found, searchQuery),
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
            ) {
                item {
                    Text(
                        text = "Kota Populer / Preset",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = WeatheraSpacing.xs),
                    )
                }
                items(popularPresetCities, key = { it.id }) { item ->
                    SearchResultItemCard(
                        location = item,
                        isActive = (item.cityName.equals(activeCityName, ignoreCase = true)),
                        onSelect = { onSelectLocation(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItemCard(
    location: SearchResultLocation,
    isActive: Boolean,
    onSelect: () -> Unit,
) {
    WeatherCard(
        onClick = onSelect,
        borderColor = if (isActive) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        contentPadding = WeatheraSpacing.md,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val regionText = listOfNotNull(location.adminRegion, location.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                Text(
                    text = regionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isActive) {
                Surface(
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = stringResource(R.string.currently_active),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = WeatheraSpacing.md, vertical = WeatheraSpacing.xs),
                    )
                }
            } else {
                WeatherPrimaryButton(
                    text = stringResource(R.string.select_as_active),
                    onClick = onSelect,
                )
            }
        }
    }
}
