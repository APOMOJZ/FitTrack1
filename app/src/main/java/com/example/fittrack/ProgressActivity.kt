package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fittrack.databinding.ActivityProgressBinding
import com.example.fittrack.model.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        db = AppDatabase.getDatabase(this, lifecycleScope)
        
        setupBottomNavigation()
        loadProgressData()
        
        binding.progressTabs.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                loadProgressData()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadProgressData() {
        val days = when (binding.progressTabs.selectedTabPosition) {
            0 -> 7
            1 -> 30
            else -> 90
        }

        lifecycleScope.launch {
            val dailyCalories = mutableListOf<Float>()
            val labels = mutableListOf<String>()
            val dateFormat = SimpleDateFormat("EE", Locale.getDefault())
            val dateMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            
            var totalCal = 0f
            var daysWithData = 0
            val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
            val goal = sharedPref.getInt("calorie_goal", 2000)
            var daysMetGoal = 0

            for (i in (days - 1) downTo 0) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis
                
                val dailyTotal = db.diaryEntryDao().getTotalCaloriesForDayOnce(start, end) ?: 0
                dailyCalories.add(dailyTotal.toFloat())
                
                if (days <= 7) {
                    labels.add(dateFormat.format(calendar.time))
                } else if (i % (days / 5) == 0) {
                    labels.add(dateMonthFormat.format(calendar.time))
                } else {
                    labels.add("")
                }

                if (dailyTotal > 0) {
                    totalCal += dailyTotal
                    daysWithData++
                    if (dailyTotal in (goal - 200)..(goal + 200)) {
                        daysMetGoal++
                    }
                }
            }
            
            binding.calorieChart.setData(dailyCalories, labels)
            
            val avg = if (daysWithData > 0) (totalCal / daysWithData).toInt() else 0
            binding.tvAvgCalories.text = String.format(Locale.getDefault(), "%d kcal", avg)
            
            val adherence = if (daysWithData > 0) (daysMetGoal * 100 / daysWithData) else 0
            binding.tvGoalAdherence.text = String.format(Locale.getDefault(), "%d%%", adherence)
            
            // Simplified streak calculation (consecutive days with data)
            var streak = 0
            for (i in dailyCalories.indices.reversed()) {
                if (dailyCalories[i] > 0) streak++ else if (i < dailyCalories.size - 1) break
            }
            binding.tvStreak.text = String.format(Locale.getDefault(), "%d Days", streak)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_progress
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_diary -> {
                    startActivity(Intent(this, DiaryActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, FoodSearchActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_progress -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    false
                }
                else -> false
            }
        }
    }
}
