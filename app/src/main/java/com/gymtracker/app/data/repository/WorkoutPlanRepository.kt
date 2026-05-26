package com.gymtracker.app.data.repository

import com.gymtracker.app.data.db.dao.WorkoutPlanDao
import com.gymtracker.app.data.db.entity.ExerciseEntity
import com.gymtracker.app.data.db.entity.PlanExerciseEntity
import com.gymtracker.app.data.db.entity.WorkoutPlanEntity
import com.gymtracker.app.data.db.model.PlanWithCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanRepository @Inject constructor(
    private val workoutPlanDao: WorkoutPlanDao
) {
    fun getAllPlans(): Flow<List<WorkoutPlanEntity>> = workoutPlanDao.getAllPlans()

    fun getAllPlansWithCount(): Flow<List<PlanWithCount>> = workoutPlanDao.getAllPlansWithCount()

    suspend fun getPlanById(id: Long): WorkoutPlanEntity? = workoutPlanDao.getPlanById(id)

    fun getPlanByIdFlow(id: Long): Flow<WorkoutPlanEntity?> = workoutPlanDao.getPlanByIdFlow(id)

    fun getExercisesForPlan(planId: Long): Flow<List<ExerciseEntity>> =
        workoutPlanDao.getExercisesForPlan(planId)

    suspend fun createPlan(name: String): Long =
        workoutPlanDao.insertPlan(WorkoutPlanEntity(name = name))

    suspend fun updatePlan(plan: WorkoutPlanEntity) = workoutPlanDao.updatePlan(plan)

    suspend fun deletePlan(plan: WorkoutPlanEntity) = workoutPlanDao.deletePlan(plan)

    suspend fun addExerciseToPlan(planId: Long, exerciseId: Long) {
        val existing = workoutPlanDao.getPlanExercisesRaw(planId)
        val nextOrder = (existing.maxOfOrNull { it.orderIndex } ?: -1) + 1
        workoutPlanDao.addExerciseToPlan(
            PlanExerciseEntity(planId = planId, exerciseId = exerciseId, orderIndex = nextOrder)
        )
    }

    suspend fun removeExerciseFromPlan(planId: Long, exerciseId: Long) =
        workoutPlanDao.removeExerciseFromPlanByIds(planId, exerciseId)

    suspend fun reorderExercises(planId: Long, exercises: List<ExerciseEntity>) {
        val raw = workoutPlanDao.getPlanExercisesRaw(planId)
        val reordered = exercises.mapIndexed { index, exercise ->
            raw.first { it.exerciseId == exercise.id }.copy(orderIndex = index)
        }
        workoutPlanDao.updatePlanExercises(reordered)
    }
}
