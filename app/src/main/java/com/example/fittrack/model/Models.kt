package com.example.fittrack.model

import java.util.Date

data class FoodEntry(
    val id: String,
    val name: String,
    val brand: String,
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int,
    val servingSize: String,
    val mealType: MealType,
    val date: Date
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACKS
}

data class DailyLog(
    val date: Date,
    val totalCalories: Int,
    val calorieGoal: Int,
    val steps: Int,
    val stepsGoal: Int,
    val carbsConsumed: Int,
    val carbsGoal: Int,
    val proteinConsumed: Int,
    val proteinGoal: Int,
    val fatConsumed: Int,
    val fatGoal: Int
)