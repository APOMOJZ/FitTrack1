package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.example.fittrack.databinding.ActivityDashboardBinding
import com.example.fittrack.databinding.ItemMealCardBinding
import com.example.fittrack.model.AppDatabase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: AppDatabase
    private var calorieGoal = 2000
    private var proteinGoal = 150
    private var currentExerciseCalories = 0
    private var currentFoodCalories = 0

    private val healthConnectPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private val requestHealthConnectPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(healthConnectPermissions)) {
            fetchHealthConnectSteps()
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
        
        checkHealthConnect()
    }

    private fun checkHealthConnect() {
        if (HealthConnectClient.getSdkStatus(this) == HealthConnectClient.SDK_AVAILABLE) {
            val client = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                val granted = client.permissionController.getGrantedPermissions()
                if (granted.containsAll(healthConnectPermissions)) {
                    fetchHealthConnectSteps()
                } else {
                    requestHealthConnectPermissions.launch(healthConnectPermissions)
                }
            }
        } else {
            updateStepsUI(0)
        }
    }

    private fun fetchHealthConnectSteps() {
        val client = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            try {
                val startTime = ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).toInstant()
                val endTime = Instant.now()
                val response = client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                val stepCount = response[StepsRecord.COUNT_TOTAL] ?: 0L
                updateStepsUI(stepCount.toInt())
            } catch (e: Exception) {
                updateStepsUI(0)
            }
        }
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        calorieGoal = sharedPref.getInt("calorie_goal", 2000)
        proteinGoal = sharedPref.getInt("protein_goal", 150)
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
                currentFoodCalories = data.first
                val carbs = data.second
                val protein = data.third.first
                val fat = data.third.second
                updateCalorieUI(currentFoodCalories, carbs, protein, fat)
            }
        }
    }

    private fun updateCalorieUI(calories: Int, carbs: Int, protein: Int, fat: Int) {
        val remaining = (calorieGoal + currentExerciseCalories) - calories
        binding.tvRemainingCalories.text = String.format(Locale.getDefault(), "%,d", remaining)
        
        val totalBudget = calorieGoal + currentExerciseCalories
        binding.calorieProgress.setProgress(calories.toFloat() / totalBudget.toFloat())
        
        binding.tvGoalValue.text = String.format(Locale.getDefault(), "%,d", calorieGoal)
        binding.tvFoodValue.text = calories.toString()
        binding.tvExerciseValue.text = currentExerciseCalories.toString()
        
        // Distribution based on user's goal
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val goal = sharedPref.getString("user_goal", "Maintain Weight")
        
        val proteinTarget = proteinGoal
        val fatTarget = (calorieGoal * 0.25 / 9).toInt()
        val carbTarget = ((calorieGoal - (proteinTarget * 4) - (fatTarget * 9)) / 4).toInt()
        
        binding.tvCarbsText.text = String.format(Locale.getDefault(), "%dg / %dg", carbs, carbTarget)
        binding.tvProteinText.text = String.format(Locale.getDefault(), "%dg / %dg", protein, proteinTarget)
        binding.tvFatText.text = String.format(Locale.getDefault(), "%dg / %dg", fat, fatTarget)
        
        binding.carbsProgress.progress = if (carbTarget > 0) (carbs * 100 / carbTarget) else 0
        binding.proteinProgress.progress = if (proteinTarget > 0) (protein * 100 / proteinTarget) else 0
        binding.fatProgress.progress = if (fatTarget > 0) (fat * 100 / fatTarget) else 0

        setupMealCards()
    }

    private fun updateStepsUI(steps: Int) {
        binding.tvStepCount.text = String.format(Locale.getDefault(), "%,d", steps)
        val progressPercent = (steps.toFloat() / 10000 * 100).toInt().coerceIn(0, 100)
        binding.stepsProgress.progress = progressPercent
        
        currentExerciseCalories = (steps * 0.04).toInt()
        binding.tvCaloriesBurned.text = getString(R.string.calories_burned, currentExerciseCalories)
        
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis
            
            val carbs = db.diaryEntryDao().getTotalCarbsForDayOnce(startOfDay, endOfDay) ?: 0
            val protein = db.diaryEntryDao().getTotalProteinForDayOnce(startOfDay, endOfDay) ?: 0
            val fat = db.diaryEntryDao().getTotalFatForDayOnce(startOfDay, endOfDay) ?: 0
            
            updateCalorieUI(currentFoodCalories, carbs, protein, fat)
        }
    }

    private fun setupToolbar() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "User")
        binding.tvGreeting.text = getString(R.string.good_morning, name)
        
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupMealCards() {
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
}
