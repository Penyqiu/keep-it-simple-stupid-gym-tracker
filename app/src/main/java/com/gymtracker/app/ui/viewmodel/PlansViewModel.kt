package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.WorkoutPlanEntity
import com.gymtracker.app.data.db.model.PlanWithCount
import com.gymtracker.app.data.repository.WorkoutPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    val plans = workoutPlanRepository.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val plansWithCount: kotlinx.coroutines.flow.StateFlow<List<PlanWithCount>> =
        workoutPlanRepository.getAllPlansWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createPlan(name: String, onCreated: (Long) -> Unit) = viewModelScope.launch {
        val id = workoutPlanRepository.createPlan(name)
        onCreated(id)
    }

    fun deletePlan(plan: WorkoutPlanEntity) = viewModelScope.launch {
        workoutPlanRepository.deletePlan(plan)
    }
}
