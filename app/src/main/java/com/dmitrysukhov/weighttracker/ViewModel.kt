package com.dmitrysukhov.weighttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.joda.time.DateTime

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

    fun addFoodToWeightEntry(gramsToAdd: Int) = viewModelScope.launch {
        val todayStart = DateTime.now().withTimeAtStartOfDay().toDate().time
        val todayEnd = todayStart + (1000 * 60 * 60 * 24)
        val existingEntry = weightDao.getWeightEntryByDate(todayStart, todayEnd).firstOrNull()
        if (existingEntry != null) {
            val updatedEntry = existingEntry.copy(grams = existingEntry.grams + gramsToAdd)
            weightDao.insertWeight(updatedEntry)
        } else {
            val newEntry = WeightEntry(
                date = todayStart, weight = 0f, noSugar = false, noBread = false,
                grams = gramsToAdd, failedDiet = false
            )
            weightDao.insertWeight(newEntry)
        }
    }

    fun getWeightEntryForDate(date: Long?): Flow<WeightEntry?> {
        val startOfDay = DateTime(date).withTimeAtStartOfDay().millis
        val endOfDay = DateTime(date).plusDays(1).withTimeAtStartOfDay().millis
        return weightDao.getWeightEntryByDate(startOfDay, endOfDay)
    }
}
