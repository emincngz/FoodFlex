package com.example.foodflex.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.foodflex.data.*
import com.example.foodflex.util.CalorieCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

// Data class tanımları artık bu dosyadan kaldırıldı. Onlar data/UiModels.kt dosyasında.

class MealLogViewModel(application: Application) : AndroidViewModel(application) {

    private val mealLogDao: MealLogDao = AppDatabase.getDatabase(application).mealLogDao()
    private val userManager = UserProfileManager

    private val _currentUserEmail = MutableLiveData<String?>()

    val mealsForCurrentUser: LiveData<List<MealLog>> = _currentUserEmail.switchMap { email ->
        email?.let { mealLogDao.getMealsForUser(it) } ?: MutableLiveData(emptyList())
    }

    val dailyGoal: LiveData<DailyGoal> = _currentUserEmail.switchMap { email ->
        val liveData = MutableLiveData<DailyGoal>()
        email?.let {
            userManager.loadUserProfile(getApplication(), it)?.let { profile ->
                liveData.postValue(calculateDailyGoalForProfile(profile))
            }
        }
        liveData
    }

    private val _singleDayTotals = MutableLiveData<SingleDayTotals>()
    val singleDayTotals: LiveData<SingleDayTotals> = _singleDayTotals

    init {
        userManager.getCurrentUserEmail(application)?.let { loadDataForUser(it) }
    }

    fun loadDataForUser(email: String) {
        _currentUserEmail.postValue(email)
    }

    fun clearDataOnLogout() {
        _currentUserEmail.postValue(null)
    }

    fun insertMeal(mealLog: MealLog) = viewModelScope.launch(Dispatchers.IO) { mealLogDao.insertMeal(mealLog) }

    fun deleteMeal(mealLog: MealLog) = viewModelScope.launch(Dispatchers.IO) { mealLogDao.deleteMeal(mealLog) }

    fun deleteAllMealsForCurrentUser() {
        _currentUserEmail.value?.let { email ->
            viewModelScope.launch(Dispatchers.IO) {
                mealLogDao.deleteAllMealsForUser(email)
            }
        }
    }

    fun loadTotalsForDay(dateInMillis: Long) {
        _currentUserEmail.value?.let { email ->
            viewModelScope.launch(Dispatchers.IO) {
                val calendar = Calendar.getInstance().apply { timeInMillis = dateInMillis }
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0);
                val startTime = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59);
                val endTime = calendar.timeInMillis
                val mealsForDay = mealLogDao.getMealsBetweenDatesForUser(email, startTime, endTime)
                val totals = SingleDayTotals(
                    totalCalories = mealsForDay.sumOf { it.calculatedCalories },
                    totalProtein = mealsForDay.sumOf { it.calculatedProtein },
                    totalCarbs = mealsForDay.sumOf { it.calculatedCarbs },
                    totalFat = mealsForDay.sumOf { it.calculatedFat }
                )
                _singleDayTotals.postValue(totals)
            }
        }
    }

    private fun calculateDailyGoalForProfile(profile: UserProfile): DailyGoal {
        val bmr = CalorieCalculator.calculateBmr(profile.gender, profile.weightKg, profile.heightCm, profile.age)
        val tdee = CalorieCalculator.calculateTdee(bmr, profile.activityLevel)
        val calorieGoal = CalorieCalculator.calculateCalorieGoal(tdee, profile.goal)
        val macros = CalorieCalculator.calculateMacros(calorieGoal)
        return DailyGoal(calories = calorieGoal, protein = macros["protein"] ?: 0.0, carbs = macros["carbs"] ?: 0.0, fat = macros["fat"] ?: 0.0)
    }
}