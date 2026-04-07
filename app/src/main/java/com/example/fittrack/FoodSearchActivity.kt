package com.example.fittrack

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fittrack.adapter.FoodAdapter
import com.example.fittrack.api.OpenFoodService
import com.example.fittrack.databinding.ActivityFoodSearchBinding
import com.example.fittrack.databinding.BottomSheetFoodDetailBinding
import com.example.fittrack.model.AppDatabase
import com.example.fittrack.model.FoodEntity
import com.example.fittrack.model.FoodEntry
import com.example.fittrack.model.MealType
import com.example.fittrack.model.DiaryEntryEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

class FoodSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodSearchBinding
    private lateinit var db: AppDatabase

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val openFoodService = retrofit.create(OpenFoodService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        db = AppDatabase.getDatabase(this, lifecycleScope)
        
        setupRecyclerView()
        setupSearchView()
        loadAllFoods()
    }

    private fun setupRecyclerView() {
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
    }

    private fun loadAllFoods() {
        lifecycleScope.launch {
            val foods = db.foodDao().getAllFoods()
            updateList(foods)
        }
    }

    private fun updateList(foods: List<FoodEntity>) {
        val displayFoods = foods.map { 
            FoodEntry(it.id.toString(), it.name, it.brand ?: "Generic", it.calories, it.carbs, it.protein, it.fat, it.servingSize, MealType.BREAKFAST, Date())
        }
        binding.rvSearchResults.adapter = FoodAdapter(displayFoods) { food ->
            showFoodDetail(food)
        }
    }

    private fun showFoodDetail(food: FoodEntry) {
        val dialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetFoodDetailBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.tvFoodNameDetail.text = food.name
        bottomSheetBinding.tvBrandDetail.text = food.brand
        
        bottomSheetBinding.btnAddDetail.setOnClickListener {
            saveFoodToDiary(food)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveFoodToDiary(food: FoodEntry) {
        lifecycleScope.launch {
            val entry = DiaryEntryEntity(
                name = food.name,
                calories = food.calories,
                carbs = food.carbs,
                protein = food.protein,
                fat = food.fat,
                mealType = food.mealType.name,
                date = System.currentTimeMillis()
            )
            db.diaryEntryDao().insert(entry)
            
            Snackbar.make(binding.root, "${food.name} added to diary", Snackbar.LENGTH_SHORT).show()
            binding.root.postDelayed({ finish() }, 800)
        }
    }

    private fun setupSearchView() {
        binding.searchView.requestFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchFoods(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    loadAllFoods()
                } else if (newText.length > 2) {
                    searchFoods(newText)
                }
                return true
            }
        })
    }

    private fun searchFoods(query: String?) {
        lifecycleScope.launch {
            try {
                val response = openFoodService.searchFood(query ?: "")
                val onlineResults = response.products.map { product ->
                    val kCal = product.nutriments?.energy_100g?.let { (it * 0.239).toInt() } ?: 0
                    FoodEntity(
                        name = product.product_name ?: "Unknown Product",
                        brand = product.brands ?: "Generic",
                        calories = kCal,
                        carbs = product.nutriments?.carbohydrates_100g?.toInt() ?: 0,
                        protein = product.nutriments?.proteins_100g?.toInt() ?: 0,
                        fat = product.nutriments?.fat_100g?.toInt() ?: 0,
                        servingSize = product.serving_size ?: "100g"
                    )
                }.filter { it.name != "Unknown Product" }
                
                if (onlineResults.isNotEmpty()) {
                    updateList(onlineResults)
                } else {
                    val localResults = db.foodDao().searchFoods("%$query%")
                    updateList(localResults)
                }
            } catch (e: Exception) {
                Log.e("FoodSearch", "API Error", e)
                val results = db.foodDao().searchFoods("%$query%")
                updateList(results)
            }
        }
    }
}
