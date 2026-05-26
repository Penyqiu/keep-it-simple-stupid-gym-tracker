package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.repository.ExerciseRepository
import com.gymtracker.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetail(
    val exercise: ExerciseEntity,
    val personalRecord: Double?,
    val sessionCount: Int
)

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val allExercises = exerciseRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val muscleGroups = exerciseRepository.getAllMuscleGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedMuscleGroup = MutableStateFlow<String?>(null)
    val selectedMuscleGroup: StateFlow<String?> = _selectedMuscleGroup

    val filteredExercises: StateFlow<List<ExerciseEntity>> = combine(
        allExercises, _selectedMuscleGroup
    ) { exercises, group ->
        if (group == null) exercises else exercises.filter { it.muscleGroup == group }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDetail = MutableStateFlow<ExerciseDetail?>(null)
    val selectedDetail: StateFlow<ExerciseDetail?> = _selectedDetail

    fun selectMuscleGroup(group: String?) {
        _selectedMuscleGroup.value = group
    }

    fun showDetail(exercise: ExerciseEntity) = viewModelScope.launch {
        val pr = workoutRepository.getMaxWeightForExercise(exercise.id)
        val count = workoutRepository.getSessionCountForExercise(exercise.id)
        _selectedDetail.value = ExerciseDetail(exercise, pr, count)
    }

    fun clearDetail() {
        _selectedDetail.value = null
    }

    fun addCustomExercise(name: String, muscleGroup: String) = viewModelScope.launch {
        exerciseRepository.addExercise(name, muscleGroup, isCustom = true)
    }

    fun deleteExercise(exercise: ExerciseEntity) = viewModelScope.launch {
        exerciseRepository.deleteExercise(exercise)
    }
}
