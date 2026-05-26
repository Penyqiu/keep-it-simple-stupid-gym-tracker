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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutExercise(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<WorkoutSetEntity> = emptyList(),
    val previousSets: List<WorkoutSetEntity> = emptyList(),
    val suggestedWeight: Double? = null
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

    private val _restSecondsLeft = MutableStateFlow(0)
    val restSecondsLeft: StateFlow<Int> = _restSecondsLeft

    private val _restTotalSeconds = MutableStateFlow(0)
    val restTotalSeconds: StateFlow<Int> = _restTotalSeconds

    private var timerJob: kotlinx.coroutines.Job? = null

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
                    val prevSets = prevSetsMap[exercise.id] ?: emptyList()
                    WorkoutExercise(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        previousSets = prevSets,
                        suggestedWeight = calculateSuggestedWeight(prevSets)
                    )
                }
            }
            // Collect sets only after plan exercises are loaded to avoid race condition
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
                current + WorkoutExercise(
                    exerciseId = exerciseId,
                    exerciseName = exerciseName,
                    previousSets = previousSets,
                    suggestedWeight = calculateSuggestedWeight(previousSets)
                )
            } else current
        }
    }

    private fun calculateSuggestedWeight(previousSets: List<WorkoutSetEntity>): Double? {
        if (previousSets.isEmpty()) return null
        val maxWeight = previousSets.maxOf { it.weight }
        val allHitTarget = previousSets.all { it.reps >= 5 }
        return if (allHitTarget) roundToNearestPlate(maxWeight + 2.5) else maxWeight
    }

    private fun roundToNearestPlate(weight: Double): Double =
        Math.round(weight / 2.5) * 2.5

    fun finishWorkout(onDone: (Long) -> Unit) = viewModelScope.launch {
        val sessionId = _sessionId.value ?: return@launch
        workoutRepository.finishSession(sessionId)
        workoutRepository.checkAndUnlockAchievements(achievementRepository)
        stopRestTimer()
        updateWidget()
        onDone(sessionId)
    }

    private fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _restTimerRunning.value = true
        _restTotalSeconds.value = seconds
        _restSecondsLeft.value = seconds
        timerJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _restSecondsLeft.value = remaining
                delay(1_000)
            }
            _restTimerRunning.value = false
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _restTimerRunning.value = false
        _restSecondsLeft.value = 0
    }

    private fun vibrateOnComplete() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun updateWidget() = viewModelScope.launch {
        com.gymtracker.app.widget.GymTrackerWidget.updateAll(context)
    }
}
