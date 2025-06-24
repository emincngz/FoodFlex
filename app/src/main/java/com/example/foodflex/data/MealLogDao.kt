package com.example.foodflex.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeal(mealLog: MealLog)

    @Delete
    suspend fun deleteMeal(mealLog: MealLog)

    @Query("SELECT * FROM meal_logs_table WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getMealsForUser(userEmail: String): LiveData<List<MealLog>>

    @Query("SELECT * FROM meal_logs_table WHERE userEmail = :userEmail AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getMealsBetweenDatesForUser(userEmail: String, startTime: Long, endTime: Long): List<MealLog>

    @Query("DELETE FROM meal_logs_table WHERE userEmail = :userEmail")
    suspend fun deleteAllMealsForUser(userEmail: String)
}