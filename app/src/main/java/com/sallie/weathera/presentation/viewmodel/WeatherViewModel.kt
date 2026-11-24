package com.sallie.weathera.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.data.local.UserPreferencesRepository
import com.sallie.weathera.data.repository.WeatherRepository
import com.sallie.weathera.data.repository.WeatherRepositoryImpl
import com.sallie.weathera.domain.model.CurrentWeather
import com.sallie.weathera.domain.model.DailyForecastItem
import com.sallie.weathera.domain.model.HourlyForecastItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val currentWeather: CurrentWeather,
        val hourlyForecast: List<HourlyForecastItem>,
        val dailyForecast: List<DailyForecastItem>,
        val isRefreshing: Boolean = false,
    ) : WeatherUiState

    data class Error(
        val message: String,
        val isOffline: Boolean = false,
        val previousData: Success? = null,
    ) : WeatherUiState
}

class WeatherViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: WeatherRepository = WeatherRepositoryImpl(),
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(application),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentPreferences = UserPreferences()

    init {
        viewModelScope.launch {
            try {
                preferencesRepository.userPreferencesFlow.collect { prefs ->
                    currentPreferences = prefs
                    fetchWeatherData(
                        isRefresh = false,
                        cityName = prefs.activeCityName,
                        country = prefs.activeCountry,
                        latitude = prefs.activeLatitude,
                        longitude = prefs.activeLongitude,
                    )
                }
            } catch (_: Exception) {
                fetchWeatherData(isRefresh = false)
            }
        }
    }

    fun fetchWeatherData(
        isRefresh: Boolean = false,
        cityName: String = currentPreferences.activeCityName,
        country: String = currentPreferences.activeCountry,
        latitude: Double = currentPreferences.activeLatitude,
        longitude: Double = currentPreferences.activeLongitude,
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (isRefresh) {
                if (currentState is WeatherUiState.Success) {
                    _uiState.value = currentState.copy(isRefreshing = true)
                }
            } else {
                if (currentState !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Loading
                }
            }

            try {
                val result = repository.getWeatherForecast(
                    latitude = latitude,
                    longitude = longitude,
                    cityName = cityName,
                    country = country,
                )
                _uiState.value = WeatherUiState.Success(
                    currentWeather = result.currentWeather,
                    hourlyForecast = result.hourlyForecast,
                    dailyForecast = result.dailyForecast,
                    isRefreshing = false,
                )
            } catch (e: Exception) {
                val isOffline = (e is UnknownHostException || e is ConnectException)
                val errorMessage = when {
                    isOffline -> "Tidak ada koneksi internet. Silakan periksa jaringan Anda dan coba lagi."
                    e is IOException -> "Gagal terhubung ke server cuaca. Silakan coba lagi."
                    else -> e.localizedMessage ?: "Terjadi kesalahan saat memuat data cuaca."
                }

                val prevData = when (currentState) {
                    is WeatherUiState.Success -> currentState.copy(isRefreshing = false)
                    is WeatherUiState.Error -> currentState.previousData
                    else -> null
                }

                _uiState.value = WeatherUiState.Error(
                    message = errorMessage,
                    isOffline = isOffline,
                    previousData = prevData,
                )
            }
        }
    }

    fun retry() {
        fetchWeatherData(isRefresh = false)
    }

    fun refresh() {
        fetchWeatherData(isRefresh = true)
    }
}
