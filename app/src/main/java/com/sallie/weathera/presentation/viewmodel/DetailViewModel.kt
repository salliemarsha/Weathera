package com.sallie.weathera.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sallie.weathera.data.local.UserPreferencesRepository
import com.sallie.weathera.data.repository.DetailRepository
import com.sallie.weathera.data.repository.DetailRepositoryImpl
import com.sallie.weathera.domain.model.AirQualityDetail
import com.sallie.weathera.domain.model.FeelsLikeDetail
import com.sallie.weathera.domain.model.HumidityDetail
import com.sallie.weathera.domain.model.PrecipitationDetail
import com.sallie.weathera.domain.model.UvDetail
import com.sallie.weathera.domain.model.WindDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Which metric card is currently open (null = none). */
enum class DetailType { AIR_QUALITY, UV_INDEX, WIND, PRECIPITATION, FEELS_LIKE, HUMIDITY }

/** Generic loading/success/error wrapper for each detail request. */
sealed interface DetailUiState<out T> {
    data object Idle : DetailUiState<Nothing>
    data object Loading : DetailUiState<Nothing>
    data class Success<T>(val data: T) : DetailUiState<T>
    data class Error(val message: String) : DetailUiState<Nothing>
}

class DetailViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DetailRepository = DetailRepositoryImpl(),
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(application),
) : AndroidViewModel(application) {

    private val _openDetail = MutableStateFlow<DetailType?>(null)
    val openDetail: StateFlow<DetailType?> = _openDetail.asStateFlow()

    private val _airQuality = MutableStateFlow<DetailUiState<AirQualityDetail>>(DetailUiState.Idle)
    val airQuality: StateFlow<DetailUiState<AirQualityDetail>> = _airQuality.asStateFlow()

    private val _uv = MutableStateFlow<DetailUiState<UvDetail>>(DetailUiState.Idle)
    val uv: StateFlow<DetailUiState<UvDetail>> = _uv.asStateFlow()

    private val _wind = MutableStateFlow<DetailUiState<WindDetail>>(DetailUiState.Idle)
    val wind: StateFlow<DetailUiState<WindDetail>> = _wind.asStateFlow()

    private val _precipitation = MutableStateFlow<DetailUiState<PrecipitationDetail>>(DetailUiState.Idle)
    val precipitation: StateFlow<DetailUiState<PrecipitationDetail>> = _precipitation.asStateFlow()

    private val _feelsLike = MutableStateFlow<DetailUiState<FeelsLikeDetail>>(DetailUiState.Idle)
    val feelsLike: StateFlow<DetailUiState<FeelsLikeDetail>> = _feelsLike.asStateFlow()

    private val _humidity = MutableStateFlow<DetailUiState<HumidityDetail>>(DetailUiState.Idle)
    val humidity: StateFlow<DetailUiState<HumidityDetail>> = _humidity.asStateFlow()

    fun openDetail(type: DetailType, currentWeather: com.sallie.weathera.domain.model.CurrentWeather) {
        _openDetail.value = type
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val lat = prefs.activeLatitude
            val lon = prefs.activeLongitude
            val lang = prefs.languageCode
            when (type) {
                DetailType.AIR_QUALITY -> loadAirQuality(lat, lon, lang)
                DetailType.UV_INDEX    -> loadUv(lat, lon, currentWeather.uvIndex, lang)
                DetailType.WIND        -> loadWind(lat, lon, currentWeather, lang)
                DetailType.PRECIPITATION -> loadPrecipitation(lat, lon, currentWeather, lang)
                DetailType.FEELS_LIKE  -> loadFeelsLike(lat, lon, currentWeather, lang)
                DetailType.HUMIDITY    -> loadHumidity(lat, lon, currentWeather.humidity, lang)
            }
        }
    }

    fun closeDetail() {
        _openDetail.value = null
    }

    private suspend fun loadAirQuality(lat: Double, lon: Double, lang: String) {
        _airQuality.value = DetailUiState.Loading
        _airQuality.value = try {
            DetailUiState.Success(repository.getAirQualityDetail(lat, lon, lang))
        } catch (e: Exception) {
            DetailUiState.Error(e.localizedMessage ?: "Failed to load air quality data")
        }
    }

    private suspend fun loadUv(lat: Double, lon: Double, currentUv: Int, lang: String) {
        _uv.value = DetailUiState.Loading
        _uv.value = try {
            DetailUiState.Success(repository.getUvDetail(lat, lon, currentUv, lang))
        } catch (e: Exception) {
            DetailUiState.Error(e.localizedMessage ?: "Failed to load UV data")
        }
    }

    private suspend fun loadWind(lat: Double, lon: Double, w: com.sallie.weathera.domain.model.CurrentWeather, lang: String) {
        _wind.value = DetailUiState.Loading
        _wind.value = try {
            DetailUiState.Success(repository.getWindDetail(lat, lon, w.windInfo.speedKmh, w.windInfo.gustsKmh, w.windInfo.angleDegree, w.windInfo.direction, lang))
        } catch (e: Exception) {
            DetailUiState.Error(e.localizedMessage ?: "Failed to load wind data")
        }
    }

    private suspend fun loadPrecipitation(lat: Double, lon: Double, w: com.sallie.weathera.domain.model.CurrentWeather, lang: String) {
        _precipitation.value = DetailUiState.Loading
        _precipitation.value = try {
            DetailUiState.Success(repository.getPrecipitationDetail(lat, lon, w.rainfallInfo.lastHourMm, w.rainfallInfo.probability, lang))
        } catch (e: Exception) {
            DetailUiState.Error(e.localizedMessage ?: "Failed to load precipitation data")
        }
    }

    private suspend fun loadFeelsLike(lat: Double, lon: Double, w: com.sallie.weathera.domain.model.CurrentWeather, lang: String) {
        _feelsLike.value = DetailUiState.Loading
        _feelsLike.value = try {
            DetailUiState.Success(repository.getFeelsLikeDetail(lat, lon, w.temperature, w.feelsLike, w.humidity, w.windInfo.speedKmh, lang))
        } catch (e: Exception) {
            DetailUiState.Error(e.localizedMessage ?: "Failed to load feels like data")
        }
    }

    private suspend fun loadHumidity(lat: Double, lon: Double, humidity: Int, lang: String) {
        _humidity.value = DetailUiState.Loading
        _humidity.value = try {
            DetailUiState.Success(repository.getHumidityDetail(lat, lon, humidity, lang))
        } catch (e: Exception) {
            DetailUiState.Error(e.localizedMessage ?: "Failed to load humidity data")
        }
    }
}
