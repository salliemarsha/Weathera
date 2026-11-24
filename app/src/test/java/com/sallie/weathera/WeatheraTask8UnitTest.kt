package com.sallie.weathera

import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.domain.model.SearchResultLocation
import com.sallie.weathera.presentation.components.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatheraTask8UnitTest {

    @Test
    fun testWmoWeatherCodeMapping() {
        assertEquals(WeatherCondition.CLEAR_SKY, WeatherCondition.fromWmoCode(0, isDay = true))
        assertEquals(WeatherCondition.NIGHT_CLEAR, WeatherCondition.fromWmoCode(0, isDay = false))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromWmoCode(1, isDay = true))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(3, isDay = true))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45, isDay = true))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromWmoCode(51, isDay = true))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(61, isDay = true))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(71, isDay = true))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(95, isDay = true))
    }

    @Test
    fun testDefaultUserPreferences() {
        val defaultPrefs = UserPreferences()
        assertEquals("Surakarta", defaultPrefs.activeCityName)
        assertEquals("Indonesia", defaultPrefs.activeCountry)
        assertEquals(-7.5755, defaultPrefs.activeLatitude, 0.0001)
        assertEquals(110.8243, defaultPrefs.activeLongitude, 0.0001)
        assertEquals("SYSTEM", defaultPrefs.themeMode)
        assertEquals("in", defaultPrefs.languageCode)
    }

    @Test
    fun testSearchResultLocationModel() {
        val location = SearchResultLocation(
            id = "101",
            cityName = "Jakarta",
            country = "Indonesia",
            adminRegion = "DKI Jakarta",
            latitude = -6.2088,
            longitude = 106.8456,
            timezone = "Asia/Jakarta",
        )
        assertEquals("Jakarta", location.cityName)
        assertEquals("Indonesia", location.country)
        assertEquals(-6.2088, location.latitude, 0.0001)
        assertEquals(106.8456, location.longitude, 0.0001)
    }
}
