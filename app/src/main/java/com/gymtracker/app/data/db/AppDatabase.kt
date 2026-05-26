package com.gymtracker.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymtracker.app.data.db.dao.*
import com.gymtracker.app.data.db.entity.*
import com.gymtracker.app.data.db.migrations.MIGRATION_1_2
import com.gymtracker.app.data.db.migrations.MIGRATION_2_3

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        PlanExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        BodyWeightEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun sessionDao(): SessionDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "gymtracker.db"
        val migrations = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
