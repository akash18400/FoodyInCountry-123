package com.example.adkpractical.api

import com.example.adkpractical.models.CountryItem
import retrofit2.http.GET
import retrofit2.http.Query

interface RestCountriesApi {
    @GET("v3.1/all")
    suspend fun getAllCountries(
        @Query("fields") fields: String = "name,cca2,capital,region,population,flags,currencies,languages"
    ): List<CountryItem>
}