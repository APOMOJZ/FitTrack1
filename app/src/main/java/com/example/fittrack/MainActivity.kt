package com.example.fittrack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fittrack.databinding.ActivityDashboardBinding
import com.example.fittrack.databinding.ItemMealCardBinding
import com.example.fittrack.model.AppDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: AppDatabase
    private var calorieGoal = 2000

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkGoogleFitPermissions()
        } else {
            updateStepsUI(0) 
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this, lifecycleScope)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        loadUserData()
        setupToolbar()
        setupBottomNavigation()
        observeDiaryData()

        checkRuntimePermissions()
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val weight = sharedPref.getFloat("user_weight", 70f)
        val height = sharedPref.getFloat("user_height", 170f)
        val age = sharedPref.getInt("user_age", 25)
        
        calorieGoal = ((10 * weight) + (6.25 * height) - (5 * age) + 5).toInt()
        if (calorieGoal < 1200) calorieGoal = 1200
    }

    private fun observeDiaryData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        lifecycleScope.launch {
            combine(
                db.diaryEntryDao().getTotalCaloriesForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getTotalCarbsForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getTotalProteinForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getTotalFatForDay(startOfDay, endOfDay)
            ) { cal, carb, prot, fat ->
                Triple(cal ?: 0, carb ?: 0, Pair(prot ?: 0, fat ?: 0))
            }.collect { data ->
                val calories = data.first
                val carbs = data.second
                val protein = data.third.first
                val fat = data.third.second
                updateCalorieUI(calories, carbs, protein, fat)
            }
        }
    }

    private fun updateCalorieUI(calories: Int, carbs: Int, protein: Int, fat: Int) {
        val remaining = calorieGoal - calories
        binding.tvRemainingCalories.text = String.format(Locale.getDefault(), "%,d", remaining)
        binding.calorieProgress.setProgress(calories.toFloat() / calorieGoal.toFloat())
        
        binding.tvGoalValue.text = String.format(Locale.getDefault(), "%,d", calorieGoal)
        binding.tvFoodValue.text = calories.toString()
        
        // Dynamic Macro Targets based on calorie goal
        val carbTarget = (calorieGoal * 0.5 / 4).toInt()
        val proteinTarget = (calorieGoal * 0.3 / 4).toInt()
        val fatTarget = (calorieGoal * 0.2 / 9).toInt()
        
        binding.tvCarbsText.text = String.format(Locale.getDefault(), "%dg / %dg", carbs, carbTarget)
        binding.tvProteinText.text = String.format(Locale.getDefault(), "%dg / %dg", protein, proteinTarget)
        binding.tvFatText.text = String.format(Locale.getDefault(), "%dg / %dg", fat, fatTarget)
        
        binding.carbsProgress.progress = if (carbTarget > 0) (carbs * 100 / carbTarget) else 0
        binding.proteinProgress.progress = if (proteinTarget > 0) (protein * 100 / proteinTarget) else 0
        binding.fatProgress.progress = if (fatTarget > 0) (fat * 100 / fatTarget) else 0

        setupMealCards(calories) // This is a simplification
    }

    private fun checkRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                checkGoogleFitPermissions()
            }
        } else {
            checkGoogleFitPermissions()
        }
    }

    private fun checkGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, FIT_PERMISSIONS_REQUEST_CODE, account, fitnessOptions)
        } else {
            readStepCount()
        }
    }

    private fun readStepCount() {
        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val steps = if (dataSet.isEmpty) 0 else {
                    dataSet.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()
                }
                updateStepsUI(steps)
            }
            .addOnFailureListener { updateStepsUI(0) }
    }

    private fun updateStepsUI(steps: Int) {
        binding.tvStepCount.text = String.format(Locale.getDefault(), "%,d", steps)
        val progressPercent = (steps.toFloat() / 10000 * 100).toInt().coerceIn(0, 100)
        binding.stepsProgress.progress = progressPercent
        binding.tvCaloriesBurned.text = String.format(Locale.getDefault(), "%d kcal burned", (steps * 0.04).toInt())
    }

    private fun setupToolbar() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "User")
        binding.tvGreeting.text = getString(R.string.good_morning, name)
        
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupMealCards(totalCalories: Int) {
        // In a real app, you'd filter by meal type. Showing placeholders for now.
        setupMeal(binding.cardBreakfast, "Breakfast", "Click + to add")
        setupMeal(binding.cardLunch, "Lunch", "Click + to add")
        setupMeal(binding.cardDinner, "Dinner", "Click + to add")
        setupMeal(binding.cardSnacks, "Snacks", "Click + to add")
    }

    private fun setupMeal(mealBinding: ItemMealCardBinding, name: String, calories: String) {
        mealBinding.tvMealName.text = name
        mealBinding.tvMealCalories.text = calories
        mealBinding.btnAddFood.setOnClickListener {
            startActivity(Intent(this, FoodSearchActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_diary -> {
                    startActivity(Intent(this, DiaryActivity::class.java))
                    false
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, FoodSearchActivity::class.java))
                    false
                }
                R.id.navigation_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    false
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FIT_PERMISSIONS_REQUEST_CODE && resultCode == RESULT_OK) {
            readStepCount()
        }
    }

    companion object {
        private const val FIT_PERMISSIONS_REQUEST_CODE = 1001
    }
}
