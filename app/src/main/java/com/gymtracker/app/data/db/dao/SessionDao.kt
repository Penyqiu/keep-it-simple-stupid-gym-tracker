package com.gymtracker.app.data.db.dao

import androidx.room.*
import com.gymtracker.app.data.db.entity.WorkoutSessionEntity
import com.gymtracker.app.data.db.entity.WorkoutSetEntity
import com.gymtracker.app.data.db.model.SessionWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 5): Flow<List<WorkoutSessionEntity>>

    @Query("""
        SELECT s.id, s.planId, s.planName, s.startedAt, s.finishedAt,
               COUNT(DISTINCT ws.exerciseId) as exerciseCount,
               COALESCE(SUM(ws.weight * ws.reps), 0.0) as volume
        FROM workout_sessions s
        LEFT JOIN workout_sets ws ON ws.sessionId = s.id AND ws.isCompleted = 1
        WHERE s.finishedAt IS NOT NULL
        GROUP BY s.id
        ORDER BY s.startedAt DESC
        LIMIT :limit
    """)
    fun getRecentSessionsWithStats(limit: Int): Flow<List<SessionWithStats>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setNumber")
    suspend fun getSetsForSessionOnce(sessionId: Long): List<WorkoutSetEntity>

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE finishedAt IS NOT NULL")
    suspend fun getTotalWorkoutCount(): Int

    @Query("SELECT SUM(weight * reps) FROM workout_sets ws INNER JOIN workout_sessions s ON ws.sessionId = s.id WHERE s.finishedAt IS NOT NULL")
    suspend fun getTotalVolume(): Double?

    @Query("SELECT SUM(weight * reps) FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getVolumeForSession(sessionId: Long): Double?

    @Query("SELECT startedAt FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC")
    suspend fun getAllWorkoutDates(): List<Long>

    @Query("SELECT startedAt FROM workout_sessions WHERE finishedAt IS NOT NULL AND startedAt >= :since ORDER BY startedAt ASC")
    suspend fun getWorkoutDaysInRange(since: Long): List<Long>

    @Query("SELECT MAX(weight) FROM workout_sets ws INNER JOIN workout_sessions s ON ws.sessionId = s.id WHERE ws.exerciseId = :exerciseId AND s.finishedAt IS NOT NULL")
    suspend fun getMaxWeightForExercise(exerciseId: Long): Double?

    @Query("SELECT MAX(weight) FROM workout_sets ws INNER JOIN workout_sessions s ON ws.sessionId = s.id WHERE s.finishedAt IS NOT NULL")
    suspend fun getMaxSingleSetWeight(): Double?

    @Query("""
        SELECT COUNT(DISTINCT DATE(startedAt / 1000, 'unixepoch'))
        FROM workout_sessions
        WHERE finishedAt IS NOT NULL
        AND startedAt >= :weekStart AND startedAt < :weekEnd
    """)
    suspend fun getWorkoutsInWeek(weekStart: Long, weekEnd: Long): Int

    @Query("""
        SELECT * FROM workout_sets
        WHERE exerciseId = :exerciseId
        AND sessionId != :excludeSessionId
        AND sessionId IN (SELECT id FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC LIMIT 1)
        ORDER BY setNumber
    """)
    suspend fun getPreviousSessionSets(exerciseId: Long, excludeSessionId: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND sessionId IN (SELECT id FROM workout_sessions WHERE finishedAt IS NOT NULL) ORDER BY timestamp DESC")
    fun getSetHistoryForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT DISTINCT exerciseId FROM workout_sets WHERE sessionId IN (SELECT id FROM workout_sessions WHERE finishedAt IS NOT NULL)")
    suspend fun getExercisesWithHistory(): List<Long>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE finishedAt IS NOT NULL")
    fun getTotalWorkoutCountFlow(): Flow<Int>

    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :exerciseId AND sessionId IN (SELECT id FROM workout_sessions WHERE finishedAt IS NOT NULL AND id != :excludeSessionId)")
    suspend fun getMaxWeightBeforeSession(exerciseId: Long, excludeSessionId: Long): Double?

    @Query("SELECT exerciseName FROM workout_sets WHERE sessionId IN (SELECT id FROM workout_sessions WHERE finishedAt IS NOT NULL)")
    suspend fun getAllCompletedSetExerciseNames(): List<String>

    @Query("SELECT * FROM workout_sessions ORDER BY id")
    suspend fun getAllSessionsOnce(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sets ORDER BY id")
    suspend fun getAllSetsOnce(): List<WorkoutSetEntity>

    @Query("SELECT COUNT(DISTINCT sessionId) FROM workout_sets WHERE exerciseId = :exerciseId AND sessionId IN (SELECT id FROM workout_sessions WHERE finishedAt IS NOT NULL)")
    suspend fun getSessionCountForExercise(exerciseId: Long): Int

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionForRestore(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetForRestore(set: WorkoutSetEntity)
}
