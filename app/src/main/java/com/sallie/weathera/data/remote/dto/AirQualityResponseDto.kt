package com.sallie.weathera.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AirQualityResponseDto(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("current") val current: AirQualityCurrentDto?,
    @SerializedName("hourly") val hourly: AirQualityHourlyDto?,
)

data class AirQualityCurrentDto(
    @SerializedName("time") val time: String?,
    @SerializedName("pm2_5") val pm25: Double?,
    @SerializedName("pm10") val pm10: Double?,
    @SerializedName("ozone") val ozone: Double?,
    @SerializedName("carbon_monoxide") val carbonMonoxide: Double?,
    @SerializedName("nitrogen_dioxide") val nitrogenDioxide: Double?,
    @SerializedName("sulphur_dioxide") val sulphurDioxide: Double?,
    @SerializedName("european_aqi") val europeanAqi: Int?,
    @SerializedName("us_aqi") val usAqi: Int?,
)

data class AirQualityHourlyDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("pm2_5") val pm25: List<Double?>?,
    @SerializedName("pm10") val pm10: List<Double?>?,
    @SerializedName("ozone") val ozone: List<Double?>?,
    @SerializedName("us_aqi") val usAqi: List<Int?>?,
)
