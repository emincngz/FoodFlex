package com.example.foodflex.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs_table")
data class MealLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String, // Öğünün kime ait olduğunu belirtir.
    val foodName: String,
    val userPortionQuantity: Double,
    val userPortionUnit: String,
    val calculatedCalories: Double,
    val calculatedProtein: Double,
    val calculatedCarbs: Double,
    val calculatedFat: Double,
    val basePortionDescription: String,
    val basePortionGrams: Double?,
    val timestamp: Long = System.currentTimeMillis()
)