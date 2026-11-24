package com.sallie.weathera

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = UserPreferencesRepository.getInstance(applicationContext)

        setContent {
            val userPreferences by preferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = UserPreferences()
            )

            val darkTheme = when (userPreferences.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val baseContext = LocalContext.current
            androidx.compose.runtime.SideEffect {
                updateLocaleConfiguration(baseContext, userPreferences.languageCode)
            }

            val scope = rememberCoroutineScope()

            WeatheraTheme(darkTheme = darkTheme) {
                WeatheraAppContent(
                    darkTheme = darkTheme,
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

    private fun updateLocaleConfiguration(context: Context, languageCode: String) {
        val locale = if (languageCode == "en") Locale.forLanguageTag("en-US") else Locale.forLanguageTag("id-ID")
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

@Composable
fun WeatheraAppContent(
    darkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentDestination) {
            NavDestination.HOME -> {
                HomeScreen(
                    isDarkTheme = darkTheme,
                    onToggleTheme = onToggleTheme,
                    onNavigateToLocations = { currentDestination = NavDestination.LOCATIONS },
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
            onNavigate = { destination ->
                currentDestination = destination
            },
            onCenterFabClick = {
                currentDestination = if (currentDestination == NavDestination.SETTINGS) NavDestination.HOME else NavDestination.SETTINGS
            },
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
