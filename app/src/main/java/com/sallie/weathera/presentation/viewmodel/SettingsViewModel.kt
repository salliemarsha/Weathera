package com.sallie.weathera.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(application),
) : AndroidViewModel(application) {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences(),
        )

    fun updateThemeMode(themeMode: String) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(themeMode)
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.updateLanguageCode(languageCode)
        }
    }
}
