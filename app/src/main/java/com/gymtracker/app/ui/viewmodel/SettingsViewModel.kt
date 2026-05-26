package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.datastore.SettingsDataStore
import com.gymtracker.app.data.repository.ExerciseRepository
import com.gymtracker.app.utils.ExerciseSeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val darkTheme = settingsDataStore.darkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val useKg = settingsDataStore.useKg.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val restTimerSeconds = settingsDataStore.restTimerSeconds.stateIn(viewModelScope, SharingStarted.Eagerly, 90)
    val onboardingDone = settingsDataStore.onboardingDone.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        seedExercisesIfNeeded()
    }

    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch {
        settingsDataStore.setDarkTheme(enabled)
    }

    fun setUseKg(useKg: Boolean) = viewModelScope.launch {
        settingsDataStore.setUseKg(useKg)
    }

    fun setRestTimerSeconds(seconds: Int) = viewModelScope.launch {
        settingsDataStore.setRestTimerSeconds(seconds)
    }

    fun completeOnboarding() = viewModelScope.launch {
        settingsDataStore.setOnboardingDone()
    }

    private fun seedExercisesIfNeeded() = viewModelScope.launch {
        if (exerciseRepository.countExercises() == 0) {
            exerciseRepository.seedExercises(ExerciseSeed.exercises)
        }
    }
}
