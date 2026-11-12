package com.example.adkpractical.api

import com.example.adkpractical.models.OpenFoodFactsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProductDetails(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "product_name,brands,image_url,nutriscore_grade,quantity"
    ): OpenFoodFactsResponse
}