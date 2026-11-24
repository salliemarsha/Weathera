package com.sallie.weathera.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val activeCityName: String = "Surakarta",
    val activeCountry: String = "Indonesia",
    val activeLatitude: Double = -7.5755,
    val activeLongitude: Double = 110.8243,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val languageCode: String = "in", // "in", "en"
)

class UserPreferencesRepository private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private object PreferencesKeys {
        val CITY_NAME = stringPreferencesKey("active_city_name")
        val COUNTRY = stringPreferencesKey("active_country")
        val LATITUDE = doublePreferencesKey("active_latitude")
        val LONGITUDE = doublePreferencesKey("active_longitude")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                activeCityName = preferences[PreferencesKeys.CITY_NAME] ?: "Surakarta",
                activeCountry = preferences[PreferencesKeys.COUNTRY] ?: "Indonesia",
                activeLatitude = preferences[PreferencesKeys.LATITUDE] ?: -7.5755,
                activeLongitude = preferences[PreferencesKeys.LONGITUDE] ?: 110.8243,
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                languageCode = preferences[PreferencesKeys.LANGUAGE_CODE] ?: "in",
            )
        }

    suspend fun updateActiveLocation(cityName: String, country: String, latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CITY_NAME] = cityName
            preferences[PreferencesKeys.COUNTRY] = country
            preferences[PreferencesKeys.LATITUDE] = latitude
            preferences[PreferencesKeys.LONGITUDE] = longitude
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode
        }
    }

    suspend fun updateLanguageCode(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }
}
