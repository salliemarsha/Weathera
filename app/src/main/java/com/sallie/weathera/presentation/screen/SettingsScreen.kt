package com.sallie.weathera.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sallie.weathera.R
import com.sallie.weathera.data.local.UserPreferences
import com.sallie.weathera.presentation.components.WeatherCard
import com.sallie.weathera.presentation.components.WeatherGradientCard
import com.sallie.weathera.presentation.viewmodel.SettingsViewModel
import com.sallie.weathera.ui.theme.ElectricLavender
import com.sallie.weathera.ui.theme.Primary
import com.sallie.weathera.ui.theme.WeatheraCornerRadius
import com.sallie.weathera.ui.theme.WeatheraSpacing

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    SettingsScreenContent(
        preferences = preferences,
        onSelectTheme = { viewModel.updateThemeMode(it) },
        onSelectLanguage = { viewModel.updateLanguage(it) },
        modifier = modifier,
    )
}

@Composable
fun SettingsScreenContent(
    preferences: UserPreferences,
    onSelectTheme: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = WeatheraSpacing.lg, vertical = WeatheraSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.xl),
    ) {
        // Header
        SettingsHeaderSection()

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WeatheraSpacing.lg),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppearanceSection(
                        selectedTheme = preferences.themeMode,
                        onSelectTheme = onSelectTheme,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.xl)) {
                        LanguageSection(
                            selectedLanguage = preferences.languageCode,
                            onSelectLanguage = onSelectLanguage,
                        )
                        AboutAppSection()
                    }
                }
            }
        } else {
            AppearanceSection(
                selectedTheme = preferences.themeMode,
                onSelectTheme = onSelectTheme,
            )
            LanguageSection(
                selectedLanguage = preferences.languageCode,
                onSelectLanguage = onSelectLanguage,
            )
            AboutAppSection()
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
private fun SettingsHeaderSection() {
    Column {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.appearance_section),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppearanceSection(
    selectedTheme: String,
    onSelectTheme: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm)) {
        Text(
            text = stringResource(R.string.appearance_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        WeatherCard {
            Column(modifier = Modifier.padding(WeatheraSpacing.md)) {
                ThemeOptionItem(
                    title = stringResource(R.string.theme_system),
                    subtitle = "Ikuti mode tampilan perangkat Android Anda",
                    selected = (selectedTheme == "SYSTEM"),
                    onClick = { onSelectTheme("SYSTEM") },
                )
                ThemeOptionItem(
                    title = stringResource(R.string.theme_light),
                    subtitle = "Tampilan latar terang Royal Violet & Lavender",
                    selected = (selectedTheme == "LIGHT"),
                    onClick = { onSelectTheme("LIGHT") },
                )
                ThemeOptionItem(
                    title = stringResource(R.string.theme_dark),
                    subtitle = "Tampilan malam gelap dengan kontras nyaman",
                    selected = (selectedTheme == "DARK"),
                    onClick = { onSelectTheme("DARK") },
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = WeatheraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Primary,
                    unselectedColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            )
        }
    }
}

@Composable
private fun LanguageSection(
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm)) {
        Text(
            text = stringResource(R.string.language_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        WeatherCard {
            Column(modifier = Modifier.padding(WeatheraSpacing.md)) {
                LanguageOptionItem(
                    title = stringResource(R.string.language_indonesian),
                    subtitle = "Bahasa Indonesia",
                    selected = (selectedLanguage == "in"),
                    onClick = { onSelectLanguage("in") },
                )
                LanguageOptionItem(
                    title = stringResource(R.string.language_english),
                    subtitle = "English (United States)",
                    selected = (selectedLanguage == "en"),
                    onClick = { onSelectLanguage("en") },
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = WeatheraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Primary,
                    unselectedColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            )
        }
    }
}

@Composable
private fun AboutAppSection() {
    WeatherGradientCard(
        gradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Primary.copy(alpha = 0.85f),
                ElectricLavender.copy(alpha = 0.95f),
            )
        ),
        shape = RoundedCornerShape(WeatheraCornerRadius.Large),
    ) {
        Column(
            modifier = Modifier.padding(WeatheraSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(WeatheraSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.about_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
            )
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
            )
            Spacer(modifier = Modifier.height(WeatheraSpacing.xs))
            Text(
                text = stringResource(R.string.app_version),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
            )
        }
    }
}
