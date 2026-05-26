package com.gymtracker.app.data.repository

import com.gymtracker.app.data.db.dao.BodyWeightDao
import com.gymtracker.app.data.db.entity.BodyWeightEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyWeightRepository @Inject constructor(
    private val bodyWeightDao: BodyWeightDao
) {
    fun getAllEntries(): Flow<List<BodyWeightEntity>> = bodyWeightDao.getAllEntries()

    suspend fun addEntry(weight: Double) =
        bodyWeightDao.insertEntry(BodyWeightEntity(weight = weight))

    suspend fun deleteEntry(entry: BodyWeightEntity) = bodyWeightDao.deleteEntry(entry)
}
