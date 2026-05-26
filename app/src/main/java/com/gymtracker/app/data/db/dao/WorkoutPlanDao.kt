package com.gymtracker.app.data.db.dao

import androidx.room.*
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.db.entity.PlanExerciseEntity
import com.gymtracker.app.data.db.entity.WorkoutPlanEntity
import com.gymtracker.app.data.db.model.PlanWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("""
        SELECT p.id, p.name, p.daysOfWeek, p.createdAt,
               COUNT(DISTINCT pe.id) as exerciseCount
        FROM workout_plans p
        LEFT JOIN plan_exercises pe ON pe.planId = p.id
        GROUP BY p.id
        ORDER BY p.createdAt DESC
    """)
    fun getAllPlansWithCount(): Flow<List<PlanWithCount>>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    fun getPlanByIdFlow(id: Long): Flow<WorkoutPlanEntity?>

    @Insert
    suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: WorkoutPlanEntity)

    @Delete
    suspend fun deletePlan(plan: WorkoutPlanEntity)

    @Query("SELECT e.* FROM exercises e INNER JOIN plan_exercises pe ON e.id = pe.exerciseId WHERE pe.planId = :planId ORDER BY pe.orderIndex")
    fun getExercisesForPlan(planId: Long): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM plan_exercises WHERE planId = :planId ORDER BY orderIndex")
    suspend fun getPlanExercisesRaw(planId: Long): List<PlanExerciseEntity>

    @Insert
    suspend fun addExerciseToPlan(planExercise: PlanExerciseEntity)

    @Delete
    suspend fun removeExerciseFromPlan(planExercise: PlanExerciseEntity)

    @Query("DELETE FROM plan_exercises WHERE planId = :planId AND exerciseId = :exerciseId")
    suspend fun removeExerciseFromPlanByIds(planId: Long, exerciseId: Long)

    @Update
    suspend fun updatePlanExercise(planExercise: PlanExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePlanExercises(planExercises: List<PlanExerciseEntity>)

    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    suspend fun getAllPlansOnce(): List<WorkoutPlanEntity>

    @Query("SELECT * FROM plan_exercises ORDER BY planId, orderIndex")
    suspend fun getAllPlanExercises(): List<PlanExerciseEntity>

    @Query("DELETE FROM workout_plans")
    suspend fun deleteAllPlans()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanForRestore(plan: WorkoutPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanExerciseForRestore(planExercise: PlanExerciseEntity)
}
