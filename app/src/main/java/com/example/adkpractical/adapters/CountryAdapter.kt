package com.example.adkpractical.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adkpractical.R
import com.example.adkpractical.databinding.ItemCountryBinding
import com.example.adkpractical.models.CountryItem
import com.example.adkpractical.utils.PreferencesManager

class CountryAdapter(
    private val onItemClick: (CountryItem) -> Unit,
    private val onWishlistToggle: (CountryItem) -> Unit
) : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    private val items = mutableListOf<CountryItem>()

    fun submitList(newItems: List<CountryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: CountryItem) {
            val name = country.name?.common ?: "Unknown"
            val region = country.region ?: "Unknown Region"
            val cca2 = country.cca2 ?: ""

            binding.tvCountryName.text = name
            binding.tvCountryRegion.text = region

            Glide.with(binding.root.context)
                .load(country.flags?.png)
                .placeholder(R.drawable.ic_public)
                .error(R.drawable.ic_public)
                .into(binding.ivCountryFlag)

            val isWishlisted = PreferencesManager.isWishlisted(binding.root.context, cca2)
            val iconRes = if (isWishlisted) R.drawable.ic_star_filled else R.drawable.ic_star_border
            binding.btnWishlistToggle.setImageResource(iconRes)

            binding.btnWishlistToggle.setOnClickListener {
                onWishlistToggle(country)
            }

            binding.root.setOnClickListener {
                onItemClick(country)
            }
        }
    }
}