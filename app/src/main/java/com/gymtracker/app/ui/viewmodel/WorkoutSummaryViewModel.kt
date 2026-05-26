package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseSummaryItem(
    val exerciseName: String,
    val sets: Int,
    val totalVolume: Double,
    val maxWeight: Double,
    val previousMaxWeight: Double?,
    val isNewPR: Boolean
)

data class WorkoutSummaryData(
    val planName: String?,
    val durationMinutes: Long,
    val totalVolume: Double,
    val totalSets: Int,
    val exerciseItems: List<ExerciseSummaryItem>,
    val newPrCount: Int
)

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    private val _summary = MutableStateFlow<WorkoutSummaryData?>(null)
    val summary: StateFlow<WorkoutSummaryData?> = _summary

    init {
        viewModelScope.launch { loadSummary() }
    }

    private suspend fun loadSummary() {
        val session = workoutRepository.getSessionById(sessionId) ?: return
        val sets = workoutRepository.getSetsForSessionOnce(sessionId)

        val exerciseItems = sets
            .groupBy { it.exerciseId to it.exerciseName }
            .map { (key, exSets) ->
                val (exerciseId, exerciseName) = key
                val maxWeight = exSets.maxOf { it.weight }
                val prevMax = workoutRepository.getMaxWeightBeforeSession(exerciseId, sessionId)
                ExerciseSummaryItem(
                    exerciseName = exerciseName,
                    sets = exSets.size,
                    totalVolume = exSets.sumOf { it.weight * it.reps },
                    maxWeight = maxWeight,
                    previousMaxWeight = prevMax,
                    isNewPR = prevMax == null || maxWeight > prevMax
                )
            }

        _summary.value = WorkoutSummaryData(
            planName = session.planName,
            durationMinutes = ((session.finishedAt ?: System.currentTimeMillis()) - session.startedAt) / 60_000,
            totalVolume = sets.sumOf { it.weight * it.reps },
            totalSets = sets.size,
            exerciseItems = exerciseItems,
            newPrCount = exerciseItems.count { it.isNewPR }
        )
    }
}
