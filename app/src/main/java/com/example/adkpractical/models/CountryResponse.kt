package com.example.adkpractical.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CountryItem(
    @SerializedName("name") val name: CountryName? = null,
    @SerializedName("cca2") val cca2: String? = null,
    @SerializedName("capital") val capital: List<String>? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("population") val population: Long? = null,
    @SerializedName("flags") val flags: CountryFlags? = null,
    @SerializedName("currencies") val currencies: Map<String, CurrencyItem>? = null,
    @SerializedName("languages") val languages: Map<String, String>? = null
) : Serializable

data class CountryName(
    @SerializedName("common") val common: String? = null,
    @SerializedName("official") val official: String? = null
) : Serializable

data class CountryFlags(
    @SerializedName("png") val png: String? = null,
    @SerializedName("svg") val svg: String? = null,
    @SerializedName("alt") val alt: String? = null
) : Serializable

data class CurrencyItem(
    @SerializedName("name") val name: String? = null,
    @SerializedName("symbol") val symbol: String? = null
) : Serializable