package com.sallie.weathera.presentation.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sallie.weathera.R
import com.sallie.weathera.data.local.GpsErrorReason
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.domain.model.SearchResultLocation
import com.sallie.weathera.presentation.components.LocationPinIcon
import com.sallie.weathera.presentation.components.SearchHeaderIcon
import com.sallie.weathera.presentation.components.WeatherCard
import com.sallie.weathera.presentation.components.WeatherEmptyState
import com.sallie.weathera.presentation.components.WeatherLoadingIndicator
import com.sallie.weathera.presentation.viewmodel.GpsUiState
import com.sallie.weathera.presentation.viewmodel.LocationsViewModel
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing

// ─────────────────────────────────────────────────────────────
// Screen entry point
// ─────────────────────────────────────────────────────────────

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
    val gpsState by viewModel.gpsState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // ── Permission state tracking ──────────────────────────────
    // rationaleShown prevents infinite rationale-dialog loops.
    var rationaleShown by rememberSaveable { mutableStateOf(false) }

    // ── Permission launcher ────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        val activity = context as? androidx.activity.ComponentActivity
        val showRationale = activity != null &&
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION,
            )

        viewModel.useCurrentLocation(
            permissionGranted = granted,
            shouldShowRationale = showRationale,
            onSelected = { onNavigateToHome() },
        )
    }

    // ── "Use My Location" tap handler ─────────────────────────
    val onUseCurrentLocation: () -> Unit = {
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (coarse || fine) {
            viewModel.useCurrentLocation(
                permissionGranted = true,
                onSelected = { onNavigateToHome() },
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }
    }

    // ── GPS state → dialogs ────────────────────────────────────
    when (val state = gpsState) {
        is GpsUiState.PermissionRequired -> {
            if (state.shouldShowRationale && !rationaleShown) {
                rationaleShown = true
                AlertDialog(
                    onDismissRequest = { viewModel.clearGpsState() },
                    title = { Text(stringResource(R.string.use_current_location)) },
                    text = { Text(stringResource(R.string.location_permission_rationale)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearGpsState()
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                )
                            )
                        }) { Text(stringResource(R.string.retry)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.clearGpsState() }) {
                            Text(stringResource(R.string.clear))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (!state.shouldShowRationale && rationaleShown) {
                // Permanently denied after rationale was shown — guide to settings
                AlertDialog(
                    onDismissRequest = { viewModel.clearGpsState() },
                    title = { Text(stringResource(R.string.use_current_location)) },
                    text = { Text(stringResource(R.string.location_permission_denied_permanent)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearGpsState()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                if (context !is android.app.Activity) {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            }
                            context.startActivity(intent)
                        }) { Text(stringResource(R.string.open_settings)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.clearGpsState() }) {
                            Text(stringResource(R.string.clear))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // First denial without rationale — just dismiss quietly
                viewModel.clearGpsState()
            }
        }
        is GpsUiState.LocationDisabled -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearGpsState() },
                title = { Text(stringResource(R.string.use_current_location)) },
                text = { Text(stringResource(R.string.location_disabled)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearGpsState()
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                            if (context !is android.app.Activity) {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        }
                        context.startActivity(intent)
                    }) { Text(stringResource(R.string.open_settings)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.clearGpsState() }) {
                        Text(stringResource(R.string.clear))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        is GpsUiState.LocationError -> {
            val msg = when (state.reason) {
                GpsErrorReason.TIMEOUT -> stringResource(R.string.location_timeout)
                else -> stringResource(R.string.location_error)
            }
            AlertDialog(
                onDismissRequest = { viewModel.clearGpsState() },
                title = { Text(stringResource(R.string.use_current_location)) },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearGpsState()
                        onUseCurrentLocation()
                    }) { Text(stringResource(R.string.retry)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.clearGpsState() }) {
                        Text(stringResource(R.string.clear))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> Unit
    }

    LocationsScreenContent(
        searchQuery = searchQuery,
        isSearching = isSearching,
        searchResults = searchResults,
        searchError = searchError,
        preferences = preferences,
        gpsState = gpsState,
        onQueryChange = { viewModel.onQueryChanged(it) },
        onSearchSubmit = { viewModel.onSearchClicked() },
        onClear = { viewModel.onClearQuery() },
        onSelectLocation = { location ->
            viewModel.selectLocation(location) { onNavigateToHome() }
        },
        onDeleteLocation = { locationId -> viewModel.removeLocation(locationId) },
        onUseCurrentLocation = onUseCurrentLocation,
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────
// Content composable (pure, no side effects)
// ─────────────────────────────────────────────────────────────

@Composable
fun LocationsScreenContent(
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<SearchResultLocation>,
    searchError: String?,
    preferences: UserPreferences,
    gpsState: GpsUiState = GpsUiState.Idle,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClear: () -> Unit,
    onSelectLocation: (SearchResultLocation) -> Unit,
    onDeleteLocation: (String) -> Unit = {},
    onUseCurrentLocation: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isGpsLoading = gpsState is GpsUiState.Loading
    val savedCities = preferences.savedLocations

    if (isLandscape) {
        // ── Landscape: two-column layout ───────────────────────
        // Left column: header + search + GPS button + active card (scrollable)
        // Right column: results list
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = WeatheraSpacing.lg)
                .padding(top = WeatheraSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.lg),
        ) {
            // Left column
            Column(
                modifier = Modifier
                    .weight(0.42f)
                    .padding(bottom = 84.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.locations_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.locations_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
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
                CurrentLocationButton(
                    isLoading = isGpsLoading,
                    onClick = {
                        focusManager.clearFocus()
                        onUseCurrentLocation()
                    },
                )
                Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
                ActiveLocationCard(preferences = preferences)
            }

            // Right column — results list
            Box(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .padding(bottom = 84.dp),
            ) {
                LocationResultsList(
                    searchQuery = searchQuery,
                    isSearching = isSearching,
                    searchResults = searchResults,
                    savedCities = savedCities,
                    activeCityName = preferences.activeCityName,
                    onSelectLocation = onSelectLocation,
                    onDeleteLocation = onDeleteLocation,
                )
            }
        }
    } else {
        // ── Portrait: single-column layout ─────────────────────
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = WeatheraSpacing.lg)
                .padding(top = WeatheraSpacing.lg),
        ) {
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

            Spacer(modifier = Modifier.height(WeatheraSpacing.md))

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

            Spacer(modifier = Modifier.height(WeatheraSpacing.sm))

            CurrentLocationButton(
                isLoading = isGpsLoading,
                onClick = {
                    focusManager.clearFocus()
                    onUseCurrentLocation()
                },
            )

            Spacer(modifier = Modifier.height(WeatheraSpacing.md))

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
                    savedCities = savedCities,
                    activeCityName = preferences.activeCityName,
                    onSelectLocation = onSelectLocation,
                    onDeleteLocation = onDeleteLocation,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Current Location Button
// Fixed: replaced WeatherLoadingIndicator(size=20dp) with
// inline CircularProgressIndicator to avoid layout constraint crash.
// ─────────────────────────────────────────────────────────────

@Composable
private fun CurrentLocationButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = if (isLoading) ({}) else onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WeatheraCornerRadius.Medium),
        color = Primary.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WeatheraSpacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                // ✅ Inline indicator — avoids WeatherLoadingIndicator's fillMaxWidth + xl-padding
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Primary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(WeatheraSpacing.sm))
                Text(
                    text = stringResource(R.string.locating_device),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                )
            } else {
                LocationPinIcon(color = Primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(WeatheraSpacing.sm))
                Text(
                    text = stringResource(R.string.use_current_location),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Active Location Card
// ─────────────────────────────────────────────────────────────

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
            Surface(shape = CircleShape, color = Primary) {
                Text(
                    text = stringResource(R.string.currently_active),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = WeatheraSpacing.md,
                        vertical = WeatheraSpacing.xs,
                    ),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Search Bar
// ─────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────
// Results List
// ─────────────────────────────────────────────────────────────

@Composable
private fun LocationResultsList(
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<SearchResultLocation>,
    savedCities: List<SearchResultLocation>,
    activeCityName: String,
    onSelectLocation: (SearchResultLocation) -> Unit,
    onDeleteLocation: (String) -> Unit,
) {
    when {
        isSearching -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WeatherLoadingIndicator(message = stringResource(R.string.searching_location))
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
                    items(searchResults, key = { "${it.id}_${it.latitude}_${it.longitude}" }) { item ->
                        SearchResultItemCard(
                            location = item,
                            isActive = item.cityName.equals(activeCityName, ignoreCase = true),
                            onSelect = { onSelectLocation(item) },
                        )
                    }
                }
            } else {
                WeatherEmptyState(
                    title = stringResource(R.string.city_not_found),
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
                        text = stringResource(R.string.saved_cities),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = WeatheraSpacing.xs),
                    )
                }
                items(savedCities, key = { "${it.id}_${it.latitude}_${it.longitude}" }) { item ->
                    val isActive = item.cityName.equals(activeCityName, ignoreCase = true)
                    SearchResultItemCard(
                        location = item,
                        isActive = isActive,
                        onSelect = { onSelectLocation(item) },
                        onDelete = if (!isActive) ({ onDeleteLocation(item.id) }) else null,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Search Result Item Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun SearchResultItemCard(
    location: SearchResultLocation,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null,
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
                if (regionText.isNotBlank()) {
                    Text(
                        text = regionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WeatheraSpacing.sm))

            if (isActive) {
                Surface(shape = CircleShape, color = Primary.copy(alpha = 0.15f)) {
                    Text(
                        text = stringResource(R.string.currently_active),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = WeatheraSpacing.md,
                            vertical = WeatheraSpacing.xs,
                        ),
                    )
                }
            } else {
                // Whole card is already clickable (onSelect). Only show delete button here.
                if (onDelete != null) {
                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✕",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                } else {
                    // No delete (e.g. search result) — show "Select" pill to hint interactivity
                    Surface(
                        shape = RoundedCornerShape(WeatheraCornerRadius.Small),
                        color = Primary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = stringResource(R.string.select_as_active),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                horizontal = WeatheraSpacing.sm,
                                vertical = WeatheraSpacing.xs,
                            ),
                        )
                    }
                }
            }
        }
    }
}
