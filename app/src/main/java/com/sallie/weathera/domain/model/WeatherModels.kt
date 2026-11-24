package com.sallie.weathera.domain.model

import com.sallie.weathera.presentation.components.WeatherCondition

data class CityWeather(
    val id: String,
    val cityName: String,
    val country: String,
    val temperature: Int,
    val tempMax: Int,
    val tempMin: Int,
    val condition: WeatherCondition,
    val isSaved: Boolean = false,
)

data class AirQuality(
    val index: Int = 3,
    val category: String = "Low Health Risk",
    val description: String = "Kualitas udara sangat baik dan aman untuk aktivitas luar ruangan.",
    val progress: Float = 0.28f,
)

data class SunriseSunset(
    val sunrise: String = "05:28 AM",
    val sunset: String = "17:48 WIB",
    val daylightRemaining: String = "Siang hari ~12 jam 20 menit",
    val sunProgress: Float = 0.65f, // 0.0 at sunrise, 1.0 at sunset
)

data class WindInfo(
    val speedKmh: Float = 9.7f,
    val direction: String = "ENE",
    val angleDegree: Float = 68f,
    val gustsKmh: Float = 14.2f,
)

data class RainfallInfo(
    val lastHourMm: Float = 1.8f,
    val expectedNext24hMm: Float = 1.2f,
    val probability: Int = 20,
)

data class CurrentWeather(
    val cityName: String,
    val country: String,
    val dateFormatted: String,
    val temperature: Int,
    val condition: WeatherCondition,
    val feelsLike: Int,
    val tempMax: Int,
    val tempMin: Int,
    val humidity: Int,
    val windSpeed: Int,
    val precipitationProb: Int,
    val uvIndex: Int,
    val uvCategory: String = "Moderate",
    val lastUpdated: String,
    val airQuality: AirQuality = AirQuality(),
    val sunriseSunset: SunriseSunset = SunriseSunset(),
    val windInfo: WindInfo = WindInfo(),
    val rainfallInfo: RainfallInfo = RainfallInfo(),
)

data class HourlyForecastItem(
    val time: String,
    val temperature: Int,
    val condition: WeatherCondition,
    val rainProbability: Int,
    val isNow: Boolean = false,
)

data class DailyForecastItem(
    val dayName: String,
    val condition: WeatherCondition,
    val tempMin: Int,
    val tempMax: Int,
    val rainProbability: Int = 0,
)

object WeatherDemoData {
    val initialCurrentWeather = CurrentWeather(
        cityName = "Surakarta",
        country = "Indonesia",
        dateFormatted = "Senin, 23 November 2026",
        temperature = 28,
        condition = WeatherCondition.PARTLY_CLOUDY,
        feelsLike = 30,
        tempMax = 30,
        tempMin = 23,
        humidity = 72,
        windSpeed = 12,
        precipitationProb = 20,
        uvIndex = 5,
        uvCategory = "Sedang",
        lastUpdated = "12:30 WIB",
        airQuality = AirQuality(
            index = 3,
            category = "Low Health Risk",
            description = "Kualitas udara tergolong bersih dan nyaman untuk bernapas.",
            progress = 0.30f,
        ),
        sunriseSunset = SunriseSunset(
            sunrise = "05:28 AM",
            sunset = "17:48 WIB",
            daylightRemaining = "Tersisa 5 jam sinar matahari",
            sunProgress = 0.58f,
        ),
        windInfo = WindInfo(
            speedKmh = 12f,
            direction = "ENE",
            angleDegree = 65f,
            gustsKmh = 16.5f,
        ),
        rainfallInfo = RainfallInfo(
            lastHourMm = 0.4f,
            expectedNext24hMm = 2.1f,
            probability = 20,
        ),
    )

    val hourlyForecast: List<HourlyForecastItem> = listOf(
        HourlyForecastItem("Now", 28, WeatherCondition.PARTLY_CLOUDY, 20, isNow = true),
        HourlyForecastItem("13:00", 29, WeatherCondition.PARTLY_CLOUDY, 20),
        HourlyForecastItem("14:00", 30, WeatherCondition.CLEAR_SKY, 10),
        HourlyForecastItem("15:00", 29, WeatherCondition.CLEAR_SKY, 10),
        HourlyForecastItem("16:00", 28, WeatherCondition.PARTLY_CLOUDY, 25),
        HourlyForecastItem("17:00", 27, WeatherCondition.RAIN, 60),
        HourlyForecastItem("18:00", 26, WeatherCondition.DRIZZLE, 45),
        HourlyForecastItem("19:00", 25, WeatherCondition.CLOUDY, 30),
        HourlyForecastItem("20:00", 25, WeatherCondition.NIGHT_CLEAR, 10),
        HourlyForecastItem("21:00", 24, WeatherCondition.NIGHT_CLEAR, 5),
        HourlyForecastItem("22:00", 24, WeatherCondition.NIGHT_CLEAR, 5),
        HourlyForecastItem("23:00", 23, WeatherCondition.NIGHT_CLEAR, 5),
        HourlyForecastItem("00:00", 23, WeatherCondition.NIGHT_CLEAR, 5),
        HourlyForecastItem("01:00", 23, WeatherCondition.NIGHT_CLEAR, 5),
        HourlyForecastItem("02:00", 22, WeatherCondition.NIGHT_CLEAR, 5),
        HourlyForecastItem("03:00", 22, WeatherCondition.FOG, 15),
        HourlyForecastItem("04:00", 22, WeatherCondition.FOG, 15),
        HourlyForecastItem("05:00", 23, WeatherCondition.PARTLY_CLOUDY, 10),
        HourlyForecastItem("06:00", 24, WeatherCondition.CLEAR_SKY, 5),
        HourlyForecastItem("07:00", 25, WeatherCondition.CLEAR_SKY, 5),
        HourlyForecastItem("08:00", 26, WeatherCondition.CLEAR_SKY, 5),
        HourlyForecastItem("09:00", 27, WeatherCondition.PARTLY_CLOUDY, 10),
        HourlyForecastItem("10:00", 28, WeatherCondition.PARTLY_CLOUDY, 15),
        HourlyForecastItem("11:00", 29, WeatherCondition.PARTLY_CLOUDY, 20),
    )

    val dailyForecast: List<DailyForecastItem> = listOf(
        DailyForecastItem("Today", WeatherCondition.PARTLY_CLOUDY, 23, 30, rainProbability = 20),
        DailyForecastItem("Tuesday", WeatherCondition.CLEAR_SKY, 24, 32, rainProbability = 5),
        DailyForecastItem("Wednesday", WeatherCondition.RAIN, 22, 28, rainProbability = 75),
        DailyForecastItem("Thursday", WeatherCondition.CLOUDY, 23, 29, rainProbability = 30),
        DailyForecastItem("Friday", WeatherCondition.CLEAR_SKY, 24, 31, rainProbability = 5),
        DailyForecastItem("Saturday", WeatherCondition.RAIN, 22, 27, rainProbability = 80),
        DailyForecastItem("Sunday", WeatherCondition.PARTLY_CLOUDY, 23, 30, rainProbability = 15),
    )

    val defaultDemoCities: List<CityWeather> = listOf(
        CityWeather("1", "Surakarta", "Indonesia", 28, 30, 23, WeatherCondition.PARTLY_CLOUDY, isSaved = true),
        CityWeather("2", "Kathmandu", "Nepal", 25, 27, 20, WeatherCondition.PARTLY_CLOUDY, isSaved = true),
        CityWeather("3", "Montreal", "Canada", 19, 24, 18, WeatherCondition.CLOUDY, isSaved = true),
        CityWeather("4", "Yogyakarta", "Indonesia", 29, 31, 24, WeatherCondition.CLEAR_SKY, isSaved = false),
        CityWeather("5", "Jakarta", "Indonesia", 31, 33, 25, WeatherCondition.RAIN, isSaved = false),
        CityWeather("6", "Bandung", "Indonesia", 24, 26, 19, WeatherCondition.CLOUDY, isSaved = false),
        CityWeather("7", "Surabaya", "Indonesia", 32, 34, 26, WeatherCondition.CLEAR_SKY, isSaved = false),
        CityWeather("8", "Semarang", "Indonesia", 30, 32, 25, WeatherCondition.PARTLY_CLOUDY, isSaved = false),
        CityWeather("9", "Denpasar", "Indonesia", 29, 31, 25, WeatherCondition.CLEAR_SKY, isSaved = false),
    )

    fun createCurrentWeatherForCity(city: CityWeather): CurrentWeather {
        return initialCurrentWeather.copy(
            cityName = city.cityName,
            country = city.country,
            temperature = city.temperature,
            tempMax = city.tempMax,
            tempMin = city.tempMin,
            condition = city.condition,
            feelsLike = city.temperature + 2,
        )
    }
}
