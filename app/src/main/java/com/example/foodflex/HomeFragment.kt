package com.example.foodflex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_go_to_log_meal).setOnClickListener {
            // "Besin Gir" butonuna basıldığında, alttaki menüde ilgili sekmeyi seçili hale getir
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            bottomNav?.selectedItemId = R.id.logMealFragment
        }

        view.findViewById<Button>(R.id.button_go_to_diary).setOnClickListener {
            // "Günlüğüme Git" butonuna basıldığında, alttaki menüde ilgili sekmeyi seçili hale getir
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            bottomNav?.selectedItemId = R.id.diaryFragment
        }

        view.findViewById<Button>(R.id.button_go_to_reports).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_reportsFragment)
        }
    }
}