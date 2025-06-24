package com.example.foodflex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodflex.data.UserProfileManager
import com.example.foodflex.viewmodel.MealLogViewModel
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {

    private val viewModel: MealLogViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val editTextEmail = view.findViewById<TextInputEditText>(R.id.edit_text_login_email)
        val editTextPassword = view.findViewById<TextInputEditText>(R.id.edit_text_login_password)
        val buttonLogin = view.findViewById<Button>(R.id.button_login)
        val textGoToRegister = view.findViewById<TextView>(R.id.text_go_to_register)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (UserProfileManager.checkPassword(requireContext(), email, password)) {
                UserProfileManager.startUserSession(requireContext(), email)
                viewModel.loadDataForUser(email)
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(context, "E-posta veya şifre hatalı.", Toast.LENGTH_SHORT).show()
            }
        }
        textGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        return view
    }
}