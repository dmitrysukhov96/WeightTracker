package com.dmitrysukhov.weighttracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weightEntry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteWeightById(id: Int)

    @Query("SELECT * FROM weight_entries WHERE date >= :start AND date < :end ORDER BY date ASC")
    fun getWeightEntriesForMonth(start: Long, end: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE date >= :start AND date < :end LIMIT 1")
    fun getWeightEntryByDate(start: Long, end: Long): Flow<WeightEntry?>
}