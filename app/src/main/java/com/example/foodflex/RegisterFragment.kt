package com.example.foodflex

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val buttonNext = view.findViewById<Button>(R.id.button_register_next)
        val editTextName = view.findViewById<TextInputEditText>(R.id.edit_text_register_name)
        val editTextEmail = view.findViewById<TextInputEditText>(R.id.edit_text_register_email)
        val editTextPassword = view.findViewById<TextInputEditText>(R.id.edit_text_register_password)

        // YENİ: textview'i koda bağla
        val textGoToLogin = view.findViewById<TextView>(R.id.text_go_to_login)

        buttonNext.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(context, "Şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = bundleOf(
                "name" to name,
                "email" to email,
                "password" to password
            )

            findNavController().navigate(R.id.action_registerFragment_to_initialProfileSetupFragment, bundle)
        }

        // YENİ: textview'e tıklama olayı ekle
        textGoToLogin.setOnClickListener {
            // Bir önceki ekrana dönmek için en basit ve doğru yöntem:
            findNavController().popBackStack()
        }

        return view
    }
}