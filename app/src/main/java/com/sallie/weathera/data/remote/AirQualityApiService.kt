package com.sallie.weathera.data.remote

import com.sallie.weathera.data.remote.dto.AirQualityResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApiService {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "pm2_5,pm10,ozone,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,european_aqi,us_aqi",
        @Query("hourly") hourly: String = "pm2_5,pm10,ozone,us_aqi",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto",
    ): AirQualityResponseDto
}
