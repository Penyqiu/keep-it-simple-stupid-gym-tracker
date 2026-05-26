package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.repository.WorkoutPlanRepository
import com.gymtracker.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalWorkouts: Int = 0,
    val currentStreak: Int = 0,
    val totalVolume: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    val recentSessions = workoutRepository.getRecentSessions(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allPlans = workoutPlanRepository.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = MutableStateFlow(HomeUiState())

    init {
        refreshStats()
    }

    fun refreshStats() = viewModelScope.launch {
        uiState.value = HomeUiState(
            totalWorkouts = workoutRepository.getTotalWorkoutCount(),
            currentStreak = workoutRepository.getCurrentStreak(),
            totalVolume = workoutRepository.getTotalVolume()
        )
    }

    fun startQuickWorkout(onSessionCreated: (Long) -> Unit) = viewModelScope.launch {
        val sessionId = workoutRepository.startSession()
        onSessionCreated(sessionId)
    }

    fun startPlanWorkout(planId: Long, planName: String, onSessionCreated: (Long) -> Unit) =
        viewModelScope.launch {
            val sessionId = workoutRepository.startSession(planId, planName)
            onSessionCreated(sessionId)
        }
}
