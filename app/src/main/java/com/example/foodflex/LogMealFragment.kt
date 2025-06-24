package com.example.foodflex

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.foodflex.data.MealLog
import com.example.foodflex.data.UserProfileManager
import com.example.foodflex.viewmodel.MealLogViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class LogMealFragment : Fragment() {

    private val viewModel: MealLogViewModel by activityViewModels()

    // Gemini API için
    private lateinit var generativeModel: GenerativeModel

    // Arayüz elemanları
    private lateinit var imageViewFood: ImageView
    private lateinit var buttonOpenCamera: Button
    private lateinit var buttonOpenGallery: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resultsScrollView: ScrollView
    private lateinit var textFoodName: TextView
    private lateinit var textBasePortion: TextView
    private lateinit var editTextQuantity: EditText
    private lateinit var spinnerUnit: Spinner
    private lateinit var buttonCalculate: Button
    private lateinit var buttonSave: Button
    private lateinit var editTextCalories: TextInputEditText
    private lateinit var editTextProtein: TextInputEditText
    private lateinit var editTextCarbs: TextInputEditText
    private lateinit var editTextFat: TextInputEditText

    // Diğer Değişkenler
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var currentRequestedPermissions: Array<String>? = null

    // Hesaplama için temel değerleri saklayacak değişkenler
    private var foodName: String = ""
    private var basePortionDescription: String = ""
    private var baseGrams: Double? = null
    private var baseCalories: Double = 0.0
    private var baseProtein: Double = 0.0
    private var baseCarbs: Double = 0.0
    private var baseFat: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_meal, container, false)
        bindViews(view)
        setupClickListeners()
        initializeGemini()
        setupResultLaunchers()
        setupPermissionLauncher()
        return view
    }

    private fun bindViews(view: View) {
        imageViewFood = view.findViewById(R.id.image_view_food)
        buttonOpenCamera = view.findViewById(R.id.button_camera)
        buttonOpenGallery = view.findViewById(R.id.button_gallery)
        progressBar = view.findViewById(R.id.progress_bar_gemini)
        resultsScrollView = view.findViewById(R.id.scroll_view_results)
        textFoodName = view.findViewById(R.id.text_food_name)
        textBasePortion = view.findViewById(R.id.text_base_portion)
        editTextQuantity = view.findViewById(R.id.edit_text_quantity)
        spinnerUnit = view.findViewById(R.id.spinner_unit)
        buttonCalculate = view.findViewById(R.id.button_calculate)
        buttonSave = view.findViewById(R.id.button_save)
        editTextCalories = view.findViewById(R.id.edit_text_calories)
        editTextProtein = view.findViewById(R.id.edit_text_protein)
        editTextCarbs = view.findViewById(R.id.edit_text_carbs)
        editTextFat = view.findViewById(R.id.edit_text_fat)
    }

    private fun setupClickListeners() {
        buttonOpenCamera.setOnClickListener {
            checkAndRequestPermissions(arrayOf(Manifest.permission.CAMERA)) { openCamera() }
        }
        buttonOpenGallery.setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            checkAndRequestPermissions(permissions) { openGallery() }
        }
        buttonCalculate.setOnClickListener { calculateNewPortion() }
        buttonSave.setOnClickListener { saveMealLog() }
    }

    private fun analyzeImageWithGemini(imageBitmap: Bitmap) {
        setLoading(true)
        resultsScrollView.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = """
                Bu fotoğraftaki yiyeceği analiz et. Cevabını aşağıdaki formatta, her bilgiyi ayrı bir satırda ve sadece istenen bilgileri vererek oluştur:
                foodName: [Yiyeceğin Türkçe adı]
                portionDescription: [Yaygın bir porsiyon açıklaması, örn: 1 adet orta boy]
                portionGrams: [Bu porsiyonun yaklaşık gram karşılığı, sadece sayı]
                calories: [Bu porsiyon için kalori değeri, sadece sayı]
                protein: [Bu porsiyon için protein (gram), sadece sayı]
                carbs: [Bu porsiyon için karbonhidrat (gram), sadece sayı]
                fat: [Bu porsiyon için yağ (gram), sadece sayı]
                Eğer bir değeri bulamazsan, o satıra "0" yaz.
                """.trimIndent()
                val inputContent = content { image(imageBitmap); text(prompt) }
                val response = generativeModel.generateContent(inputContent)
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    parseAndDisplayResponse(response.text)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseAndDisplayResponse(responseText: String?) {
        if (responseText == null) {
            Toast.makeText(requireContext(), "Geçersiz yanıt alındı.", Toast.LENGTH_SHORT).show()
            return
        }
        val responseMap = responseText.lines().mapNotNull { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
        foodName = responseMap["foodName"] ?: "Bilinmeyen Yiyecek"
        basePortionDescription = responseMap["portionDescription"] ?: "Bilinmeyen Porsiyon"
        baseGrams = responseMap["portionGrams"]?.toDoubleOrNull()
        baseCalories = responseMap["calories"]?.toDoubleOrNull() ?: 0.0
        baseProtein = responseMap["protein"]?.toDoubleOrNull() ?: 0.0
        baseCarbs = responseMap["carbs"]?.toDoubleOrNull() ?: 0.0
        baseFat = responseMap["fat"]?.toDoubleOrNull() ?: 0.0
        textFoodName.text = foodName
        textBasePortion.text = "Temel Porsiyon: $basePortionDescription"
        editTextCalories.setText(String.format(Locale.US, "%.1f", baseCalories))
        editTextProtein.setText(String.format(Locale.US, "%.1f", baseProtein))
        editTextCarbs.setText(String.format(Locale.US, "%.1f", baseCarbs))
        editTextFat.setText(String.format(Locale.US, "%.1f", baseFat))
        val units = mutableListOf<String>()
        units.add(basePortionDescription)
        if (baseGrams != null) units.add("gram")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = adapter
        editTextQuantity.setText("1")
        resultsScrollView.visibility = View.VISIBLE
    }

    private fun calculateNewPortion() {
        val quantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 1.0
        val selectedUnit = spinnerUnit.selectedItem.toString()
        var ratio = 1.0
        if (selectedUnit == "gram" && baseGrams != null && baseGrams!! > 0) {
            ratio = quantity / baseGrams!!
        } else if (selectedUnit == basePortionDescription) {
            ratio = quantity
        }
        editTextCalories.setText(String.format(Locale.US, "%.1f", baseCalories * ratio))
        editTextProtein.setText(String.format(Locale.US, "%.1f", baseProtein * ratio))
        editTextCarbs.setText(String.format(Locale.US, "%.1f", baseCarbs * ratio))
        editTextFat.setText(String.format(Locale.US, "%.1f", baseFat * ratio))
    }

    private fun saveMealLog() {
        val currentUserEmail = UserProfileManager.getCurrentUserEmail(requireContext())
        if (currentUserEmail == null) {
            Toast.makeText(requireContext(), "Giriş yapmış kullanıcı bulunamadı. Lütfen tekrar giriş yapın.", Toast.LENGTH_LONG).show()
            return
        }
        val finalCalories = editTextCalories.text.toString().toDoubleOrNull() ?: 0.0
        val finalProtein = editTextProtein.text.toString().toDoubleOrNull() ?: 0.0
        val finalCarbs = editTextCarbs.text.toString().toDoubleOrNull() ?: 0.0
        val finalFat = editTextFat.text.toString().toDoubleOrNull() ?: 0.0
        val userQuantity = editTextQuantity.text.toString().toDoubleOrNull() ?: 0.0
        val userUnit = spinnerUnit.selectedItem.toString()
        if (foodName.isBlank() || foodName == "Bilinmeyen Yiyecek") {
            Toast.makeText(requireContext(), "Lütfen geçerli bir yiyecek kaydedin.", Toast.LENGTH_SHORT).show()
            return
        }
        val mealLog = MealLog(
            userEmail = currentUserEmail,
            foodName = this.foodName,
            userPortionQuantity = userQuantity,
            userPortionUnit = userUnit,
            calculatedCalories = finalCalories,
            calculatedProtein = finalProtein,
            calculatedCarbs = finalCarbs,
            calculatedFat = finalFat,
            basePortionDescription = this.basePortionDescription,
            basePortionGrams = this.baseGrams
        )
        viewModel.insertMeal(mealLog)
        Toast.makeText(requireContext(), "$foodName günlüğe eklendi!", Toast.LENGTH_SHORT).show()
        resetUI()
    }

    private fun resetUI() {
        resultsScrollView.visibility = View.GONE
        imageViewFood.setImageResource(0) // Clear image
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonOpenCamera.isEnabled = !isLoading
        buttonOpenGallery.isEnabled = !isLoading
    }

    private fun initializeGemini() {
        generativeModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)
    }

    // --- İzin ve Galeri/Kamera Fonksiyonları ---
    private fun setupResultLaunchers() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    imageViewFood.setImageBitmap(it)
                    analyzeImageWithGemini(it)
                }
            }
        }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(requireActivity().contentResolver, it)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                    }
                    imageViewFood.setImageBitmap(imageBitmap)
                    analyzeImageWithGemini(imageBitmap)
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Fotoğraf yüklenemedi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                currentRequestedPermissions?.let { perms ->
                    if (perms.contains(Manifest.permission.CAMERA)) openCamera()
                    else if (perms.contains(Manifest.permission.READ_MEDIA_IMAGES) || perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) openGallery()
                }
            } else {
                if (!shouldShowRequestPermissionRationale(permissions.keys.first())) {
                    showSettingsDialog()
                } else {
                    Toast.makeText(requireContext(), "İzinler gereklidir.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkAndRequestPermissions(permissions: Array<String>, onGranted: () -> Unit) {
        currentRequestedPermissions = permissions
        val permissionsToRequest = permissions.filter { ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()
        if (permissionsToRequest.isEmpty()) onGranted() else requestPermissionLauncher.launch(permissionsToRequest)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try { cameraLauncher.launch(cameraIntent) } catch (e: ActivityNotFoundException) { Toast.makeText(requireContext(), "Kamera uygulaması bulunamadı.", Toast.LENGTH_LONG).show() }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("İzin Gerekli")
            .setMessage("Bu özelliğin çalışması için gerekli izinleri vermeniz gerekiyor. Ayarlardan izinleri etkinleştirebilirsiniz.")
            .setPositiveButton("Ayarlara Git") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}