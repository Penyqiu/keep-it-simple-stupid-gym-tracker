package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.BodyWeightEntity
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.db.entity.WorkoutSetEntity
import com.gymtracker.app.data.repository.BodyWeightRepository
import com.gymtracker.app.data.repository.ExerciseRepository
import com.gymtracker.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseProgress(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutSetEntity>,
    val personalRecord: Double,
    val estimatedOneRepMax: Double
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val bodyWeightRepository: BodyWeightRepository
) : ViewModel() {

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId

    val exercisesWithHistory: StateFlow<List<ExerciseEntity>> = flow {
        val ids = workoutRepository.getExercisesWithHistory()
        exerciseRepository.getAllExercises()
            .collect { all -> emit(all.filter { it.id in ids }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedExerciseProgress: StateFlow<ExerciseProgress?> = _selectedExerciseId
        .filterNotNull()
        .flatMapLatest { exerciseId ->
            workoutRepository.getSetHistoryForExercise(exerciseId).map { sets ->
                val exercise = exerciseRepository.getExerciseById(exerciseId) ?: return@map null
                val pr = sets.maxOfOrNull { it.weight } ?: 0.0
                val bestSet = sets.maxByOrNull { it.weight }
                val estimated1rm = if (bestSet != null && bestSet.reps > 0)
                    estimatedOneRepMax(bestSet.weight, bestSet.reps) else 0.0
                ExerciseProgress(exercise, sets, pr, estimated1rm)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val bodyWeightEntries = bodyWeightRepository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectExercise(exerciseId: Long) {
        _selectedExerciseId.value = exerciseId
    }

    fun addBodyWeight(weight: Double) = viewModelScope.launch {
        bodyWeightRepository.addEntry(weight)
    }

    fun deleteBodyWeightEntry(entry: BodyWeightEntity) = viewModelScope.launch {
        bodyWeightRepository.deleteEntry(entry)
    }

    // Epley formula
    private fun estimatedOneRepMax(weight: Double, reps: Int): Double =
        if (reps == 1) weight else weight * (1 + reps / 30.0)
}
