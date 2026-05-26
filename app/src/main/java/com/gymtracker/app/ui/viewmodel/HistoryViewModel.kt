package com.gymtracker.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.WorkoutSessionEntity
import com.gymtracker.app.data.db.entity.WorkoutSetEntity
import com.gymtracker.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val allSessions = workoutRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionsByDate: StateFlow<Map<String, List<WorkoutSessionEntity>>> = allSessions
        .map { sessions ->
            sessions.filter { it.finishedAt != null }.groupBy { session ->
                val date = Instant.ofEpochMilli(session.startedAt)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val workoutDates: StateFlow<Set<java.time.LocalDate>> = allSessions
        .map { sessions ->
            sessions.filter { it.finishedAt != null }.map { session ->
                Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault()).toLocalDate()
            }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun deleteSession(session: WorkoutSessionEntity) = viewModelScope.launch {
        workoutRepository.deleteSession(session)
    }

    fun getVolumeForSession(sessionId: Long, onResult: (Double) -> Unit) = viewModelScope.launch {
        onResult(workoutRepository.getVolumeForSession(sessionId))
    }

    fun exportToCsv(context: Context, uri: Uri) = viewModelScope.launch {
        val sessions = allSessions.value.filter { it.finishedAt != null }
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write("date,plan,exercise,set,weight_kg,reps\n")
            sessions.forEach { session ->
                val date = Instant.ofEpochMilli(session.startedAt)
                    .atZone(ZoneId.systemDefault()).format(dateFormat)
                val planName = session.planName ?: "Quick Workout"
                val sets = workoutRepository.getSetsForSessionOnce(session.id)
                sets.forEach { set ->
                    writer.write("$date,$planName,${set.exerciseName},${set.setNumber},${set.weight},${set.reps}\n")
                }
            }
        }
    }

    fun repeatWorkout(session: WorkoutSessionEntity, onSessionCreated: (Long) -> Unit) =
        viewModelScope.launch {
            val newSessionId = workoutRepository.startSession(session.planId, session.planName)
            val originalSets = workoutRepository.getSetsForSessionOnce(session.id)
            originalSets.forEach { set ->
                workoutRepository.addSet(
                    newSessionId, set.exerciseId, set.exerciseName,
                    set.setNumber, set.weight, set.reps
                )
            }
            onSessionCreated(newSessionId)
        }
}
