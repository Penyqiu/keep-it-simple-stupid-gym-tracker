package com.gymtracker.app.data.repository

import com.gymtracker.app.data.db.dao.SessionDao
import com.gymtracker.app.data.db.entity.WorkoutSessionEntity
import com.gymtracker.app.data.db.entity.WorkoutSetEntity
import com.gymtracker.app.data.db.model.SessionWithStats
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>> = sessionDao.getAllSessions()

    fun getRecentSessions(limit: Int = 5): Flow<List<WorkoutSessionEntity>> =
        sessionDao.getRecentSessions(limit)

    fun getRecentSessionsWithStats(limit: Int = 5): Flow<List<SessionWithStats>> =
        sessionDao.getRecentSessionsWithStats(limit)

    suspend fun getSessionById(id: Long): WorkoutSessionEntity? = sessionDao.getSessionById(id)

    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>> =
        sessionDao.getSetsForSession(sessionId)

    suspend fun getSetsForSessionOnce(sessionId: Long): List<WorkoutSetEntity> =
        sessionDao.getSetsForSessionOnce(sessionId)

    suspend fun startSession(planId: Long? = null, planName: String? = null): Long =
        sessionDao.insertSession(WorkoutSessionEntity(planId = planId, planName = planName))

    suspend fun finishSession(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(session.copy(finishedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(session: WorkoutSessionEntity) = sessionDao.deleteSession(session)

    suspend fun addSet(
        sessionId: Long,
        exerciseId: Long,
        exerciseName: String,
        setNumber: Int,
        weight: Double,
        reps: Int
    ): Long = sessionDao.insertSet(
        WorkoutSetEntity(
            sessionId = sessionId,
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            setNumber = setNumber,
            weight = weight,
            reps = reps
        )
    )

    suspend fun updateSet(set: WorkoutSetEntity) = sessionDao.updateSet(set)

    suspend fun deleteSet(set: WorkoutSetEntity) = sessionDao.deleteSet(set)

    suspend fun getTotalWorkoutCount(): Int = sessionDao.getTotalWorkoutCount()

    suspend fun getTotalVolume(): Double = sessionDao.getTotalVolume() ?: 0.0

    suspend fun getVolumeForSession(sessionId: Long): Double =
        sessionDao.getVolumeForSession(sessionId) ?: 0.0

    suspend fun getCurrentStreak(): Int {
        val dates = sessionDao.getAllWorkoutDates()
            .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct()
            .sortedDescending()
        if (dates.isEmpty()) return 0
        var streak = 0
        var expected = java.time.LocalDate.now()
        for (date in dates) {
            if (date == expected || date == expected.minusDays(1)) {
                streak++
                expected = date.minusDays(1)
            } else break
        }
        return streak
    }

    suspend fun getWorkoutDaysInRange(since: Long): List<Long> =
        sessionDao.getWorkoutDaysInRange(since)

    fun getSetHistoryForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>> =
        sessionDao.getSetHistoryForExercise(exerciseId)

    suspend fun getMaxWeightForExercise(exerciseId: Long): Double? =
        sessionDao.getMaxWeightForExercise(exerciseId)

    suspend fun getSessionCountForExercise(exerciseId: Long): Int =
        sessionDao.getSessionCountForExercise(exerciseId)

    suspend fun getMaxSingleSetWeight(): Double? = sessionDao.getMaxSingleSetWeight()

    suspend fun getMaxWorkoutsInAnyWeek(): Int {
        val allDates = sessionDao.getAllWorkoutDates()
        if (allDates.isEmpty()) return 0
        val byWeek = allDates.groupBy {
            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            date.with(java.time.DayOfWeek.MONDAY)
        }
        return byWeek.values.maxOf { it.size }
    }

    suspend fun getExercisesWithHistory(): List<Long> = sessionDao.getExercisesWithHistory()

    suspend fun getPreviousSessionSets(exerciseId: Long, excludeSessionId: Long): List<WorkoutSetEntity> =
        sessionDao.getPreviousSessionSets(exerciseId, excludeSessionId)

    fun getTotalWorkoutCountFlow(): Flow<Int> = sessionDao.getTotalWorkoutCountFlow()

    suspend fun getMaxWeightBeforeSession(exerciseId: Long, sessionId: Long): Double? =
        sessionDao.getMaxWeightBeforeSession(exerciseId, sessionId)

    suspend fun getLongestStreak(): Int {
        val dates = sessionDao.getAllWorkoutDates()
            .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
            .distinct()
            .sorted()
        if (dates.isEmpty()) return 0
        var maxStreak = 1
        var current = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                if (current > maxStreak) maxStreak = current
            } else {
                current = 1
            }
        }
        return maxStreak
    }

    suspend fun getFavouriteDayOfWeek(): String? {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dates = sessionDao.getAllWorkoutDates()
            .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
        if (dates.isEmpty()) return null
        val best = dates.groupBy { it.dayOfWeek.value - 1 }.maxByOrNull { it.value.size }?.key ?: return null
        return dayNames[best]
    }

    suspend fun getTopExercises(limit: Int = 5): List<Pair<String, Int>> =
        sessionDao.getAllCompletedSetExerciseNames()
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)

    suspend fun checkAndUnlockAchievements(achievementRepository: AchievementRepository) {
        val totalWorkouts = getTotalWorkoutCount()
        val streak = getCurrentStreak()
        val totalVolume = getTotalVolume()
        val maxWeight = getMaxSingleSetWeight() ?: 0.0
        val maxWeekly = getMaxWorkoutsInAnyWeek()

        val toUnlock = mutableListOf<String>()

        if (totalWorkouts >= 1) toUnlock.add("workout_1")
        if (totalWorkouts >= 10) toUnlock.add("workout_10")
        if (totalWorkouts >= 50) toUnlock.add("workout_50")
        if (totalWorkouts >= 100) toUnlock.add("workout_100")

        if (streak >= 3) toUnlock.add("streak_3")
        if (streak >= 7) toUnlock.add("streak_7")
        if (streak >= 30) toUnlock.add("streak_30")

        if (totalVolume >= 10_000) toUnlock.add("volume_10k")
        if (totalVolume >= 100_000) toUnlock.add("volume_100k")
        if (totalVolume >= 1_000_000) toUnlock.add("volume_1m")

        if (maxWeight >= 100) toUnlock.add("weight_100kg")
        if (maxWeekly >= 5) toUnlock.add("weekly_5")

        toUnlock.forEach { achievementRepository.unlock(it) }
    }
}
