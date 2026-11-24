package com.sallie.weathera.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.data.local.UserPreferencesRepository
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

    private var searchJob: Job? = null

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
            delay(500) // Debounce 500ms
            performSearch(newQuery)
        }
    }

    fun onSearchClicked() {
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            searchJob?.cancel()
            viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    fun onClearQuery() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _searchError.value = null
        _isSearching.value = false
    }

    fun selectLocation(location: SearchResultLocation, onSelected: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.updateActiveLocation(
                cityName = location.cityName,
                country = location.country,
                latitude = location.latitude,
                longitude = location.longitude,
            )
            onSelected()
        }
    }

    private suspend fun performSearch(query: String) {
        _isSearching.value = true
        _searchError.value = null
        try {
            val language = userPreferences.value.languageCode
            val results = locationRepository.searchCity(query, language = language)
            _searchResults.value = results
            if (results.isEmpty()) {
                _searchError.value = "Kota tidak ditemukan"
            }
        } catch (e: Exception) {
            _searchError.value = e.localizedMessage ?: "Gagal mencari kota"
            _searchResults.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }
}
