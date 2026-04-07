package com.example.fittrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fittrack.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding is already completed
        val sharedPref = getSharedPreferences("FitTrackPrefs", Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("onboarding_completed", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetStarted.setOnClickListener {
            goToNextScreen()
        }
    }

    private fun goToNextScreen() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }
}
