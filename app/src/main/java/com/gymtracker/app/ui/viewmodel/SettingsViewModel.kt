package com.gymtracker.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.datastore.SettingsDataStore
import com.gymtracker.app.data.repository.ExerciseRepository
import com.gymtracker.app.utils.ExerciseSeed
import com.gymtracker.app.utils.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val exerciseRepository: ExerciseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val darkTheme = settingsDataStore.darkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val useKg = settingsDataStore.useKg.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val restTimerSeconds = settingsDataStore.restTimerSeconds.stateIn(viewModelScope, SharingStarted.Eagerly, 90)
    val onboardingDone = settingsDataStore.onboardingDone.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val reminderEnabled = settingsDataStore.reminderEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val reminderHour = settingsDataStore.reminderHour.stateIn(viewModelScope, SharingStarted.Eagerly, 8)
    val reminderMinute = settingsDataStore.reminderMinute.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val reminderDays = settingsDataStore.reminderDays.stateIn(viewModelScope, SharingStarted.Eagerly, setOf(1, 3, 5))

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

    fun setReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsDataStore.setReminderEnabled(enabled)
        if (enabled) {
            ReminderScheduler.schedule(context, reminderHour.value, reminderMinute.value)
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        settingsDataStore.setReminderTime(hour, minute)
        if (reminderEnabled.value) {
            ReminderScheduler.schedule(context, hour, minute)
        }
    }

    fun setReminderDays(days: Set<Int>) = viewModelScope.launch {
        settingsDataStore.setReminderDays(days)
    }

    private fun seedExercisesIfNeeded() = viewModelScope.launch {
        exerciseRepository.seedExercises(ExerciseSeed.exercises)
    }
}
