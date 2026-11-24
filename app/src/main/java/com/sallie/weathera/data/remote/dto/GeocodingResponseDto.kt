package com.sallie.weathera.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeocodingResponseDto(
    @SerializedName("results") val results: List<GeocodingLocationDto>?,
)

data class GeocodingLocationDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("country") val country: String?,
    @SerializedName("admin1") val admin1: String?,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("timezone") val timezone: String?,
)
