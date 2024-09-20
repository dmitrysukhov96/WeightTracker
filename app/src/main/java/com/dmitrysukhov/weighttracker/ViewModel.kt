package com.dmitrysukhov.weighttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WeightViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase = Room.databaseBuilder(
        application, AppDatabase::class.java, "weight_db"
    ).build()
    val weightDao = database.weightDao()

    fun insertWeight(weightEntry: WeightEntry) = viewModelScope.launch {
        weightDao.insertWeight(weightEntry)
    }

    fun getWeightEntriesForMonth(start: Long, end: Long): Flow<List<WeightEntry>> {
        return weightDao.getWeightEntriesForMonth(start, end)
    }

    fun deleteWeight(weightEntry: WeightEntry) = viewModelScope.launch {
        weightDao.deleteWeightById(weightEntry.id)
    }
}
