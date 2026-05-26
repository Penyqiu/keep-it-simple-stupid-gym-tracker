package com.gymtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymtracker.app.ui.navigation.NavGraph
import com.gymtracker.app.ui.theme.GymTrackerTheme
import com.gymtracker.app.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val darkTheme by settingsViewModel.darkTheme.collectAsState()
            val onboardingDone by settingsViewModel.onboardingDone.collectAsState()

            GymTrackerTheme(darkTheme = darkTheme ?: isSystemInDarkTheme()) {
                NavGraph(
                    onboardingDone = onboardingDone,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
