package com.example.foodflex.data

// Bu dosya, sadece ViewModel ve Fragment'lar arasında veri taşımak için
// kullanılan basit veri sınıflarını içerir.

data class SingleDayTotals(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0
)

data class DailyGoal(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)