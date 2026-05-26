package com.gymtracker.app.di

import android.content.Context
import androidx.room.Room
import com.gymtracker.app.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(*AppDatabase.migrations)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideExerciseDao(db: AppDatabase) = db.exerciseDao()
    @Provides fun provideWorkoutPlanDao(db: AppDatabase) = db.workoutPlanDao()
    @Provides fun provideSessionDao(db: AppDatabase) = db.sessionDao()
    @Provides fun provideBodyWeightDao(db: AppDatabase) = db.bodyWeightDao()
    @Provides fun provideAchievementDao(db: AppDatabase) = db.achievementDao()
}
