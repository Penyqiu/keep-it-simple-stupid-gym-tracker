package com.gymtracker.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_USE_KG = booleanPreferencesKey("use_kg")
        val KEY_REST_TIMER_SECONDS = intPreferencesKey("rest_timer_seconds")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val darkTheme: Flow<Boolean?> = context.dataStore.data.map { it[KEY_DARK_THEME] }
    val useKg: Flow<Boolean> = context.dataStore.data.map { it[KEY_USE_KG] ?: true }
    val restTimerSeconds: Flow<Int> = context.dataStore.data.map { it[KEY_REST_TIMER_SECONDS] ?: 90 }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setUseKg(useKg: Boolean) {
        context.dataStore.edit { it[KEY_USE_KG] = useKg }
    }

    suspend fun setRestTimerSeconds(seconds: Int) {
        context.dataStore.edit { it[KEY_REST_TIMER_SECONDS] = seconds }
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }
}
