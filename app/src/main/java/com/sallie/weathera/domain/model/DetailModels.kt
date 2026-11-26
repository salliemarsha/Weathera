package com.sallie.weathera.domain.model

/**
 * Rich detail models used by the weather metric detail overlay.
 * These are loaded on demand when a user taps a metric card.
 */

// ─────────────────────────────────────────────────────────────
// Air Quality
// ─────────────────────────────────────────────────────────────

data class AirQualityDetail(
    val usAqi: Int,
    val europeanAqi: Int,
    val category: String,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double,
    val carbonMonoxide: Double,
    val nitrogenDioxide: Double,
    val sulphurDioxide: Double,
    val hourlyTimes: List<String>,
    val hourlyUsAqi: List<Int>,
    val recommendation: String,
)

// ─────────────────────────────────────────────────────────────
// UV Index
// ─────────────────────────────────────────────────────────────

data class UvDetail(
    val currentUvIndex: Int,
    val maxUvIndex: Int,
    val category: String,
    val recommendation: String,
    val hourlyTimes: List<String>,   // display strings, e.g. "14:00"
    val hourlyUvIndex: List<Double>, // raw UV values per hour
)

// ─────────────────────────────────────────────────────────────
// Wind
// ─────────────────────────────────────────────────────────────

data class WindDetail(
    val speedKmh: Float,
    val gustsKmh: Float,
    val directionDeg: Float,
    val directionCardinal: String,
    val hourlyTimes: List<String>,
    val hourlySpeedKmh: List<Double>,
)

// ─────────────────────────────────────────────────────────────
// Precipitation
// ─────────────────────────────────────────────────────────────

data class PrecipitationDetail(
    val currentMm: Float,
    val probabilityPct: Int,
    val dailySumMm: Float,
    val hourlyTimes: List<String>,
    val hourlyMm: List<Double>,
    val hourlyProbability: List<Int>,
)

// ─────────────────────────────────────────────────────────────
// Feels Like
// ─────────────────────────────────────────────────────────────

data class FeelsLikeDetail(
    val actualTemp: Int,
    val feelsLikeTemp: Int,
    val diffCelsius: Int,
    val humidity: Int,
    val windSpeedKmh: Float,
    val hourlyTimes: List<String>,
    val hourlyActual: List<Int>,
    val hourlyFeelsLike: List<Int>,
)

// ─────────────────────────────────────────────────────────────
// Humidity
// ─────────────────────────────────────────────────────────────

data class HumidityDetail(
    val humidity: Int,
    val dewPointC: Double,
    val hourlyTimes: List<String>,
    val hourlyHumidity: List<Int>,
    val humidityCategory: String,
)
