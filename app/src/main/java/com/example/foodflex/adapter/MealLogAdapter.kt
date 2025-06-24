package com.example.foodflex.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodflex.R
import com.example.foodflex.data.MealLog
import java.util.Locale

class MealLogAdapter : ListAdapter<MealLog, MealLogAdapter.MealViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_log, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = getItem(position)
        holder.bind(meal)
    }

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.text_food_name)
        private val portionTextView: TextView = itemView.findViewById(R.id.text_portion)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.text_calories)
        private val proteinTextView: TextView = itemView.findViewById(R.id.text_item_protein)
        private val carbsTextView: TextView = itemView.findViewById(R.id.text_item_carbs)
        private val fatTextView: TextView = itemView.findViewById(R.id.text_item_fat)

        fun bind(meal: MealLog) {
            foodNameTextView.text = meal.foodName
            portionTextView.text = String.format(Locale.US, "%.1f %s", meal.userPortionQuantity, meal.userPortionUnit)
            caloriesTextView.text = String.format(Locale.US, "%.0f kcal", meal.calculatedCalories)

            // METİN FORMATINI GÜNCELLE
            proteinTextView.text = String.format(Locale.US, "P: %.1f g", meal.calculatedProtein)
            carbsTextView.text = String.format(Locale.US, "K: %.1f g", meal.calculatedCarbs)
            fatTextView.text = String.format(Locale.US, "Y: %.1f g", meal.calculatedFat)
        }
    }
}

class MealDiffCallback : DiffUtil.ItemCallback<MealLog>() {
    override fun areItemsTheSame(oldItem: MealLog, newItem: MealLog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MealLog, newItem: MealLog): Boolean {
        return oldItem == newItem
    }
}