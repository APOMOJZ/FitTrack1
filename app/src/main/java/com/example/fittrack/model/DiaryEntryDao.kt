package com.example.fittrack.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Insert
    suspend fun insert(entry: DiaryEntryEntity)

    @Query("SELECT * FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getEntriesForDay(startOfDay: Long, endOfDay: Long): Flow<List<DiaryEntryEntity>>

    @Query("SELECT SUM(calories) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTotalCaloriesForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(calories) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getTotalCaloriesForDayOnce(startOfDay: Long, endOfDay: Long): Int?
    
    @Query("SELECT SUM(carbs) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTotalCarbsForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(carbs) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getTotalCarbsForDayOnce(startOfDay: Long, endOfDay: Long): Int?

    @Query("SELECT SUM(protein) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTotalProteinForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(protein) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getTotalProteinForDayOnce(startOfDay: Long, endOfDay: Long): Int?

    @Query("SELECT SUM(fat) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTotalFatForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(fat) FROM diary_entries WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getTotalFatForDayOnce(startOfDay: Long, endOfDay: Long): Int?
}
