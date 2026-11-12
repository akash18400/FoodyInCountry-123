package com.example.adkpractical.models

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponse(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("status_verbose") val statusVerbose: String? = null,
    @SerializedName("product") val product: ProductData? = null
)

data class ProductData(
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("brands") val brands: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("nutriscore_grade") val nutriscoreGrade: String? = null,
    @SerializedName("quantity") val quantity: String? = null
)