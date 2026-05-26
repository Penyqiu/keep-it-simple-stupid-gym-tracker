package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
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

    fun selectMuscleGroup(group: String?) {
        _selectedMuscleGroup.value = group
    }

    fun addCustomExercise(name: String, muscleGroup: String) = viewModelScope.launch {
        exerciseRepository.addExercise(name, muscleGroup, isCustom = true)
    }

    fun deleteExercise(exercise: ExerciseEntity) = viewModelScope.launch {
        exerciseRepository.deleteExercise(exercise)
    }
}
