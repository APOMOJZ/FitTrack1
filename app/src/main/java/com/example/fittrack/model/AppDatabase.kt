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
                FoodEntity(name = "Apple", brand = "Fresh", calories = 95, carbs = 25, protein = 0, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Banana", brand = "Fresh", calories = 105, carbs = 27, protein = 1, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Chicken Breast", brand = "Generic", calories = 165, carbs = 0, protein = 31, fat = 4, servingSize = "100g"),
                FoodEntity(name = "Brown Rice", brand = "Generic", calories = 216, carbs = 45, protein = 5, fat = 2, servingSize = "1 cup"),
                FoodEntity(name = "Greek Yogurt", brand = "Chobani", calories = 120, carbs = 15, protein = 12, fat = 0, servingSize = "1 container"),
                FoodEntity(name = "Almonds", brand = "Generic", calories = 160, carbs = 6, protein = 6, fat = 14, servingSize = "28g"),
                FoodEntity(name = "Oatmeal", brand = "Quaker", calories = 150, carbs = 27, protein = 5, fat = 3, servingSize = "1 cup"),
                FoodEntity(name = "Egg", brand = "Large", calories = 70, carbs = 0, protein = 6, fat = 5, servingSize = "1 egg"),
                FoodEntity(name = "Milk", brand = "Whole", calories = 150, carbs = 12, protein = 8, fat = 8, servingSize = "1 cup"),
                FoodEntity(name = "Peanut Butter", brand = "Jif", calories = 190, carbs = 8, protein = 7, fat = 16, servingSize = "2 tbsp"),
                FoodEntity(name = "Salmon", brand = "Wild Caught", calories = 208, carbs = 0, protein = 22, fat = 13, servingSize = "100g"),
                FoodEntity(name = "Broccoli", brand = "Fresh", calories = 55, carbs = 11, protein = 4, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Avocado", brand = "Hass", calories = 240, carbs = 12, protein = 3, fat = 22, servingSize = "1 medium"),
                FoodEntity(name = "Whole Wheat Bread", brand = "Oroweat", calories = 100, carbs = 19, protein = 4, fat = 1, servingSize = "1 slice"),
                FoodEntity(name = "Sweet Potato", brand = "Fresh", calories = 112, carbs = 26, protein = 2, fat = 0, servingSize = "Medium"),
                FoodEntity(name = "Quinoa", brand = "Organic", calories = 222, carbs = 39, protein = 8, fat = 4, servingSize = "1 cup cooked"),
                FoodEntity(name = "Spinach", brand = "Fresh", calories = 7, carbs = 1, protein = 1, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Blueberries", brand = "Fresh", calories = 85, carbs = 21, protein = 1, fat = 0, servingSize = "1 cup"),
                FoodEntity(name = "Cheese", brand = "Cheddar", calories = 115, carbs = 1, protein = 7, fat = 9, servingSize = "1 slice (28g)"),
                FoodEntity(name = "Coffee", brand = "Black", calories = 2, carbs = 0, protein = 0, fat = 0, servingSize = "1 cup")
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
                .fallbackToDestructiveMigration() // Simplified for now
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
