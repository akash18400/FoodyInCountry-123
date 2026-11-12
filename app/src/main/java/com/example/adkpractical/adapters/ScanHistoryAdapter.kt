package com.example.adkpractical.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adkpractical.R
import com.example.adkpractical.databinding.ItemScanHistoryBinding
import com.example.adkpractical.models.ScanHistoryItem

class ScanHistoryAdapter(
    private val onItemClick: (ScanHistoryItem) -> Unit
) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<ScanHistoryItem>()

    fun submitList(newItems: List<ScanHistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScanHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemScanHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanHistoryItem) {
            binding.tvHistoryProductName.text = item.productName.ifEmpty { "Unknown Product" }
            val brandQty = listOfNotNull(item.brand.ifEmpty { null }, item.quantity.ifEmpty { null })
                .joinToString(" - ")
            binding.tvHistoryBrandQuantity.text = if (brandQty.isNotEmpty()) brandQty else "N/A"

            binding.tvHistoryNutriscore.text = if (item.nutriscoreGrade.isNotEmpty()) {
                "Grade ${item.nutriscoreGrade.uppercase()}"
            } else {
                "Grade N/A"
            }

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_food)
                .error(R.drawable.ic_food)
                .into(binding.ivHistoryProduct)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}