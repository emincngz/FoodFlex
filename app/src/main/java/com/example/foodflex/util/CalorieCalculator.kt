package com.example.foodflex.util

object CalorieCalculator {

    // Mifflin-St Jeor formülüne göre Bazal Metabolizma Hızı (BMR) hesaplar
    fun calculateBmr(gender: String, weightKg: Double, heightCm: Double, age: Int): Double {
        return if (gender.equals("Erkek", ignoreCase = true)) {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        } else { // Kadın veya diğer
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
        }
    }

    // Toplam Günlük Enerji Harcamasını (TDEE) hesaplar
    fun calculateTdee(bmr: Double, activityLevel: String): Double {
        val multiplier = when (activityLevel.lowercase()) {
            "sedanter (az veya hiç egzersiz)" -> 1.2
            "hafif aktif (1-3 gün/hafta egzersiz)" -> 1.375
            "orta derecede aktif (3-5 gün/hafta egzersiz)" -> 1.55
            "çok aktif (6-7 gün/hafta egzersiz)" -> 1.725
            "ekstra aktif (çok ağır egzersiz/fiziksel iş)" -> 1.9
            else -> 1.2 // Varsayılan
        }
        return bmr * multiplier
    }

    // Kilo hedefine göre günlük kalori hedefini ayarlar
    fun calculateCalorieGoal(tdee: Double, goal: String): Double {
        return when (goal.lowercase()) {
            "kilo vermek" -> tdee - 500 // Günde 500 kcal açık (haftada yaklaşık 0.5 kg)
            "kilo almak" -> tdee + 500 // Günde 500 kcal fazlalık
            "kilosunu korumak" -> tdee
            else -> tdee // Varsayılan
        }
    }

    // Kalori hedefine göre Makro (Protein, Karb, Yağ) dağılımını gram olarak hesaplar
    // Oranlar: %40 Karbonhidrat, %30 Protein, %30 Yağ (bu oranlar değiştirilebilir)
    fun calculateMacros(calorieGoal: Double): Map<String, Double> {
        val carbs = (calorieGoal * 0.40) / 4 // 1g karbonhidrat = 4 kcal
        val protein = (calorieGoal * 0.30) / 4 // 1g protein = 4 kcal
        val fat = (calorieGoal * 0.30) / 9   // 1g yağ = 9 kcal
        return mapOf(
            "protein" to protein,
            "carbs" to carbs,
            "fat" to fat
        )
    }
}