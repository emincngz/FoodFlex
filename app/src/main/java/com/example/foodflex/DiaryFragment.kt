package com.example.foodflex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodflex.adapter.MealLogAdapter
import com.example.foodflex.data.DailyGoal
import com.example.foodflex.data.MealLog
import com.example.foodflex.viewmodel.MealLogViewModel
import java.util.Calendar
import java.util.Locale

class DiaryFragment : Fragment() {

    private val viewModel: MealLogViewModel by activityViewModels()
    private lateinit var mealLogAdapter: MealLogAdapter

    // Arayüz elemanları
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var textSummaryCalories: TextView
    private lateinit var progressCalories: ProgressBar
    private lateinit var textSummaryProtein: TextView
    private lateinit var progressProtein: ProgressBar
    private lateinit var textSummaryCarbs: TextView
    private lateinit var progressCarbs: ProgressBar
    private lateinit var textSummaryFat: TextView
    private lateinit var progressFat: ProgressBar
    private lateinit var textGoalCalories: TextView
    private lateinit var textGoalProtein: TextView
    private lateinit var textGoalCarbs: TextView
    private lateinit var textGoalFat: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupRecyclerView()
        observeViewModel()
    }

    private fun bindViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_diary)
        emptyStateLayout = view.findViewById(R.id.layout_empty_state)
        textSummaryCalories = view.findViewById(R.id.text_summary_calories)
        progressCalories = view.findViewById(R.id.progress_calories)
        textSummaryProtein = view.findViewById(R.id.text_summary_protein)
        progressProtein = view.findViewById(R.id.progress_protein)
        textSummaryCarbs = view.findViewById(R.id.text_summary_carbs)
        progressCarbs = view.findViewById(R.id.progress_carbs)
        textSummaryFat = view.findViewById(R.id.text_summary_fat)
        progressFat = view.findViewById(R.id.progress_fat)
        textGoalCalories = view.findViewById(R.id.text_goal_calories)
        textGoalProtein = view.findViewById(R.id.text_goal_protein)
        textGoalCarbs = view.findViewById(R.id.text_goal_carbs)
        textGoalFat = view.findViewById(R.id.text_goal_fat)
    }

    private fun setupRecyclerView() {
        mealLogAdapter = MealLogAdapter()
        recyclerView.adapter = mealLogAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val mealLog = mealLogAdapter.currentList[position]
                // DÜZELTME: Fonksiyon adı deleteMeal olarak değiştirildi.
                viewModel.deleteMeal(mealLog)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun observeViewModel() {
        viewModel.dailyGoal.observe(viewLifecycleOwner) { goal ->
            if (goal != null) {
                updateGoalTexts(goal)
                // Hedefler değiştiğinde özet kartını da anlık olarak güncelle
                updateSummaryCard(viewModel.mealsForCurrentUser.value ?: emptyList(), goal)
            }
        }

        // DÜZELTME: Artık allMeals yerine mealsForCurrentUser gözlemleniyor.
        viewModel.mealsForCurrentUser.observe(viewLifecycleOwner) { meals ->
            updateSummaryCard(meals, viewModel.dailyGoal.value)
        }
    }

    private fun updateSummaryCard(meals: List<MealLog>, dailyGoal: DailyGoal?) {
        // ViewModel zaten doğru kullanıcının verilerini getirdiği için filtrelemeye gerek yok,
        // ama sadece o günün toplamını göstermek için tarih filtresi kalmalı.
        val todayMeals = meals.filter {
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        if (todayMeals.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }

        mealLogAdapter.submitList(todayMeals)

        val totalCalories = todayMeals.sumOf { it.calculatedCalories }
        val totalProtein = todayMeals.sumOf { it.calculatedProtein }
        val totalCarbs = todayMeals.sumOf { it.calculatedCarbs }
        val totalFat = todayMeals.sumOf { it.calculatedFat }

        val goal = dailyGoal ?: DailyGoal(0.0, 0.0, 0.0, 0.0)

        updateMacroView(textSummaryCalories, progressCalories, totalCalories, goal.calories, "kcal")
        updateMacroView(textSummaryProtein, progressProtein, totalProtein, goal.protein, "g")
        updateMacroView(textSummaryCarbs, progressCarbs, totalCarbs, goal.carbs, "g")
        updateMacroView(textSummaryFat, progressFat, totalFat, goal.fat, "g")
    }

    private fun updateGoalTexts(goal: DailyGoal) {
        textGoalCalories.text = String.format(Locale.US, "Kalori: %.0f kcal", goal.calories)
        textGoalProtein.text = String.format(Locale.US, "Protein: %.0f g", goal.protein)
        textGoalCarbs.text = String.format(Locale.US, "Karbonhidrat: %.0f g", goal.carbs)
        textGoalFat.text = String.format(Locale.US, "Yağ: %.0f g", goal.fat)
    }

    private fun updateMacroView(textView: TextView, progressBar: ProgressBar, consumed: Double, goal: Double, unit: String) {
        val label = when(textView.id) {
            R.id.text_summary_calories -> "Kalori"
            R.id.text_summary_protein -> "Protein"
            R.id.text_summary_carbs -> "Karbonhidrat"
            R.id.text_summary_fat -> "Yağ"
            else -> ""
        }
        textView.text = String.format(Locale.US, "%s: %.0f / %.0f %s", label, consumed, goal, unit)
        val progress = if (goal > 0) (consumed / goal * 100).toInt() else 0
        progressBar.progress = progress
    }
}