package com.sallie.weathera.domain.model

data class SearchResultLocation(
    val id: String,
    val cityName: String,
    val country: String,
    val adminRegion: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
)
