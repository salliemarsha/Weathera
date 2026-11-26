package com.sallie.weathera

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.data.local.UserPreferencesRepository
import com.sallie.weathera.presentation.components.NavDestination
import com.sallie.weathera.presentation.components.WeatherBottomNav
import com.sallie.weathera.presentation.screen.HomeScreen
import com.sallie.weathera.presentation.screen.LocationsScreen
import com.sallie.weathera.presentation.screen.SettingsScreen
import com.sallie.weathera.ui.theme.WeatheraTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    /**
     * The language code that was baked into this Activity instance via
     * [attachBaseContext]. This is the ONLY value used to decide whether
     * [recreate] is needed — never compare against DataStore or SharedPreferences
     * inside LaunchedEffect, because that creates a race condition or loop.
     */
    private var appliedLanguageCode: String = "in"

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("weathera_lang", Context.MODE_PRIVATE)
        val langCode = prefs.getString("language_code", "in") ?: "in"
        appliedLanguageCode = langCode
        super.attachBaseContext(applyLocale(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository.getInstance(applicationContext)

        // Read the language synchronously from SharedPreferences so the initialValue
        // already has the correct language. This prevents the loop caused when
        // DataStore hasn't emitted yet and initialValue("in") != appliedLanguageCode("en").
        val sharedPrefs = getSharedPreferences("weathera_lang", Context.MODE_PRIVATE)
        val savedLangCode = sharedPrefs.getString("language_code", "in") ?: "in"
        val savedThemeMode = sharedPrefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"

        val correctInitialPrefs = UserPreferences(
            themeMode = savedThemeMode,
            languageCode = savedLangCode,
        )

        setContent {
            val userPreferences by preferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = correctInitialPrefs,
            )

            val darkTheme = when (userPreferences.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val scope = rememberCoroutineScope()

            // Guard: only recreate when the language the user chose is different from
            // the language this Activity was actually started with. This LaunchedEffect
            // fires once per unique languageCode value. Because correctInitialPrefs
            // already has the right languageCode, the first emission from DataStore
            // (same value) will NOT trigger a recreate unless the user genuinely changed it.
            LaunchedEffect(userPreferences.languageCode) {
                if (userPreferences.languageCode != appliedLanguageCode) {
                    getSharedPreferences("weathera_lang", Context.MODE_PRIVATE)
                        .edit()
                        .putString("language_code", userPreferences.languageCode)
                        .apply()
                    recreate()
                }
            }

            WeatheraTheme(darkTheme = darkTheme) {
                WeatheraAppContent(
                    darkTheme = darkTheme,
                    savedLocationsCount = userPreferences.savedLocations.size,
                    onToggleTheme = {
                        val nextMode = if (darkTheme) "LIGHT" else "DARK"
                        scope.launch {
                            preferencesRepository.updateThemeMode(nextMode)
                        }
                    }
                )
            }
        }
    }

    private fun applyLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "en") Locale.forLanguageTag("en-US")
                     else Locale.forLanguageTag("id-ID")
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

// ─────────────────────────────────────────────────────────────
// App content
// Simple when-based navigation — AnimatedContent was removed
// because it caused IllegalStateException in the Compose
// animation runtime when transition states were inconsistent.
// ─────────────────────────────────────────────────────────────

@Composable
fun WeatheraAppContent(
    darkTheme: Boolean = false,
    savedLocationsCount: Int = 0,
    onToggleTheme: () -> Unit = {},
) {
    var currentDestination by rememberSaveable { mutableStateOf(NavDestination.HOME) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentDestination) {
            NavDestination.HOME -> {
                HomeScreen(
                    isDarkTheme = darkTheme,
                    onToggleTheme = onToggleTheme,
                    onNavigateToLocations = { currentDestination = NavDestination.LOCATIONS },
                    onNavigateToSettings = { currentDestination = NavDestination.SETTINGS },
                )
            }
            NavDestination.LOCATIONS -> {
                LocationsScreen(
                    onNavigateToHome = { currentDestination = NavDestination.HOME },
                )
            }
            NavDestination.SETTINGS -> {
                SettingsScreen()
            }
        }

        WeatherBottomNav(
            currentDestination = currentDestination,
            onNavigate = { destination -> currentDestination = destination },
            savedLocationsCount = savedLocationsCount,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    WeatheraTheme {
        WeatheraAppContent()
    }
}
