package com.sallie.weathera.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String?,
    @SerializedName("current") val current: CurrentDto?,
    @SerializedName("hourly") val hourly: HourlyDto?,
    @SerializedName("daily") val daily: DailyDto?,
)

data class CurrentDto(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature2m: Double?,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: Int?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("is_day") val isDay: Int?,
    @SerializedName("precipitation") val precipitation: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double?,
    @SerializedName("wind_direction_10m") val windDirection10m: Double?,
    @SerializedName("wind_gusts_10m") val windGusts10m: Double?,
    @SerializedName("dew_point_2m") val dewPoint2m: Double?,
)

data class HourlyDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m") val temperature2m: List<Double>?,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double>?,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: List<Int>?,
    @SerializedName("dew_point_2m") val dewPoint2m: List<Double>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>?,
    @SerializedName("precipitation") val precipitation: List<Double>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("wind_speed_10m") val windSpeed10m: List<Double>?,
    @SerializedName("uv_index") val uvIndex: List<Double>?,
)

data class DailyDto(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int>?,
    @SerializedName("temperature_2m_max") val temperature2mMax: List<Double>?,
    @SerializedName("temperature_2m_min") val temperature2mMin: List<Double>?,
    @SerializedName("sunrise") val sunrise: List<String>?,
    @SerializedName("sunset") val sunset: List<String>?,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>?,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>?,
)
