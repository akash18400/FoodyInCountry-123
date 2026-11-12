package com.example.adkpractical

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.adkpractical.adapters.ScanHistoryAdapter
import com.example.adkpractical.api.RetrofitClient
import com.example.adkpractical.databinding.FragmentFoodScannerBinding
import com.example.adkpractical.models.ScanHistoryItem
import com.example.adkpractical.utils.PreferencesManager
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch

class FoodScannerFragment : Fragment() {

    private var _binding: FragmentFoodScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: ScanHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.btnScanBarcode.setOnClickListener {
            startBarcodeScanner()
        }

        loadHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = ScanHistoryAdapter { historyItem ->
            displayProductDetails(
                name = historyItem.productName,
                brand = historyItem.brand,
                quantity = historyItem.quantity,
                nutriscore = historyItem.nutriscoreGrade,
                imageUrl = historyItem.imageUrl
            )
        }
        binding.rvScanHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun loadHistory() {
        val history = PreferencesManager.getScanHistory(requireContext())
        historyAdapter.submitList(history)
        if (history.isNotEmpty()) {
            val latest = history.first()
            displayProductDetails(
                name = latest.productName,
                brand = latest.brand,
                quantity = latest.quantity,
                nutriscore = latest.nutriscoreGrade,
                imageUrl = latest.imageUrl
            )
        }
    }

    private fun startBarcodeScanner() {
        val scanner = GmsBarcodeScanning.getClient(requireContext())
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrEmpty()) {
                    fetchFoodDetails(rawValue)
                } else {
                    Toast.makeText(requireContext(), "No barcode detected", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFoodDetails(barcode: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.openFoodFactsApi.getProductDetails(barcode)
                showLoading(false)
                if (response.status == 1 && response.product != null) {
                    val p = response.product
                    val name = p.productName.orEmpty().ifEmpty { "Unknown Product" }
                    val brand = p.brands.orEmpty()
                    val quantity = p.quantity.orEmpty()
                    val nutriscore = p.nutriscoreGrade.orEmpty()
                    val imageUrl = p.imageUrl.orEmpty()

                    displayProductDetails(name, brand, quantity, nutriscore, imageUrl)

                    val item = ScanHistoryItem(
                        barcode = barcode,
                        productName = name,
                        brand = brand,
                        quantity = quantity,
                        nutriscoreGrade = nutriscore,
                        imageUrl = imageUrl
                    )
                    PreferencesManager.saveScanHistoryItem(requireContext(), item)
                    loadHistory()
                } else {
                    Toast.makeText(requireContext(), "Product not found for barcode: $barcode", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error fetching product: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayProductDetails(
        name: String,
        brand: String,
        quantity: String,
        nutriscore: String,
        imageUrl: String
    ) {
        binding.tvProductName.text = name
        val brandQty = listOfNotNull(brand.ifEmpty { null }, quantity.ifEmpty { null }).joinToString(" - ")
        binding.tvBrandQuantity.text = if (brandQty.isNotEmpty()) brandQty else "N/A"
        binding.tvNutriscore.text = if (nutriscore.isNotEmpty()) {
            "Nutri-Score: Grade ${nutriscore.uppercase()}"
        } else {
            "Nutri-Score: N/A"
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_food)
            .error(R.drawable.ic_food)
            .into(binding.ivProductImage)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarScanner.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnScanBarcode.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}