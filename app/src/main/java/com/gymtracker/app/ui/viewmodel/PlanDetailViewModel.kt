package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.db.entity.WorkoutPlanEntity
import com.gymtracker.app.data.repository.ExerciseRepository
import com.gymtracker.app.data.repository.WorkoutPlanRepository
import com.gymtracker.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _planId = MutableStateFlow<Long?>(null)

    val plan: StateFlow<WorkoutPlanEntity?> = _planId
        .filterNotNull()
        .flatMapLatest { id -> workoutPlanRepository.getPlanByIdFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val exercises: StateFlow<List<ExerciseEntity>> = _planId
        .filterNotNull()
        .flatMapLatest { workoutPlanRepository.getExercisesForPlan(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allExercises = exerciseRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val scheduledDays: StateFlow<Set<Int>> = plan
        .map { p ->
            p?.daysOfWeek
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet() ?: emptySet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun loadPlan(planId: Long) {
        _planId.value = planId
    }

    fun toggleScheduledDay(day: Int) = viewModelScope.launch {
        val currentPlan = plan.value ?: return@launch
        val newDays = if (day in scheduledDays.value)
            scheduledDays.value - day
        else
            scheduledDays.value + day
        workoutPlanRepository.updatePlan(
            currentPlan.copy(daysOfWeek = newDays.sorted().joinToString(","))
        )
    }

    fun addExercise(exerciseId: Long) = viewModelScope.launch {
        val planId = _planId.value ?: return@launch
        workoutPlanRepository.addExerciseToPlan(planId, exerciseId)
    }

    fun removeExercise(exerciseId: Long) = viewModelScope.launch {
        val planId = _planId.value ?: return@launch
        workoutPlanRepository.removeExerciseFromPlan(planId, exerciseId)
    }

    fun reorderExercises(exercises: List<ExerciseEntity>) = viewModelScope.launch {
        val planId = _planId.value ?: return@launch
        workoutPlanRepository.reorderExercises(planId, exercises)
    }

    fun startWorkout(planId: Long, planName: String, onSessionCreated: (Long) -> Unit) =
        viewModelScope.launch {
            val sessionId = workoutRepository.startSession(planId, planName)
            onSessionCreated(sessionId)
        }
}
