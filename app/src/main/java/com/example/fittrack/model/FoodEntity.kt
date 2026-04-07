package com.example.fittrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int,
    val servingSize: String
)
