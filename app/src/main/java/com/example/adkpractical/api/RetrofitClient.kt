package com.example.adkpractical.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/"
    private const val REST_COUNTRIES_BASE_URL = "https://restcountries.com/"

    val openFoodFactsApi: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_FOOD_FACTS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    val restCountriesApi: RestCountriesApi by lazy {
        Retrofit.Builder()
            .baseUrl(REST_COUNTRIES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestCountriesApi::class.java)
    }
}