package com.sallie.weathera.data.repository

import com.sallie.weathera.data.remote.GeocodingApiService
import com.sallie.weathera.data.remote.RetrofitClient
import com.sallie.weathera.domain.model.SearchResultLocation

interface LocationRepository {
    suspend fun searchCity(query: String, language: String = "en"): List<SearchResultLocation>
}

class LocationRepositoryImpl(
    private val apiService: GeocodingApiService = RetrofitClient.geocodingApiService,
) : LocationRepository {
    override suspend fun searchCity(query: String, language: String): List<SearchResultLocation> {
        if (query.isBlank()) return emptyList()
        val dto = apiService.searchLocation(name = query.trim(), language = language)
        val results = dto.results ?: return emptyList()

        return results
            .filter { it.name != null && it.latitude != null && it.longitude != null }
            .distinctBy { "${it.name}-${it.country}-${it.admin1}" }
            .map { dtoLocation ->
                SearchResultLocation(
                    id = dtoLocation.id?.toString() ?: "loc_${dtoLocation.latitude}_${dtoLocation.longitude}_${System.nanoTime()}",
                    cityName = dtoLocation.name ?: "",
                    country = dtoLocation.country ?: "",
                    adminRegion = dtoLocation.admin1,
                    latitude = dtoLocation.latitude ?: 0.0,
                    longitude = dtoLocation.longitude ?: 0.0,
                    timezone = dtoLocation.timezone,
                )
            }
    }
}
