package com.sallie.weathera.data.repository

import com.sallie.weathera.data.remote.RetrofitClient
import com.sallie.weathera.data.remote.WeatherApiService
import com.sallie.weathera.data.remote.dto.WeatherResponseDto
import com.sallie.weathera.domain.model.AirQuality
import com.sallie.weathera.domain.model.CurrentWeather
import com.sallie.weathera.domain.model.DailyForecastItem
import com.sallie.weathera.domain.model.HourlyForecastItem
import com.sallie.weathera.domain.model.RainfallInfo
import com.sallie.weathera.domain.model.SunriseSunset
import com.sallie.weathera.domain.model.WindInfo
import com.sallie.weathera.presentation.components.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class WeatherResult(
    val currentWeather: CurrentWeather,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem>,
)

interface WeatherRepository {
    suspend fun getWeatherForecast(
        latitude: Double = -7.5755,
        longitude: Double = 110.8243,
        cityName: String = "Surakarta",
        country: String = "Indonesia",
        languageCode: String = "in",
    ): WeatherResult
}

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService = RetrofitClient.weatherApiService,
) : WeatherRepository {

    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double,
        cityName: String,
        country: String,
        languageCode: String,
    ): WeatherResult {
        val dto = apiService.getForecast(
            latitude = latitude,
            longitude = longitude,
        )
        return mapDtoToDomain(dto, cityName, country, languageCode)
    }

    private fun mapDtoToDomain(
        dto: WeatherResponseDto,
        cityName: String,
        country: String,
        languageCode: String,
    ): WeatherResult {
        val current = dto.current
        val hourly = dto.hourly
        val daily = dto.daily

        val isDay = (current?.isDay == 1)
        val weatherCode = current?.weatherCode ?: 0
        val condition = WeatherCondition.fromWmoCode(weatherCode, isDay)

        val temp = current?.temperature2m?.roundToInt() ?: 0
        val feelsLike = current?.apparentTemperature?.roundToInt() ?: temp
        val humidity = current?.relativeHumidity2m ?: 0
        val windSpeed = current?.windSpeed10m?.roundToInt() ?: 0
        val precipitation = current?.precipitation?.toFloat() ?: 0f

        val todayMax = daily?.temperature2mMax?.firstOrNull()?.roundToInt() ?: temp
        val todayMin = daily?.temperature2mMin?.firstOrNull()?.roundToInt() ?: temp
        val precipProb = daily?.precipitationProbabilityMax?.firstOrNull() ?: 0
        val uvIndex = daily?.uvIndexMax?.firstOrNull()?.roundToInt() ?: 0
        val uvCategory = getUvCategory(uvIndex, languageCode)

        val dateFormatted = formatDateFormatted(current?.time, languageCode)
        val lastUpdated = formatLastUpdated(current?.time)

        val sunriseRaw = daily?.sunrise?.firstOrNull()
        val sunsetRaw = daily?.sunset?.firstOrNull()
        val sunriseFormatted = formatTimeAmPm(sunriseRaw) ?: "05:28 AM"
        val sunsetFormatted = formatTimeWib(sunsetRaw) ?: "17:48 WIB"

        val windDirDeg = current?.windDirection10m?.toFloat() ?: 0f
        val windDirectionText = getWindDirectionCardinal(windDirDeg)

        val aqDesc = if (languageCode == "in") "Kualitas udara tergolong bersih dan nyaman untuk bernapas."
                     else "Air quality is clean and comfortable for breathing."
        val daylightRem = if (languageCode == "in") "Sinar matahari sepanjang hari"
                          else "Daylight throughout the day"

        val currentWeather = CurrentWeather(
            cityName = cityName,
            country = country,
            dateFormatted = dateFormatted,
            temperature = temp,
            condition = condition,
            feelsLike = feelsLike,
            tempMax = todayMax,
            tempMin = todayMin,
            humidity = humidity,
            windSpeed = windSpeed,
            precipitationProb = precipProb,
            uvIndex = uvIndex,
            uvCategory = uvCategory,
            lastUpdated = lastUpdated,
            airQuality = AirQuality(
                index = 3,
                category = if (languageCode == "in") "Risiko Kesehatan Rendah" else "Low Health Risk",
                description = aqDesc,
                progress = 0.30f,
            ),
            sunriseSunset = SunriseSunset(
                sunrise = sunriseFormatted,
                sunset = sunsetFormatted,
                daylightRemaining = daylightRem,
                sunProgress = calculateSunProgress(sunriseRaw, sunsetRaw, current?.time),
            ),
            windInfo = WindInfo(
                speedKmh = current?.windSpeed10m?.toFloat() ?: 0f,
                direction = windDirectionText,
                angleDegree = windDirDeg,
                gustsKmh = current?.windGusts10m?.toFloat()
                    ?: ((current?.windSpeed10m?.toFloat() ?: 0f) * 1.3f),
            ),
            rainfallInfo = RainfallInfo(
                lastHourMm = precipitation,
                expectedNext24hMm = (precipProb * 0.05f),
                probability = precipProb,
            ),
        )
        // Process Hourly Forecast (24 Hours)
        val hourlyItems = mutableListOf<HourlyForecastItem>()
        if (hourly?.time != null && hourly.temperature2m != null) {
            val currentHourIndex = findCurrentHourIndex(hourly.time, current?.time)
            val totalHours = hourly.time.size
            val count = minOf(24, totalHours - currentHourIndex)

            for (i in 0 until count) {
                val index = currentHourIndex + i
                if (index < totalHours) {
                    val rawTime = hourly.time[index]
                    val isNow = (i == 0)
                    val displayTime = if (isNow) (if (languageCode == "in") "Sekarang" else "Now") else formatHourTime(rawTime)
                    val hTemp = hourly.temperature2m[index].roundToInt()
                    val hCode = hourly.weatherCode?.getOrNull(index) ?: 0
                    val hProb = hourly.precipitationProbability?.getOrNull(index) ?: 0
                    val hIsDay = isHourDaytime(rawTime)
                    val hCondition = WeatherCondition.fromWmoCode(hCode, hIsDay)

                    hourlyItems.add(
                        HourlyForecastItem(
                            time = displayTime,
                            temperature = hTemp,
                            condition = hCondition,
                            rainProbability = hProb,
                            isNow = isNow,
                        )
                    )
                }
            }
        }

        // Process Daily Forecast (7 Days)
        val dailyItems = mutableListOf<DailyForecastItem>()
        if (daily?.time != null) {
            val count = minOf(7, daily.time.size)
            for (i in 0 until count) {
                val rawDate = daily.time[i]
                val dayName = if (i == 0) (if (languageCode == "in") "Hari Ini" else "Today") else getDayName(rawDate, languageCode)
                val dCode = daily.weatherCode?.getOrNull(i) ?: 0
                val dMin = daily.temperature2mMin?.getOrNull(i)?.roundToInt() ?: 0
                val dMax = daily.temperature2mMax?.getOrNull(i)?.roundToInt() ?: 0
                val dProb = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
                val dCondition = WeatherCondition.fromWmoCode(dCode, isDay = true)

                dailyItems.add(
                    DailyForecastItem(
                        dayName = dayName,
                        condition = dCondition,
                        tempMin = dMin,
                        tempMax = dMax,
                        rainProbability = dProb,
                    )
                )
            }
        }

        return WeatherResult(
            currentWeather = currentWeather,
            hourlyForecast = hourlyItems,
            dailyForecast = dailyItems,
        )
    }

    private fun getUvCategory(uvIndex: Int, languageCode: String): String {
        return if (languageCode == "in") {
            when {
                uvIndex <= 2 -> "Rendah"
                uvIndex <= 5 -> "Sedang"
                uvIndex <= 7 -> "Tinggi"
                uvIndex <= 10 -> "Sangat Tinggi"
                else -> "Ekstrem"
            }
        } else {
            when {
                uvIndex <= 2 -> "Low"
                uvIndex <= 5 -> "Moderate"
                uvIndex <= 7 -> "High"
                uvIndex <= 10 -> "Very High"
                else -> "Extreme"
            }
        }
    }

    private fun getWindDirectionCardinal(degrees: Float): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((degrees + 11.25f) / 22.5f).toInt() % 16
        val validIndex = if (index < 0) index + 16 else index
        return directions[validIndex]
    }

    private fun formatDateFormatted(rawTime: String?, languageCode: String): String {
        val locale = if (languageCode == "in") Locale.forLanguageTag("id-ID") else Locale.forLanguageTag("en-US")
        if (rawTime.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
            return sdf.format(Date())
        }
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = inputFormat.parse(rawTime)
            if (date != null) {
                val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
                outputFormat.format(date)
            } else {
                rawTime
            }
        } catch (_: Exception) {
            rawTime
        }
    }

    private fun formatLastUpdated(rawTime: String?): String {
        if (rawTime.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("HH:mm 'WIB'", Locale.US)
            return sdf.format(Date())
        }
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = inputFormat.parse(rawTime)
            if (date != null) {
                val outputFormat = SimpleDateFormat("HH:mm 'WIB'", Locale.US)
                outputFormat.format(date)
            } else {
                "12:00 WIB"
            }
        } catch (_: Exception) {
            "12:00 WIB"
        }
    }

    private fun formatTimeAmPm(rawTime: String?): String? {
        if (rawTime.isNullOrEmpty()) return null
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = inputFormat.parse(rawTime)
            if (date != null) {
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
                outputFormat.format(date)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun formatTimeWib(rawTime: String?): String? {
        if (rawTime.isNullOrEmpty()) return null
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = inputFormat.parse(rawTime)
            if (date != null) {
                val outputFormat = SimpleDateFormat("HH:mm 'WIB'", Locale.US)
                outputFormat.format(date)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun formatHourTime(rawTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val date = inputFormat.parse(rawTime)
            if (date != null) {
                val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
                outputFormat.format(date)
            } else rawTime
        } catch (e: Exception) {
            rawTime
        }
    }

    private fun getDayName(rawDate: String, languageCode: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(rawDate)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (languageCode == "in") {
                    when (dayOfWeek) {
                        Calendar.MONDAY -> "Senin"
                        Calendar.TUESDAY -> "Selasa"
                        Calendar.WEDNESDAY -> "Rabu"
                        Calendar.THURSDAY -> "Kamis"
                        Calendar.FRIDAY -> "Jumat"
                        Calendar.SATURDAY -> "Sabtu"
                        Calendar.SUNDAY -> "Minggu"
                        else -> rawDate
                    }
                } else {
                    when (dayOfWeek) {
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        Calendar.SUNDAY -> "Sunday"
                        else -> rawDate
                    }
                }
            } else rawDate
        } catch (e: Exception) {
            rawDate
        }
    }

    private fun findCurrentHourIndex(times: List<String>, currentRawTime: String?): Int {
        if (currentRawTime == null) return 0
        val currentPrefix = currentRawTime.take(13) // "yyyy-MM-ddTHH"
        val idx = times.indexOfFirst { it.startsWith(currentPrefix) }
        return if (idx >= 0) idx else 0
    }

    private fun isHourDaytime(rawTime: String): Boolean {
        return try {
            val hour = rawTime.substringAfter('T').take(2).toInt()
            hour in 6..18
        } catch (e: Exception) {
            true
        }
    }

    private fun calculateSunProgress(sunriseRaw: String?, sunsetRaw: String?, currentRaw: String?): Float {
        if (sunriseRaw == null || sunsetRaw == null || currentRaw == null) return 0.5f
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
            val sunrise = sdf.parse(sunriseRaw)?.time ?: 0L
            val sunset = sdf.parse(sunsetRaw)?.time ?: 0L
            val current = sdf.parse(currentRaw)?.time ?: 0L
            if (sunset <= sunrise) return 0.5f
            val progress = (current - sunrise).toFloat() / (sunset - sunrise).toFloat()
            progress.coerceIn(0.0f, 1.0f)
        } catch (_: Exception) {
            0.5f
        }
    }
}
