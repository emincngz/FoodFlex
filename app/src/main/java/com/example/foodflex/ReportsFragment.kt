package com.example.foodflex

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.example.foodflex.viewmodel.MealLogViewModel
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsFragment : Fragment() {

    // ViewModel'i Activity scope'u ile alıyoruz, böylece diğer fragment'larla aynı örneği kullanır.
    private val viewModel: MealLogViewModel by activityViewModels()

    private lateinit var buttonSelectDate: Button
    private lateinit var textSelectedDate: TextView
    private lateinit var cardDailySummary: MaterialCardView
    private lateinit var textTotalCalories: TextView
    private lateinit var textTotalProtein: TextView
    private lateinit var textTotalCarbs: TextView
    private lateinit var textTotalFat: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Arayüz elemanlarını bağla
        buttonSelectDate = view.findViewById(R.id.button_select_date)
        textSelectedDate = view.findViewById(R.id.text_selected_date)
        cardDailySummary = view.findViewById(R.id.card_daily_summary)
        textTotalCalories = view.findViewById(R.id.text_total_calories)
        textTotalProtein = view.findViewById(R.id.text_total_protein)
        textTotalCarbs = view.findViewById(R.id.text_total_carbs)
        textTotalFat = view.findViewById(R.id.text_total_fat)

        // Başlangıçta özet kartını gizle
        cardDailySummary.visibility = View.GONE
        textSelectedDate.text = "Lütfen bir tarih seçin"

        // Tarih seçme butonuna tıklama olayını ayarla
        buttonSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        // ViewModel'deki singleDayTotals LiveData'sını gözlemle
        viewModel.singleDayTotals.observe(viewLifecycleOwner) { totals ->
            // Sonuçları ekrana yazdır
            textTotalCalories.text = String.format(Locale.US, "Kalori: %.1f kcal", totals.totalCalories)
            textTotalProtein.text = String.format(Locale.US, "Protein: %.1f g", totals.totalProtein)
            textTotalCarbs.text = String.format(Locale.US, "Karbonhidrat: %.1f g", totals.totalCarbs)
            textTotalFat.text = String.format(Locale.US, "Yağ: %.1f g", totals.totalFat)

            // Veri geldiyse kartı görünür yap
            cardDailySummary.visibility = View.VISIBLE
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Seçilen tarihi işle
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                // Seçilen tarihi ekranda göster
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
                textSelectedDate.text = dateFormat.format(selectedCalendar.time)

                // ViewModel'den o günün verilerini yüklemesini iste
                viewModel.loadTotalsForDay(selectedCalendar.timeInMillis)
            },
            year,
            month,
            day
        )
        // Gelecekteki tarihlerin seçilmesini engelle
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}