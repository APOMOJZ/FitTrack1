package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrack.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        displayUserData()
        setupBottomNavigation()
        
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun displayUserData() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "User")
        val age = sharedPref.getInt("user_age", 0)
        val weight = sharedPref.getFloat("user_weight", 0f)
        val height = sharedPref.getFloat("user_height", 0f)

        binding.tvUserName.text = name
        binding.tvUserAge.text = age.toString()
        binding.tvUserWeight.text = String.format("%.1f kg", weight)
        binding.tvUserHeight.text = String.format("%.1f cm", height)
        
        // Calculate calorie goal for display here too
        val calorieGoal = ((10 * weight) + (6.25 * height) - (5 * age) + 5).toInt()
        binding.tvCalorieGoal.text = String.format("%,d kcal", if (calorieGoal > 1200) calorieGoal else 1200)
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_profile
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
                R.id.navigation_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }
}
