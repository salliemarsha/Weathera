package com.sallie.weathera.data.repository

import com.sallie.weathera.data.remote.AirQualityApiService
import com.sallie.weathera.data.remote.RetrofitClient
import com.sallie.weathera.data.remote.WeatherApiService
import com.sallie.weathera.domain.model.AirQualityDetail
import com.sallie.weathera.domain.model.FeelsLikeDetail
import com.sallie.weathera.domain.model.HumidityDetail
import com.sallie.weathera.domain.model.PrecipitationDetail
import com.sallie.weathera.domain.model.UvDetail
import com.sallie.weathera.domain.model.WindDetail
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Fetches extended weather data for metric detail screens.
 * Uses the same Open-Meteo forecast endpoint (with extended variables)
 * and the separate air-quality endpoint.
 *
 * All methods return their domain model directly — callers wrap in try/catch.
 */
interface DetailRepository {
    suspend fun getAirQualityDetail(latitude: Double, longitude: Double, languageCode: String = "in"): AirQualityDetail
    suspend fun getUvDetail(latitude: Double, longitude: Double, currentUvIndex: Int, languageCode: String = "in"): UvDetail
    suspend fun getWindDetail(latitude: Double, longitude: Double, speedKmh: Float, gustsKmh: Float, dirDeg: Float, dirCardinal: String, languageCode: String = "in"): WindDetail
    suspend fun getPrecipitationDetail(latitude: Double, longitude: Double, currentMm: Float, probabilityPct: Int, languageCode: String = "in"): PrecipitationDetail
    suspend fun getFeelsLikeDetail(latitude: Double, longitude: Double, actualTemp: Int, feelsLike: Int, humidity: Int, windSpeed: Float, languageCode: String = "in"): FeelsLikeDetail
    suspend fun getHumidityDetail(latitude: Double, longitude: Double, humidity: Int, languageCode: String = "in"): HumidityDetail
}

class DetailRepositoryImpl(
    private val weatherApi: WeatherApiService = RetrofitClient.weatherApiService,
    private val airQualityApi: AirQualityApiService = RetrofitClient.airQualityApiService,
) : DetailRepository {

    // ─────────────────────────────────────────────────────────
    // Air Quality — uses separate air-quality API
    // ─────────────────────────────────────────────────────────

    override suspend fun getAirQualityDetail(latitude: Double, longitude: Double, languageCode: String): AirQualityDetail {
        val dto = airQualityApi.getAirQuality(latitude, longitude)
        val c = dto.current

        val usAqi = c?.usAqi ?: 0
        val euAqi = c?.europeanAqi ?: 0
        val category = aqiCategory(usAqi, languageCode)
        val recommendation = aqiRecommendation(usAqi, languageCode)

        // Hourly — take first 24 entries aligned to current time
        val times = dto.hourly?.time?.take(24) ?: emptyList()
        val hourlyAqi = dto.hourly?.usAqi?.take(24)?.map { it ?: 0 } ?: emptyList()
        val displayTimes = times.map { formatHourOnly(it) }

        return AirQualityDetail(
            usAqi = usAqi,
            europeanAqi = euAqi,
            category = category,
            pm25 = c?.pm25 ?: 0.0,
            pm10 = c?.pm10 ?: 0.0,
            ozone = c?.ozone ?: 0.0,
            carbonMonoxide = c?.carbonMonoxide ?: 0.0,
            nitrogenDioxide = c?.nitrogenDioxide ?: 0.0,
            sulphurDioxide = c?.sulphurDioxide ?: 0.0,
            hourlyTimes = displayTimes,
            hourlyUsAqi = hourlyAqi,
            recommendation = recommendation,
        )
    }

    // ─────────────────────────────────────────────────────────
    // UV — uses forecast/hourly uv_index
    // ─────────────────────────────────────────────────────────

    override suspend fun getUvDetail(latitude: Double, longitude: Double, currentUvIndex: Int, languageCode: String): UvDetail {
        val dto = weatherApi.getForecast(latitude = latitude, longitude = longitude)
        val maxUv = dto.daily?.uvIndexMax?.firstOrNull()?.roundToInt() ?: currentUvIndex
        val hourly = dto.hourly
        val times = hourly?.time ?: emptyList()
        val uvValues = hourly?.uvIndex ?: emptyList()

        val pairs = alignHourly(times, uvValues, limit = 24)

        return UvDetail(
            currentUvIndex = currentUvIndex,
            maxUvIndex = maxUv,
            category = uvCategory(currentUvIndex, languageCode),
            recommendation = uvRecommendation(currentUvIndex, languageCode),
            hourlyTimes = pairs.first,
            hourlyUvIndex = pairs.second.map { it ?: 0.0 },
        )
    }

    // ─────────────────────────────────────────────────────────
    // Wind — uses forecast/hourly wind_speed_10m
    // ─────────────────────────────────────────────────────────

    override suspend fun getWindDetail(latitude: Double, longitude: Double, speedKmh: Float, gustsKmh: Float, dirDeg: Float, dirCardinal: String, languageCode: String): WindDetail {
        val dto = weatherApi.getForecast(latitude = latitude, longitude = longitude)
        val hourly = dto.hourly
        val times = hourly?.time ?: emptyList()
        val speeds = hourly?.windSpeed10m ?: emptyList()

        val pairs = alignHourly(times, speeds, limit = 24)

        return WindDetail(
            speedKmh = speedKmh,
            gustsKmh = gustsKmh,
            directionDeg = dirDeg,
            directionCardinal = dirCardinal,
            hourlyTimes = pairs.first,
            hourlySpeedKmh = pairs.second.map { it ?: 0.0 },
        )
    }

    // ─────────────────────────────────────────────────────────
    // Precipitation — uses forecast/hourly precipitation + probability
    // ─────────────────────────────────────────────────────────

    override suspend fun getPrecipitationDetail(latitude: Double, longitude: Double, currentMm: Float, probabilityPct: Int, languageCode: String): PrecipitationDetail {
        val dto = weatherApi.getForecast(latitude = latitude, longitude = longitude)
        val dailySumMm = dto.daily?.precipitationSum?.firstOrNull()?.toFloat() ?: 0f
        val hourly = dto.hourly
        val times = hourly?.time ?: emptyList()
        val mmList = hourly?.precipitation ?: emptyList()
        val probList = hourly?.precipitationProbability ?: emptyList()

        val pairsPrec = alignHourly(times, mmList, limit = 24)
        val pairsProb = alignHourly(times, probList, limit = 24)

        return PrecipitationDetail(
            currentMm = currentMm,
            probabilityPct = probabilityPct,
            dailySumMm = dailySumMm,
            hourlyTimes = pairsPrec.first,
            hourlyMm = pairsPrec.second.map { it ?: 0.0 },
            hourlyProbability = pairsProb.second.map { it ?: 0 },
        )
    }

    // ─────────────────────────────────────────────────────────
    // Feels Like — uses forecast/hourly apparent_temperature + temperature
    // ─────────────────────────────────────────────────────────

    override suspend fun getFeelsLikeDetail(latitude: Double, longitude: Double, actualTemp: Int, feelsLike: Int, humidity: Int, windSpeed: Float, languageCode: String): FeelsLikeDetail {
        val dto = weatherApi.getForecast(latitude = latitude, longitude = longitude)
        val hourly = dto.hourly
        val times = hourly?.time ?: emptyList()
        val actualList = hourly?.temperature2m ?: emptyList()
        val feelsList = hourly?.apparentTemperature ?: emptyList()

        val pairsActual = alignHourly(times, actualList, limit = 24)
        val pairsFeels  = alignHourly(times, feelsList, limit = 24)

        return FeelsLikeDetail(
            actualTemp = actualTemp,
            feelsLikeTemp = feelsLike,
            diffCelsius = feelsLike - actualTemp,
            humidity = humidity,
            windSpeedKmh = windSpeed,
            hourlyTimes = pairsActual.first,
            hourlyActual = pairsActual.second.map { (it ?: actualTemp.toDouble()).roundToInt() },
            hourlyFeelsLike = pairsFeels.second.map { (it ?: feelsLike.toDouble()).roundToInt() },
        )
    }

    // ─────────────────────────────────────────────────────────
    // Humidity — uses forecast/hourly relative_humidity_2m + dew_point
    // ─────────────────────────────────────────────────────────

    override suspend fun getHumidityDetail(latitude: Double, longitude: Double, humidity: Int, languageCode: String): HumidityDetail {
        val dto = weatherApi.getForecast(latitude = latitude, longitude = longitude)
        val dewPoint = dto.current?.dewPoint2m ?: calculateDewPoint(humidity, dto.current?.temperature2m ?: 25.0)
        val hourly = dto.hourly
        val times = hourly?.time ?: emptyList()
        val humList = hourly?.relativeHumidity2m ?: emptyList()

        val pairs = alignHourly(times, humList, limit = 24)

        return HumidityDetail(
            humidity = humidity,
            dewPointC = dewPoint,
            hourlyTimes = pairs.first,
            hourlyHumidity = pairs.second.map { it ?: humidity },
            humidityCategory = humidityCategory(humidity, languageCode),
        )
    }

    // ─────────────────────────────────────────────────────────
    // Shared helpers
    // ─────────────────────────────────────────────────────────

    private fun <T> alignHourly(
        times: List<String>,
        values: List<T?>,
        limit: Int = 24,
    ): Pair<List<String>, List<T?>> {
        if (times.isEmpty() || values.isEmpty()) return Pair(emptyList(), emptyList())
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        var startIdx = 0
        for (i in times.indices) {
            try {
                val t = sdf.parse(times[i])?.time ?: continue
                if (t >= now - 3_600_000L) { startIdx = i; break }
            } catch (_: Exception) { }
        }
        val endIdx = minOf(startIdx + limit, times.size, values.size)
        val slicedTimes = times.subList(startIdx, endIdx).map { formatHourOnly(it) }
        val slicedValues = values.subList(startIdx, endIdx)
        return Pair(slicedTimes, slicedValues)
    }

    private fun formatHourOnly(rawTime: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = sdf.parse(rawTime)
            if (date != null) SimpleDateFormat("HH:mm", Locale.US).format(date) else rawTime
        } catch (_: Exception) { rawTime }
    }

    private fun aqiCategory(usAqi: Int, lang: String): String {
        return if (lang == "in") {
            when {
                usAqi <= 50  -> "Baik"
                usAqi <= 100 -> "Sedang"
                usAqi <= 150 -> "Tidak Sehat bagi Kelompok Sensitif"
                usAqi <= 200 -> "Tidak Sehat"
                usAqi <= 300 -> "Sangat Tidak Sehat"
                else         -> "Berbahaya"
            }
        } else {
            when {
                usAqi <= 50  -> "Good"
                usAqi <= 100 -> "Moderate"
                usAqi <= 150 -> "Unhealthy for Sensitive Groups"
                usAqi <= 200 -> "Unhealthy"
                usAqi <= 300 -> "Very Unhealthy"
                else         -> "Hazardous"
            }
        }
    }

    private fun aqiRecommendation(usAqi: Int, lang: String): String {
        return if (lang == "in") {
            when {
                usAqi <= 50  -> "Kualitas udara memuaskan. Tidak ada pembatasan untuk aktivitas luar ruangan."
                usAqi <= 100 -> "Kualitas udara dapat diterima. Orang yang sangat sensitif sebaiknya mengurangi aktivitas luar ruangan yang lama."
                usAqi <= 150 -> "Kelompok sensitif sebaiknya mengurangi aktivitas luar ruangan yang lama."
                usAqi <= 200 -> "Setiap orang sebaiknya mengurangi aktivitas luar ruangan yang lama."
                usAqi <= 300 -> "Setiap orang sebaiknya menghindari seluruh aktivitas luar ruangan."
                else         -> "Setiap orang sebaiknya tetap berada di dalam ruangan dan menjaga tingkat aktivitas tetap rendah."
            }
        } else {
            when {
                usAqi <= 50  -> "Air quality is satisfactory. No restrictions for outdoor activities."
                usAqi <= 100 -> "Air quality is acceptable. Unusually sensitive people should consider reducing prolonged outdoor exertion."
                usAqi <= 150 -> "Sensitive groups should reduce prolonged outdoor exertion."
                usAqi <= 200 -> "Everyone should reduce prolonged outdoor exertion."
                usAqi <= 300 -> "Everyone should avoid all outdoor exertion."
                else         -> "Everyone should remain indoors and keep activity levels low."
            }
        }
    }

    private fun uvCategory(uv: Int, lang: String): String {
        return if (lang == "in") {
            when {
                uv <= 2  -> "Rendah"
                uv <= 5  -> "Sedang"
                uv <= 7  -> "Tinggi"
                uv <= 10 -> "Sangat Tinggi"
                else     -> "Ekstrem"
            }
        } else {
            when {
                uv <= 2  -> "Low"
                uv <= 5  -> "Moderate"
                uv <= 7  -> "High"
                uv <= 10 -> "Very High"
                else     -> "Extreme"
            }
        }
    }

    private fun uvRecommendation(uv: Int, lang: String): String {
        return if (lang == "in") {
            when {
                uv <= 2  -> "Tidak perlu perlindungan. Anda dapat dengan aman berada di luar."
                uv <= 5  -> "Perlukan perlindungan. Cari naungan pada jam-jam siang hari."
                uv <= 7  -> "Perlindungan esensial. Kenakan tabir surya SPF 30+, topi, dan kacamata hitam."
                uv <= 10 -> "Perlindungan ekstra diperlukan. Hindari berada di luar saat siang hari. Disarankan SPF 50+."
                else     -> "Ambil segala tindakan pencegahan. Kulit yang tidak terlindungi dapat terbakar dalam beberapa menit."
            }
        } else {
            when {
                uv <= 2  -> "No protection needed. You can safely stay outside."
                uv <= 5  -> "Protection required. Seek shade during midday hours."
                uv <= 7  -> "Protection essential. Wear sunscreen SPF 30+, hat, and sunglasses."
                uv <= 10 -> "Extra protection needed. Avoid being outside at noon. SPF 50+ recommended."
                else     -> "Take all precautions. Unprotected skin can burn in minutes."
            }
        }
    }

    private fun humidityCategory(h: Int, lang: String): String {
        return if (lang == "in") {
            when {
                h < 30  -> "Kering"
                h < 50  -> "Nyaman"
                h < 70  -> "Lembap"
                else    -> "Sangat Lembap"
            }
        } else {
            when {
                h < 30  -> "Dry"
                h < 50  -> "Comfortable"
                h < 70  -> "Humid"
                else    -> "Very Humid"
            }
        }
    }

    /** Magnus formula approximation for dew point. */
    private fun calculateDewPoint(relHumidity: Int, tempC: Double): Double {
        val a = 17.625
        val b = 243.04
        val alpha = (a * tempC / (b + tempC)) + Math.log(relHumidity / 100.0)
        return (b * alpha) / (a - alpha)
    }
}
