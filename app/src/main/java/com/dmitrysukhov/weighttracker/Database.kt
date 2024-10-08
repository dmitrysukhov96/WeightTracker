package com.dmitrysukhov.weighttracker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeightEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao
}