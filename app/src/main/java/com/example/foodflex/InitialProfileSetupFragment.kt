package com.example.foodflex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodflex.data.UserProfile
import com.example.foodflex.data.UserProfileManager
import com.example.foodflex.viewmodel.MealLogViewModel
import com.google.android.material.textfield.TextInputEditText

class InitialProfileSetupFragment : Fragment() {

    private val viewModel: MealLogViewModel by activityViewModels()
    private lateinit var nameFromArgs: String
    private lateinit var emailFromArgs: String
    private lateinit var passwordFromArgs: String

    private lateinit var editTextAge: TextInputEditText
    private lateinit var editTextWeight: TextInputEditText
    private lateinit var editTextHeight: TextInputEditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var spinnerGoal: Spinner
    private lateinit var buttonFinish: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nameFromArgs = it.getString("name", "")
            emailFromArgs = it.getString("email", "")
            passwordFromArgs = it.getString("password", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_initial_profile_setup, container, false)
        bindViews(view)
        setupSpinners()
        setupClickListener()
        return view
    }

    private fun bindViews(view: View) {
        editTextAge = view.findViewById(R.id.edit_text_age)
        editTextWeight = view.findViewById(R.id.edit_text_weight)
        editTextHeight = view.findViewById(R.id.edit_text_height)
        spinnerGender = view.findViewById(R.id.spinner_gender)
        spinnerActivityLevel = view.findViewById(R.id.spinner_activity_level)
        spinnerGoal = view.findViewById(R.id.spinner_goal)
        buttonFinish = view.findViewById(R.id.button_finish_setup)
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

    private fun setupClickListener() {
        buttonFinish.setOnClickListener { finishSetup() }
    }

    private fun finishSetup() {
        val age = editTextAge.text.toString().toIntOrNull()
        val weight = editTextWeight.text.toString().toDoubleOrNull()
        val height = editTextHeight.text.toString().toDoubleOrNull()

        if (age == null || weight == null || height == null) {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        if (UserProfileManager.userExists(requireContext(), emailFromArgs)) {
            Toast.makeText(requireContext(), "Bu e-posta adresi zaten kayıtlı.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val finalProfile = UserProfile(
            name = nameFromArgs, email = emailFromArgs, age = age, weightKg = weight, heightCm = height,
            gender = spinnerGender.selectedItem.toString(),
            activityLevel = spinnerActivityLevel.selectedItem.toString(),
            goal = spinnerGoal.selectedItem.toString()
        )

        UserProfileManager.saveNewUser(requireContext(), finalProfile, passwordFromArgs)
        UserProfileManager.startUserSession(requireContext(), emailFromArgs)
        viewModel.loadDataForUser(emailFromArgs)

        Toast.makeText(requireContext(), "Hesabın başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_initialProfileSetupFragment_to_homeFragment)
    }
}