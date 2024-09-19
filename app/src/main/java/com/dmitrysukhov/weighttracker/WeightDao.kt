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

    @Query("SELECT * FROM weight_entries WHERE date >= :start AND date < :end ORDER BY date ASC")
    fun getWeightEntriesForMonth(start: Long, end: Long): Flow<List<WeightEntry>>
}