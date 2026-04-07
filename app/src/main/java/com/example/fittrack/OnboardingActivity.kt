package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrack.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()

        binding.btnContinue.setOnClickListener {
            saveUserData()
        }
    }

    private fun setupSpinners() {
        val goals = arrayOf("Weight Loss", "Maintain Weight", "Muscle Gain")
        val activityLevels = arrayOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active")

        val goalAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, goals)
        binding.spinnerGoal.setAdapter(goalAdapter)
        binding.spinnerGoal.setText(goals[1], false)

        val activityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityLevels)
        binding.spinnerActivity.setAdapter(activityAdapter)
        binding.spinnerActivity.setText(activityLevels[0], false)
    }

    private fun saveUserData() {
        val name = binding.etName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()
        val goal = binding.spinnerGoal.text.toString()
        val activityLevel = binding.spinnerActivity.text.toString()

        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toInt()
        val weight = weightStr.toFloat()
        val height = heightStr.toFloat()

        // BMR (Mifflin-St Jeor Equation)
        val bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5

        // Activity Multiplier
        val multiplier = when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            else -> 1.2
        }

        var tdee = bmr * multiplier

        // Goal Adjustment
        when (goal) {
            "Weight Loss" -> tdee -= 500
            "Muscle Gain" -> tdee += 300
        }

        val calorieGoal = tdee.toInt().coerceAtLeast(1200)
        
        // Protein Goal: 1.6g per kg for Muscle Gain, 1.2g for Weight Loss, 1.0g for Maintain
        val proteinFactor = when (goal) {
            "Muscle Gain" -> 1.8f
            "Weight Loss" -> 1.4f
            else -> 1.2f
        }
        val proteinGoal = (weight * proteinFactor).toInt()

        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_name", name)
            putInt("user_age", age)
            putFloat("user_weight", weight)
            putFloat("user_height", height)
            putString("user_goal", goal)
            putString("user_activity", activityLevel)
            putInt("calorie_goal", calorieGoal)
            putInt("protein_goal", proteinGoal)
            putBoolean("onboarding_completed", true)
            apply()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
