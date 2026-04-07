package com.example.fittrack.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FoodEntity::class, DiaryEntryEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun diaryEntryDao(): DiaryEntryDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.foodDao())
                }
            }
        }

        suspend fun populateDatabase(foodDao: FoodDao) {
            val commonFoods = listOf(
                // Fruits
                FoodEntity(name = "Apple", brand = "Fresh", calories = 95, carbs = 25, protein = 0, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Banana", brand = "Fresh", calories = 105, carbs = 27, protein = 1, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Blueberries", brand = "Fresh", calories = 85, carbs = 21, protein = 1, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Strawberry", brand = "Fresh", calories = 49, carbs = 12, protein = 1, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Orange", brand = "Fresh", calories = 62, carbs = 15, protein = 1, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Mango", brand = "Fresh", calories = 202, carbs = 50, protein = 3, fat = 1, servingSize = "1 cup"),
                FoodEntity(name = "Watermelon", brand = "Fresh", calories = 46, carbs = 11, protein = 1, fat = 0, servingSize = "1 cup"),
                
                // Proteins
                FoodEntity(name = "Chicken Breast", brand = "Generic", calories = 165, carbs = 0, protein = 31, fat = 4, servingSize = "100g"),
                FoodEntity(name = "Egg", brand = "Large", calories = 70, carbs = 0, protein = 6, fat = 5, servingSize = "1 egg"),
                FoodEntity(name = "Salmon", brand = "Wild Caught", calories = 208, carbs = 0, protein = 22, fat = 13, servingSize = "100g"),
                FoodEntity(name = "Ground Beef (90%)", brand = "Generic", calories = 176, carbs = 0, protein = 20, fat = 10, servingSize = "100g"),
                FoodEntity(name = "Tofu (Firm)", brand = "Generic", calories = 144, carbs = 4, protein = 16, fat = 9, servingSize = "1/2 cup"),
                FoodEntity(name = "Turkey Breast", brand = "Generic", calories = 135, carbs = 0, protein = 30, fat = 1, servingSize = "100g"),
                FoodEntity(name = "Canned Tuna", brand = "Generic", calories = 120, carbs = 0, protein = 26, fat = 1, servingSize = "1 can"),
                
                // Carbs/Grains
                FoodEntity(name = "Brown Rice", brand = "Generic", calories = 216, carbs = 45, protein = 5, fat = 2, servingSize = "1 cup cooked"),
                FoodEntity(name = "White Rice", brand = "Generic", calories = 205, carbs = 45, protein = 4, fat = 0, servingSize = "1 cup cooked"),
                FoodEntity(name = "Oatmeal", brand = "Quaker", calories = 150, carbs = 27, protein = 5, fat = 3, servingSize = "1 cup cooked"),
                FoodEntity(name = "Whole Wheat Bread", brand = "Oroweat", calories = 100, carbs = 19, protein = 4, fat = 1, servingSize = "1 slice"),
                FoodEntity(name = "Quinoa", brand = "Organic", calories = 222, carbs = 39, protein = 8, fat = 4, servingSize = "1 cup cooked"),
                FoodEntity(name = "Sweet Potato", brand = "Fresh", calories = 112, carbs = 26, protein = 2, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Potato (Boiled)", brand = "Fresh", calories = 87, carbs = 20, protein = 2, fat = 0, servingSize = "100g"),
                FoodEntity(name = "Pasta (Penne)", brand = "Generic", calories = 220, carbs = 43, protein = 8, fat = 1, servingSize = "1 cup cooked"),
                
                // Dairy
                FoodEntity(name = "Greek Yogurt", brand = "Chobani", calories = 120, carbs = 15, protein = 12, fat = 0, servingSize = "1 container"),
                FoodEntity(name = "Milk (Whole)", brand = "Generic", calories = 150, carbs = 12, protein = 8, fat = 8, servingSize = "1 cup"),
                FoodEntity(name = "Milk (Skim)", brand = "Generic", calories = 80, carbs = 12, protein = 8, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Cheddar Cheese", brand = "Generic", calories = 115, carbs = 1, protein = 7, fat = 9, servingSize = "1 slice"),
                FoodEntity(name = "Cottage Cheese", brand = "Generic", calories = 100, carbs = 3, protein = 11, fat = 4, servingSize = "1/2 cup"),
                FoodEntity(name = "Butter", brand = "Generic", calories = 100, carbs = 0, protein = 0, fat = 11, servingSize = "1 tbsp"),
                
                // Vegetables
                FoodEntity(name = "Broccoli", brand = "Fresh", calories = 55, carbs = 11, protein = 4, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Spinach", brand = "Fresh", calories = 7, carbs = 1, protein = 1, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Carrots", brand = "Fresh", calories = 41, carbs = 10, protein = 1, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Avocado", brand = "Hass", calories = 240, carbs = 12, protein = 3, fat = 22, servingSize = "1 medium"),
                FoodEntity(name = "Tomato", brand = "Fresh", calories = 22, carbs = 5, protein = 1, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Cucumber", brand = "Fresh", calories = 16, carbs = 4, protein = 1, fat = 0, servingSize = "1 cup"),
                
                // Snacks/Fats
                FoodEntity(name = "Almonds", brand = "Generic", calories = 160, carbs = 6, protein = 6, fat = 14, servingSize = "28g"),
                FoodEntity(name = "Peanut Butter", brand = "Jif", calories = 190, carbs = 8, protein = 7, fat = 16, servingSize = "2 tbsp"),
                FoodEntity(name = "Olive Oil", brand = "Generic", calories = 119, carbs = 0, protein = 0, fat = 14, servingSize = "1 tbsp"),
                FoodEntity(name = "Hummus", brand = "Sabra", calories = 70, carbs = 4, protein = 2, fat = 5, servingSize = "2 tbsp"),
                FoodEntity(name = "Dark Chocolate", brand = "70%", calories = 170, carbs = 13, protein = 2, fat = 12, servingSize = "28g"),
                
                // Fast Food / Restaurant
                FoodEntity(name = "Cheeseburger", brand = "McDonald's", calories = 300, carbs = 33, protein = 15, fat = 12, servingSize = "1 burger"),
                FoodEntity(name = "Big Mac", brand = "McDonald's", calories = 550, carbs = 45, protein = 25, fat = 30, servingSize = "1 burger"),
                FoodEntity(name = "Large Fries", brand = "McDonald's", calories = 480, carbs = 62, protein = 5, fat = 23, servingSize = "1 order"),
                FoodEntity(name = "Pizza Slice", brand = "Pepperoni", calories = 285, carbs = 36, protein = 12, fat = 10, servingSize = "1 slice"),
                FoodEntity(name = "Burrito Bowl", brand = "Chipotle", calories = 650, carbs = 60, protein = 40, fat = 25, servingSize = "1 bowl"),
                
                // Beverages
                FoodEntity(name = "Coffee (Black)", brand = "Generic", calories = 2, carbs = 0, protein = 0, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Coke", brand = "Coca-Cola", calories = 140, carbs = 39, protein = 0, fat = 0, servingSize = "12 oz"),
                FoodEntity(name = "Orange Juice", brand = "Generic", calories = 110, carbs = 26, protein = 2, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Green Tea", brand = "Generic", calories = 2, carbs = 0, protein = 0, fat = 0, servingSize = "1 cup")
            )
            foodDao.insertAll(commonFoods)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fittrack_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
