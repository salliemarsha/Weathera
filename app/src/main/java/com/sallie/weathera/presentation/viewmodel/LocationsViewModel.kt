package com.sallie.weathera.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sallie.weathera.data.local.GpsErrorReason
import com.sallie.weathera.data.local.GpsResult
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.data.local.UserPreferencesRepository
import com.sallie.weathera.data.local.getCurrentLocation
import com.sallie.weathera.data.repository.LocationRepository
import com.sallie.weathera.data.repository.LocationRepositoryImpl
import com.sallie.weathera.domain.model.SearchResultLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State of the GPS / Current Location request. */
sealed interface GpsUiState {
    data object Idle : GpsUiState
    data object Loading : GpsUiState
    data class PermissionRequired(val shouldShowRationale: Boolean) : GpsUiState
    data class LocationDisabled(val dummy: Unit = Unit) : GpsUiState
    data class LocationError(val reason: GpsErrorReason) : GpsUiState
    data object Success : GpsUiState
}

class LocationsViewModel @JvmOverloads constructor(
    application: Application,
    private val locationRepository: LocationRepository = LocationRepositoryImpl(),
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(application),
) : AndroidViewModel(application) {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences(),
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultLocation>>(emptyList())
    val searchResults: StateFlow<List<SearchResultLocation>> = _searchResults.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // GPS state exposed to UI
    private val _gpsState = MutableStateFlow<GpsUiState>(GpsUiState.Idle)
    val gpsState: StateFlow<GpsUiState> = _gpsState.asStateFlow()

    private var searchJob: Job? = null

    // ----------------------------------------------------------------
    // Search
    // ----------------------------------------------------------------

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        _searchError.value = null

        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            performSearch(newQuery)
        }
    }

    fun onSearchClicked() {
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            searchJob?.cancel()
            viewModelScope.launch { performSearch(query) }
        }
    }

    fun onClearQuery() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _searchError.value = null
        _isSearching.value = false
    }

    // ----------------------------------------------------------------
    // Location selection
    // ----------------------------------------------------------------

    fun selectLocation(location: SearchResultLocation, onSelected: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.addSavedLocation(location)
            preferencesRepository.updateActiveLocation(
                cityName = location.cityName,
                country = location.country,
                latitude = location.latitude,
                longitude = location.longitude,
            )
            onSelected()
        }
    }

    fun removeLocation(locationId: String) {
        viewModelScope.launch {
            preferencesRepository.removeSavedLocation(locationId)
        }
    }

    // ----------------------------------------------------------------
    // GPS / Current Location
    // ----------------------------------------------------------------

    /**
     * Called by the UI when the user has tapped "Use My Location" and the
     * runtime permission check has already been handled.
     *
     * [permissionGranted] – true when at least COARSE permission was granted.
     * [shouldShowRationale] – forwarded from ActivityCompat.shouldShowRequestPermissionRationale.
     * [onSelected] – callback invoked after the active location is updated successfully.
     */
    fun useCurrentLocation(
        permissionGranted: Boolean,
        shouldShowRationale: Boolean = false,
        onSelected: () -> Unit = {},
    ) {
        if (!permissionGranted) {
            _gpsState.value = GpsUiState.PermissionRequired(shouldShowRationale)
            return
        }

        viewModelScope.launch {
            _gpsState.value = GpsUiState.Loading
            val context = getApplication<Application>().applicationContext
            when (val result = getCurrentLocation(context)) {
                is GpsResult.Success -> {
                    // Build a SearchResultLocation so it integrates with existing flows
                    val loc = SearchResultLocation(
                        id = "gps_current",
                        cityName = result.cityName,
                        country = result.country,
                        adminRegion = null,
                        latitude = result.latitude,
                        longitude = result.longitude,
                        timezone = null,
                    )
                    // Save and set active (same as manual city selection)
                    preferencesRepository.addSavedLocation(loc)
                    preferencesRepository.updateActiveLocation(
                        cityName = result.cityName,
                        country = result.country,
                        latitude = result.latitude,
                        longitude = result.longitude,
                    )
                    _gpsState.value = GpsUiState.Success
                    onSelected()
                }
                is GpsResult.Error -> {
                    _gpsState.value = when (result.reason) {
                        GpsErrorReason.LOCATION_DISABLED -> GpsUiState.LocationDisabled()
                        GpsErrorReason.PERMISSION_DENIED ->
                            GpsUiState.PermissionRequired(shouldShowRationale = false)
                        else -> GpsUiState.LocationError(result.reason)
                    }
                }
            }
        }
    }

    /** Reset GPS state so the UI returns to Idle (dismisses any error). */
    fun clearGpsState() {
        _gpsState.value = GpsUiState.Idle
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private suspend fun performSearch(query: String) {
        _isSearching.value = true
        _searchError.value = null
        try {
            val language = userPreferences.value.languageCode
            val results = locationRepository.searchCity(query, language = language)
            _searchResults.value = results
            if (results.isEmpty()) {
                _searchError.value = "no_results"
            }
        } catch (e: Exception) {
            _searchError.value = e.localizedMessage ?: "Search failed"
            _searchResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }
}
