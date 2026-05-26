package com.gymtracker.app.data.repository

import com.gymtracker.app.data.db.dao.ExerciseDao
import com.gymtracker.app.data.db.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {
    fun getAllExercises(): Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    fun getExercisesByMuscleGroup(group: String): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByMuscleGroup(group)

    fun getAllMuscleGroups(): Flow<List<String>> = exerciseDao.getAllMuscleGroups()

    suspend fun getExerciseById(id: Long): ExerciseEntity? = exerciseDao.getExerciseById(id)

    suspend fun addExercise(name: String, muscleGroup: String, isCustom: Boolean = true): Long =
        exerciseDao.insertExercise(ExerciseEntity(name = name, muscleGroup = muscleGroup, isCustom = isCustom))

    suspend fun seedExercises(exercises: List<ExerciseEntity>) = exerciseDao.insertExercises(exercises)

    suspend fun deleteExercise(exercise: ExerciseEntity) = exerciseDao.deleteExercise(exercise)

    suspend fun countExercises(): Int = exerciseDao.countExercises()
}
