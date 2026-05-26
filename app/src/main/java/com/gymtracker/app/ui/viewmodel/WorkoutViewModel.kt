package com.gymtracker.app.ui.viewmodel

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.db.entity.WorkoutSetEntity
import com.gymtracker.app.data.repository.AchievementRepository
import com.gymtracker.app.data.repository.ExerciseRepository
import com.gymtracker.app.data.repository.WorkoutPlanRepository
import com.gymtracker.app.data.repository.WorkoutRepository
import com.gymtracker.app.service.RestTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutExercise(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<WorkoutSetEntity> = emptyList(),
    val previousSets: List<WorkoutSetEntity> = emptyList()
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val achievementRepository: AchievementRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sessionId = MutableStateFlow<Long?>(null)
    val sessionId: StateFlow<Long?> = _sessionId

    val allExercises = exerciseRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sets = _sessionId
        .filterNotNull()
        .flatMapLatest { workoutRepository.getSetsForSession(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _exercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val exercises: StateFlow<List<WorkoutExercise>> = _exercises

    private val _restTimerRunning = MutableStateFlow(false)
    val restTimerRunning: StateFlow<Boolean> = _restTimerRunning

    fun loadSession(sessionId: Long) {
        _sessionId.value = sessionId
        viewModelScope.launch {
            val session = workoutRepository.getSessionById(sessionId) ?: return@launch
            if (session.planId != null) {
                val planExercises = workoutPlanRepository.getExercisesForPlan(session.planId).first()
                val prevSetsMap = planExercises.associate { exercise ->
                    exercise.id to workoutRepository.getPreviousSessionSets(exercise.id, sessionId)
                }
                _exercises.value = planExercises.map { exercise ->
                    WorkoutExercise(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        previousSets = prevSetsMap[exercise.id] ?: emptyList()
                    )
                }
            }
        }
        viewModelScope.launch {
            sets.collect { allSets ->
                val grouped = allSets.groupBy { it.exerciseId }
                _exercises.update { current ->
                    if (current.isEmpty()) {
                        grouped.entries.map { (exerciseId, sets) ->
                            WorkoutExercise(
                                exerciseId = exerciseId,
                                exerciseName = sets.first().exerciseName,
                                sets = sets
                            )
                        }
                    } else {
                        current.map { exercise ->
                            exercise.copy(sets = grouped[exercise.exerciseId] ?: emptyList())
                        }
                    }
                }
            }
        }
    }

    fun addSet(exerciseId: Long, exerciseName: String, weight: Double, reps: Int) =
        viewModelScope.launch {
            val sessionId = _sessionId.value ?: return@launch
            val currentSets = sets.value.count { it.exerciseId == exerciseId }
            workoutRepository.addSet(sessionId, exerciseId, exerciseName, currentSets + 1, weight, reps)
        }

    fun toggleSetComplete(set: WorkoutSetEntity, restSeconds: Int) = viewModelScope.launch {
        workoutRepository.updateSet(set.copy(isCompleted = !set.isCompleted))
        if (!set.isCompleted) {
            vibrateOnComplete()
            startRestTimer(restSeconds)
        }
    }

    fun deleteSet(set: WorkoutSetEntity) = viewModelScope.launch {
        workoutRepository.deleteSet(set)
    }

    fun addExerciseToWorkout(exerciseId: Long, exerciseName: String) = viewModelScope.launch {
        val sessionId = _sessionId.value ?: return@launch
        val previousSets = workoutRepository.getPreviousSessionSets(exerciseId, sessionId)
        _exercises.update { current ->
            if (current.none { it.exerciseId == exerciseId }) {
                current + WorkoutExercise(exerciseId, exerciseName, previousSets = previousSets)
            } else current
        }
    }

    fun finishWorkout(onDone: () -> Unit) = viewModelScope.launch {
        val sessionId = _sessionId.value ?: return@launch
        workoutRepository.finishSession(sessionId)
        workoutRepository.checkAndUnlockAchievements(achievementRepository)
        stopRestTimer()
        updateWidget()
        onDone()
    }

    private fun startRestTimer(seconds: Int) {
        _restTimerRunning.value = true
        val intent = Intent(context, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_START
            putExtra(RestTimerService.EXTRA_DURATION, seconds)
        }
        context.startForegroundService(intent)
    }

    fun stopRestTimer() {
        _restTimerRunning.value = false
        context.startService(Intent(context, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_STOP
        })
    }

    private fun vibrateOnComplete() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun updateWidget() = viewModelScope.launch {
        com.gymtracker.app.widget.GymTrackerWidget.updateAll(context)
    }
}
