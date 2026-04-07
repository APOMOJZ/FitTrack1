package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrack.databinding.ActivityDashboardBinding
import com.example.fittrack.databinding.ItemMealCardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCalorieCard()
        setupMealCards()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "User")
        binding.tvGreeting.text = getString(R.string.good_morning, name)
    }

    private fun setupCalorieCard() {
        val goal = 2100
        val food = 860
        val remaining = goal - food
        
        binding.tvRemainingCalories.text = String.format("%,d", remaining)
        binding.calorieProgress.setProgress(food.toFloat() / goal.toFloat())
    }

    private fun setupMealCards() {
        setupMeal(binding.cardBreakfast, "Breakfast", "420 kcal")
        setupMeal(binding.cardLunch, "Lunch", "440 kcal")
        setupMeal(binding.cardDinner, "Dinner", "0 kcal")
        setupMeal(binding.cardSnacks, "Snacks", "0 kcal")
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
