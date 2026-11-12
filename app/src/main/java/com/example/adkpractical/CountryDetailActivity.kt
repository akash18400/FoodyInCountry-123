package com.example.adkpractical

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.adkpractical.databinding.ActivityCountryDetailBinding
import com.example.adkpractical.models.CountryItem
import com.example.adkpractical.utils.PreferencesManager
import java.text.NumberFormat
import java.util.Locale

class CountryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountryDetailBinding
    private var country: CountryItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        country = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("COUNTRY_ITEM", CountryItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("COUNTRY_ITEM") as? CountryItem
        }

        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }

        if (country == null) {
            Toast.makeText(this, "Country details unavailable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        populateUI()
    }

    private fun populateUI() {
        val item = country ?: return
        val commonName = item.name?.common ?: "Unknown"
        val officialName = item.name?.official ?: ""
        val cca2 = item.cca2 ?: ""

        binding.toolbarDetail.title = commonName
        binding.tvDetailCommonName.text = commonName
        binding.tvDetailOfficialName.text = officialName

        Glide.with(this)
            .load(item.flags?.png)
            .placeholder(R.drawable.ic_public)
            .error(R.drawable.ic_public)
            .into(binding.ivDetailFlag)

        val capitals = item.capital?.joinToString(", ") ?: "N/A"
        binding.tvDetailCapital.text = "Capital: $capitals"

        binding.tvDetailRegion.text = "Region: ${item.region ?: "N/A"}"

        val popFormatted = item.population?.let {
            NumberFormat.getInstance(Locale.getDefault()).format(it)
        } ?: "N/A"
        binding.tvDetailPopulation.text = "Population: $popFormatted"

        val currencyStr = item.currencies?.values?.joinToString(", ") { curr ->
            val symbol = if (!curr.symbol.isNullOrEmpty()) " (${curr.symbol})" else ""
            "${curr.name ?: "Unknown"}$symbol"
        } ?: "N/A"
        binding.tvDetailCurrencies.text = "Currencies: $currencyStr"

        val languageStr = item.languages?.values?.joinToString(", ") ?: "N/A"
        binding.tvDetailLanguages.text = "Languages: $languageStr"

        updateWishlistButton(cca2)

        binding.btnDetailWishlist.setOnClickListener {
            val isAdded = PreferencesManager.toggleWishlist(this, cca2)
            val msg = if (isAdded) "Added to Wishlist" else "Removed from Wishlist"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            updateWishlistButton(cca2)
        }
    }

    private fun updateWishlistButton(cca2: String) {
        val isWishlisted = PreferencesManager.isWishlisted(this, cca2)
        if (isWishlisted) {
            binding.btnDetailWishlist.text = "Remove from Wish-to-Visit"
            binding.btnDetailWishlist.setIconResource(R.drawable.ic_star_filled)
        } else {
            binding.btnDetailWishlist.text = "Add to Wish-to-Visit"
            binding.btnDetailWishlist.setIconResource(R.drawable.ic_star_border)
        }
    }
}