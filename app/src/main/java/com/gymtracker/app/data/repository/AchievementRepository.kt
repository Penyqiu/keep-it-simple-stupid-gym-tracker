package com.gymtracker.app.data.repository

import com.gymtracker.app.data.db.dao.AchievementDao
import com.gymtracker.app.data.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    fun getAllAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    fun getUnlockedCount(): Flow<Int> = achievementDao.getUnlockedCount()

    suspend fun isUnlocked(id: String): Boolean = achievementDao.getAchievementById(id) != null

    suspend fun unlock(id: String) = achievementDao.unlockAchievement(AchievementEntity(id = id))
}
