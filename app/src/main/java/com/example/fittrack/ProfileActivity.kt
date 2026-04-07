package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrack.databinding.ActivityProfileBinding
import java.util.Locale

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
        val calorieGoal = sharedPref.getInt("calorie_goal", 2000)
        val proteinGoal = sharedPref.getInt("protein_goal", 150)
        val goalType = sharedPref.getString("user_goal", "Maintain Weight")

        binding.tvUserName.text = name
        binding.tvUserAge.text = age.toString()
        binding.tvUserWeight.text = String.format(Locale.getDefault(), "%.1f kg", weight)
        binding.tvUserHeight.text = String.format(Locale.getDefault(), "%.1f cm", height)
        
        binding.tvCalorieGoal.text = String.format(Locale.getDefault(), "%,d kcal", calorieGoal)
        binding.tvProteinGoal.text = String.format(Locale.getDefault(), "%dg", proteinGoal)
        binding.tvGoalType.text = goalType
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
