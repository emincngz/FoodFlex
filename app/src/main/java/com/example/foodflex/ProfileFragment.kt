package com.example.foodflex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodflex.data.UserProfile
import com.example.foodflex.data.UserProfileManager
import com.example.foodflex.viewmodel.MealLogViewModel
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    private val viewModel: MealLogViewModel by activityViewModels()

    // Arayüz Elemanları
    private lateinit var textGreeting: TextView
    private lateinit var editTextAge: TextInputEditText
    private lateinit var editTextWeight: TextInputEditText
    private lateinit var editTextHeight: TextInputEditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var spinnerGoal: Spinner
    private lateinit var buttonUpdate: Button
    private lateinit var buttonLogout: Button

    private var currentUserProfile: UserProfile? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        bindViews(view)
        setupSpinners()
        setupClickListeners()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun bindViews(view: View) {
        textGreeting = view.findViewById(R.id.text_profile_greeting)
        editTextAge = view.findViewById(R.id.edit_text_age)
        editTextWeight = view.findViewById(R.id.edit_text_weight)
        editTextHeight = view.findViewById(R.id.edit_text_height)
        spinnerGender = view.findViewById(R.id.spinner_gender)
        spinnerActivityLevel = view.findViewById(R.id.spinner_activity_level)
        spinnerGoal = view.findViewById(R.id.spinner_goal)
        buttonUpdate = view.findViewById(R.id.button_update_profile)
        buttonLogout = view.findViewById(R.id.button_logout)
    }

    private fun loadProfileData() {
        UserProfileManager.getCurrentUserEmail(requireContext())?.let { email ->
            currentUserProfile = UserProfileManager.loadUserProfile(requireContext(), email)
            currentUserProfile?.let { profile ->
                textGreeting.text = "Merhaba, ${profile.name}!"
                editTextAge.setText(profile.age.toString())
                editTextWeight.setText(profile.weightKg.toString())
                editTextHeight.setText(profile.heightCm.toString())
                (spinnerGender.adapter as? ArrayAdapter<String>)?.let { spinnerGender.setSelection(it.getPosition(profile.gender)) }
                (spinnerActivityLevel.adapter as? ArrayAdapter<String>)?.let { spinnerActivityLevel.setSelection(it.getPosition(profile.activityLevel)) }
                (spinnerGoal.adapter as? ArrayAdapter<String>)?.let { spinnerGoal.setSelection(it.getPosition(profile.goal)) }
            }
        }
    }

    private fun setupClickListeners() {
        buttonUpdate.setOnClickListener { updateProfile() }
        buttonLogout.setOnClickListener { showLogoutConfirmationDialog() }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Çıkış Yap")
            .setMessage("Çıkış yapmak istediğinize emin misiniz? Öğün verileriniz hesabınızda saklanmaya devam edecektir.")
            .setPositiveButton("Evet, Çıkış Yap") { _, _ -> logoutUser() }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun logoutUser() {
        // *** HATA BURADAYDI, BU SATIR SİLİNDİ: viewModel.deleteAllMealsForCurrentUser() ***
        // Artık veritabanını silmiyoruz.

        // 1. Sadece aktif oturumu kapat
        UserProfileManager.logout(requireContext())
        // 2. ViewModel'deki aktif kullanıcı verisini temizle
        viewModel.clearDataOnLogout()
        // 3. Kullanıcıyı en başa, giriş akışına yönlendir
        findNavController().navigate(R.id.action_global_splashFragment)
    }

    private fun updateProfile() {
        val email = currentUserProfile?.email ?: return
        val name = currentUserProfile?.name ?: return
        val age = editTextAge.text.toString().toIntOrNull()
        val weight = editTextWeight.text.toString().toDoubleOrNull()
        val height = editTextHeight.text.toString().toDoubleOrNull()

        if (age == null || weight == null || height == null) {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedProfile = UserProfile(
            name = name, email = email, age = age, weightKg = weight, heightCm = height,
            gender = spinnerGender.selectedItem.toString(),
            activityLevel = spinnerActivityLevel.selectedItem.toString(),
            goal = spinnerGoal.selectedItem.toString()
        )

        // Bu fonksiyon şifre alanını boş geçerek mevcut şifreyi korur ve diğer bilgileri günceller.
        UserProfileManager.saveNewUser(requireContext(), updatedProfile, "")

        viewModel.loadDataForUser(email)
        Toast.makeText(requireContext(), "Profil güncellendi!", Toast.LENGTH_SHORT).show()
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(requireContext(), R.array.gender_array, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGender.adapter = adapter
        }
        ArrayAdapter.createFromResource(requireContext(), R.array.activity_level_array, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerActivityLevel.adapter = adapter
        }
        ArrayAdapter.createFromResource(requireContext(), R.array.goal_array, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGoal.adapter = adapter
        }
    }
}