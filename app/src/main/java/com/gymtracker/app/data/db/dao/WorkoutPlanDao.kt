package com.gymtracker.app.data.db.dao

import androidx.room.*
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.db.entity.PlanExerciseEntity
import com.gymtracker.app.data.db.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): WorkoutPlanEntity?

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
}
