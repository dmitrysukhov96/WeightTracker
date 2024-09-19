package com.dmitrysukhov.weighttracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val date: Long, val weight: Float,
    val noSugar: Boolean, val noBread: Boolean, val grams: Int
)