package com.gymtracker.app.data.db.dao

import androidx.room.*
import com.gymtracker.app.data.db.entity.BodyWeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {
    @Query("SELECT * FROM body_weight_entries ORDER BY recordedAt DESC")
    fun getAllEntries(): Flow<List<BodyWeightEntity>>

    @Insert
    suspend fun insertEntry(entry: BodyWeightEntity): Long

    @Delete
    suspend fun deleteEntry(entry: BodyWeightEntity)

    @Query("SELECT * FROM body_weight_entries ORDER BY id")
    suspend fun getAllEntriesOnce(): List<BodyWeightEntity>

    @Query("DELETE FROM body_weight_entries")
    suspend fun deleteAllEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForRestore(entry: BodyWeightEntity)
}
