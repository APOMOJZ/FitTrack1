package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrack.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val weight = binding.etWeight.text.toString().trim()
        val height = binding.etHeight.text.toString().trim()

        if (name.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_name", name)
            putInt("user_age", age.toInt())
            putFloat("user_weight", weight.toFloat())
            putFloat("user_height", height.toFloat())
            putBoolean("onboarding_completed", true)
            apply()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
