package com.example.foodflex.data

import android.content.Context
import android.content.SharedPreferences

data class UserProfile(
    val name: String,
    val email: String,
    val age: Int,
    val gender: String,
    val weightKg: Double,
    val heightCm: Double,
    val activityLevel: String,
    val goal: String
)

object UserProfileManager {
    private const val PROFILES_PREFS_NAME = "FoodFlexUserProfiles"
    private const val SESSION_PREFS_NAME = "FoodFlexUserSession"
    private const val KEY_CURRENT_USER_EMAIL = "current_user_email"

    private fun getProfilesStorage(context: Context): SharedPreferences {
        return context.getSharedPreferences(PROFILES_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getSessionStorage(context: Context): SharedPreferences {
        return context.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveNewUser(context: Context, profile: UserProfile, password: String) {
        val editor = getProfilesStorage(context).edit()
        val email = profile.email
        editor.putString("${email}_name", profile.name)
        editor.putString("${email}_password", password) // DİKKAT: Güvenli değil!
        editor.putInt("${email}_age", profile.age)
        editor.putString("${email}_gender", profile.gender)
        editor.putFloat("${email}_weight", profile.weightKg.toFloat())
        editor.putFloat("${email}_height", profile.heightCm.toFloat())
        editor.putString("${email}_activity_level", profile.activityLevel)
        editor.putString("${email}_goal", profile.goal)
        editor.apply()
    }

    fun userExists(context: Context, email: String): Boolean {
        return getProfilesStorage(context).contains("${email}_password")
    }

    fun loadUserProfile(context: Context, email: String): UserProfile? {
        val prefs = getProfilesStorage(context)
        if (!userExists(context, email)) return null

        return UserProfile(
            name = prefs.getString("${email}_name", "") ?: "",
            email = email,
            age = prefs.getInt("${email}_age", 0),
            gender = prefs.getString("${email}_gender", "") ?: "",
            weightKg = prefs.getFloat("${email}_weight", 0f).toDouble(),
            heightCm = prefs.getFloat("${email}_height", 0f).toDouble(),
            activityLevel = prefs.getString("${email}_activity_level", "") ?: "",
            goal = prefs.getString("${email}_goal", "") ?: ""
        )
    }

    fun checkPassword(context: Context, email: String, passwordToCheck: String): Boolean {
        val savedPassword = getProfilesStorage(context).getString("${email}_password", null)
        return savedPassword != null && savedPassword == passwordToCheck
    }

    fun startUserSession(context: Context, email: String) {
        getSessionStorage(context).edit().putString(KEY_CURRENT_USER_EMAIL, email).apply()
    }

    fun getCurrentUserEmail(context: Context): String? {
        return getSessionStorage(context).getString(KEY_CURRENT_USER_EMAIL, null)
    }

    fun logout(context: Context) {
        getSessionStorage(context).edit().remove(KEY_CURRENT_USER_EMAIL).apply()
    }
}