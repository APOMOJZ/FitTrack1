package com.example.fittrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fittrack.adapter.DiaryAdapter
import com.example.fittrack.databinding.ActivityDiaryBinding
import com.example.fittrack.model.AppDatabase
import com.example.fittrack.model.DiaryEntryEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiaryBinding
    private lateinit var db: AppDatabase
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        db = AppDatabase.getDatabase(this, lifecycleScope)
        
        setupRecyclerView()
        updateDateDisplay()
        setupBottomNavigation()
        setupListeners()
        observeDiaryData()
    }

    private fun setupRecyclerView() {
        binding.rvDiary.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.btnPrevDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            updateDateDisplay()
            observeDiaryData()
        }
        binding.btnNextDay.setOnClickListener {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            updateDateDisplay()
            observeDiaryData()
        }
        binding.fabAddFood.setOnClickListener {
            startActivity(Intent(this, FoodSearchActivity::class.java))
        }
        binding.swipeRefresh.setOnRefreshListener {
            observeDiaryData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val today = Calendar.getInstance()
        
        val dateText = when {
            isSameDay(calendar, today) -> "Today, " + SimpleDateFormat("MMM d", Locale.getDefault()).format(calendar.time)
            else -> sdf.format(calendar.time)
        }
        binding.tvDate.text = dateText
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun observeDiaryData() {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfDay = cal.timeInMillis
        
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        lifecycleScope.launch {
            combine(
                db.diaryEntryDao().getTotalCaloriesForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getTotalCarbsForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getTotalProteinForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getTotalFatForDay(startOfDay, endOfDay),
                db.diaryEntryDao().getEntriesForDay(startOfDay, endOfDay)
            ) { cal, carb, prot, fat, entries ->
                DataWrapper(cal ?: 0, carb ?: 0, prot ?: 0, fat ?: 0, entries)
            }.collect { data ->
                binding.tvTotalCalories.text = String.format(Locale.getDefault(), "%d", data.calories)
                binding.tvTotalCarbs.text = String.format(Locale.getDefault(), "%dg", data.carbs)
                binding.tvTotalProtein.text = String.format(Locale.getDefault(), "%dg", data.protein)
                binding.tvTotalFat.text = String.format(Locale.getDefault(), "%dg", data.fat)
                
                if (data.entries.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvDiary.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvDiary.visibility = View.VISIBLE
                    binding.rvDiary.adapter = DiaryAdapter(data.entries)
                }
            }
        }
    }

    private data class DataWrapper(
        val calories: Int,
        val carbs: Int,
        val protein: Int,
        val fat: Int,
        val entries: List<DiaryEntryEntity>
    )

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_diary
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    false
                }
                R.id.navigation_diary -> true
                R.id.navigation_search -> {
                    startActivity(Intent(this, FoodSearchActivity::class.java))
                    false
                }
                R.id.navigation_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    finish()
                    false
                }
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
