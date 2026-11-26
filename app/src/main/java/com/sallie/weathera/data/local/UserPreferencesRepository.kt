package com.sallie.weathera.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sallie.weathera.domain.model.SearchResultLocation
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
    val savedLocations: List<SearchResultLocation> = defaultPresetLocations,
)

val defaultPresetLocations = listOf(
    SearchResultLocation("p1", "Surakarta", "Indonesia", "Jawa Tengah", -7.5755, 110.8243, "Asia/Jakarta"),
    SearchResultLocation("p2", "Jakarta", "Indonesia", "DKI Jakarta", -6.2088, 106.8456, "Asia/Jakarta"),
    SearchResultLocation("p3", "Yogyakarta", "Indonesia", "DIY", -7.7956, 110.3695, "Asia/Jakarta"),
    SearchResultLocation("p4", "Bandung", "Indonesia", "Jawa Barat", -6.9175, 107.6191, "Asia/Jakarta"),
    SearchResultLocation("p5", "Surabaya", "Indonesia", "Jawa Timur", -7.2575, 112.7521, "Asia/Jakarta"),
    SearchResultLocation("p6", "Denpasar", "Indonesia", "Bali", -8.6705, 115.2126, "Asia/Makassar"),
)

class UserPreferencesRepository private constructor(private val context: Context) {

    private val gson = Gson()

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
        val SAVED_LOCATIONS_JSON = stringPreferencesKey("saved_locations_json")
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
            val locationsJson = preferences[PreferencesKeys.SAVED_LOCATIONS_JSON]
            val savedList = if (!locationsJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<SearchResultLocation>>() {}.type
                    gson.fromJson<List<SearchResultLocation>>(locationsJson, type) ?: defaultPresetLocations
                } catch (_: Exception) {
                    defaultPresetLocations
                }
            } else {
                defaultPresetLocations
            }

            UserPreferences(
                activeCityName = preferences[PreferencesKeys.CITY_NAME] ?: "Surakarta",
                activeCountry = preferences[PreferencesKeys.COUNTRY] ?: "Indonesia",
                activeLatitude = preferences[PreferencesKeys.LATITUDE] ?: -7.5755,
                activeLongitude = preferences[PreferencesKeys.LONGITUDE] ?: 110.8243,
                themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                languageCode = preferences[PreferencesKeys.LANGUAGE_CODE] ?: "in",
                savedLocations = savedList,
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

    suspend fun addSavedLocation(location: SearchResultLocation) {
        context.dataStore.edit { preferences ->
            val locationsJson = preferences[PreferencesKeys.SAVED_LOCATIONS_JSON]
            val currentList = if (!locationsJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<SearchResultLocation>>() {}.type
                    gson.fromJson<List<SearchResultLocation>>(locationsJson, type) ?: defaultPresetLocations
                } catch (_: Exception) {
                    defaultPresetLocations
                }
            } else {
                defaultPresetLocations
            }.toMutableList()

            val exists = currentList.any {
                it.cityName.equals(location.cityName, ignoreCase = true) &&
                        it.country.equals(location.country, ignoreCase = true)
            }
            if (!exists) {
                currentList.add(0, location)
                preferences[PreferencesKeys.SAVED_LOCATIONS_JSON] = gson.toJson(currentList)
            }
        }
    }

    suspend fun removeSavedLocation(locationId: String) {
        context.dataStore.edit { preferences ->
            val locationsJson = preferences[PreferencesKeys.SAVED_LOCATIONS_JSON]
            if (!locationsJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<SearchResultLocation>>() {}.type
                    val currentList = gson.fromJson<List<SearchResultLocation>>(locationsJson, type)?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.id == locationId }
                    preferences[PreferencesKeys.SAVED_LOCATIONS_JSON] = gson.toJson(currentList)
                } catch (_: Exception) {
                    // Ignore
                }
            }
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.getSharedPreferences("weathera_lang", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", themeMode)
            .apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode
        }
    }

    suspend fun updateLanguageCode(languageCode: String) {
        context.getSharedPreferences("weathera_lang", Context.MODE_PRIVATE)
            .edit()
            .putString("language_code", languageCode)
            .apply()
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = languageCode
        }
    }
}
