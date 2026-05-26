package com.gymtracker.app.data.db.dao

import androidx.room.*
import com.gymtracker.app.data.db.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY muscleGroup, name")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :group ORDER BY name")
    fun getExercisesByMuscleGroup(group: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT DISTINCT muscleGroup FROM exercises ORDER BY muscleGroup")
    fun getAllMuscleGroups(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    @Query("SELECT name FROM exercises WHERE isCustom = 0")
    suspend fun getSeedExerciseNames(): List<String>

    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY id")
    suspend fun getCustomExercisesOnce(): List<ExerciseEntity>

    @Query("DELETE FROM exercises WHERE isCustom = 1")
    suspend fun deleteAllCustomExercises()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForRestore(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET description = :description WHERE name = :name AND isCustom = 0")
    suspend fun updateDescriptionByName(name: String, description: String)
}
