package com.example.foodflex

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.foodflex.data.UserProfileManager

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                checkUserSession()
            }
        }, 1500)
    }

    private fun checkUserSession() {
        if (context == null) return

        // DÜZELTME: Artık isSetupComplete yerine mevcut bir oturum var mı diye kontrol ediyoruz.
        if (UserProfileManager.getCurrentUserEmail(requireContext()) != null) {
            // Aktif bir oturum var, Ana Sayfa'ya git
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        } else {
            // Aktif bir oturum yok, Giriş Ekranı'na git
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
}