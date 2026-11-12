package com.example.adkpractical.models

data class ScanHistoryItem(
    val barcode: String,
    val productName: String,
    val brand: String,
    val quantity: String,
    val nutriscoreGrade: String,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)