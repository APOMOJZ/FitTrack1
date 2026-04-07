package com.example.fittrack.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodService {
    @GET("cgi/search.pl?search_simple=1&action=process&json=1")
    suspend fun searchFood(
        @Query("search_terms") query: String,
        @Query("page_size") pageSize: Int = 20
    ): OpenFoodResponse
}

data class OpenFoodResponse(
    val products: List<OpenFoodProduct>
)

data class OpenFoodProduct(
    val product_name: String?,
    val brands: String?,
    val nutriments: OpenFoodNutriments?,
    val serving_size: String?
)

data class OpenFoodNutriments(
    val energy_100g: Double?,
    val carbohydrates_100g: Double?,
    val proteins_100g: Double?,
    val fat_100g: Double?
)
