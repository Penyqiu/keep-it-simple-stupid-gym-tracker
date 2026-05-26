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
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        // Comma-separated weekday numbers: 1=Mon … 7=Sun
        val KEY_REMINDER_DAYS = stringPreferencesKey("reminder_days")
    }

    val darkTheme: Flow<Boolean?> = context.dataStore.data.map { it[KEY_DARK_THEME] }
    val useKg: Flow<Boolean> = context.dataStore.data.map { it[KEY_USE_KG] ?: true }
    val restTimerSeconds: Flow<Int> = context.dataStore.data.map { it[KEY_REST_TIMER_SECONDS] ?: 90 }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }
    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_HOUR] ?: 8 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_MINUTE] ?: 0 }
    val reminderDays: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMINDER_DAYS]?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet()
            ?: setOf(1, 3, 5) // Mon, Wed, Fri by default
    }

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

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[KEY_REMINDER_HOUR] = hour
            it[KEY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setReminderDays(days: Set<Int>) {
        context.dataStore.edit { it[KEY_REMINDER_DAYS] = days.joinToString(",") }
    }
}
