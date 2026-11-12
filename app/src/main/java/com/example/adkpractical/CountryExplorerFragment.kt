package com.example.adkpractical

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adkpractical.adapters.CountryAdapter
import com.example.adkpractical.api.RetrofitClient
import com.example.adkpractical.databinding.FragmentCountryExplorerBinding
import com.example.adkpractical.models.CountryItem
import com.example.adkpractical.utils.PreferencesManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

class CountryExplorerFragment : Fragment() {

    private var _binding: FragmentCountryExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var countryAdapter: CountryAdapter
    private var allCountries: List<CountryItem> = emptyList()
    private var currentFilterRegion: String = "All"
    private var currentSearchQuery: String = ""

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            detectCurrentCountry()
        } else {
            Toast.makeText(requireContext(), "Location permission denied. Trying fallback detection.", Toast.LENGTH_SHORT).show()
            fallbackCountryDetection()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupRegionChips()

        binding.btnDetectCountry.setOnClickListener {
            checkPermissionAndDetectCountry()
        }

        fetchCountries()
    }

    override fun onResume() {
        super.onResume()
        applyFilters()
    }

    private fun setupRecyclerView() {
        countryAdapter = CountryAdapter(
            onItemClick = { country ->
                openCountryDetails(country)
            },
            onWishlistToggle = { country ->
                val cca2 = country.cca2 ?: return@CountryAdapter
                val isAdded = PreferencesManager.toggleWishlist(requireContext(), cca2)
                val msg = if (isAdded) "Added to Wishlist" else "Removed from Wishlist"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                applyFilters()
            }
        )
        binding.rvCountries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = countryAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearchCountry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim().orEmpty()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRegionChips() {
        binding.chipGroupRegion.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val id = checkedIds.first()
            currentFilterRegion = when (id) {
                R.id.chipWishlist -> "Wishlist"
                R.id.chipAsia -> "Asia"
                R.id.chipEurope -> "Europe"
                R.id.chipAfrica -> "Africa"
                R.id.chipAmericas -> "Americas"
                R.id.chipOceania -> "Oceania"
                else -> "All"
            }
            applyFilters()
        }
    }

    private fun fetchCountries() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                allCountries = RetrofitClient.restCountriesApi.getAllCountries()
                showLoading(false)
                applyFilters()
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error loading countries: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFilters() {
        if (allCountries.isEmpty()) return

        val wishlistedCodes = PreferencesManager.getWishlist(requireContext())

        val filtered = allCountries.filter { country ->
            val nameMatches = currentSearchQuery.isEmpty() ||
                    country.name?.common?.contains(currentSearchQuery, ignoreCase = true) == true

            val regionMatches = when (currentFilterRegion) {
                "All" -> true
                "Wishlist" -> wishlistedCodes.contains(country.cca2)
                else -> country.region.equals(currentFilterRegion, ignoreCase = true)
            }

            nameMatches && regionMatches
        }

        countryAdapter.submitList(filtered)
    }

    private fun checkPermissionAndDetectCountry() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            detectCurrentCountry()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun detectCurrentCountry() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            val countryCode = addresses.firstOrNull()?.countryCode
                            if (!countryCode.isNullOrEmpty()) {
                                handleDetectedCountryCode(countryCode)
                            } else {
                                fallbackCountryDetection()
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val countryCode = addresses?.firstOrNull()?.countryCode
                        if (!countryCode.isNullOrEmpty()) {
                            handleDetectedCountryCode(countryCode)
                        } else {
                            fallbackCountryDetection()
                        }
                    }
                } else {
                    fallbackCountryDetection()
                }
            }.addOnFailureListener {
                fallbackCountryDetection()
            }
        } catch (e: SecurityException) {
            fallbackCountryDetection()
        }
    }

    private fun fallbackCountryDetection() {
        var countryCode: String? = null
        try {
            val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            countryCode = tm?.networkCountryIso?.uppercase()
            if (countryCode.isNullOrEmpty()) {
                countryCode = tm?.simCountryIso?.uppercase()
            }
        } catch (e: Exception) {
            // Ignore
        }

        if (countryCode.isNullOrEmpty()) {
            countryCode = Locale.getDefault().country
        }

        if (!countryCode.isNullOrEmpty()) {
            handleDetectedCountryCode(countryCode)
        } else {
            Toast.makeText(requireContext(), "Could not detect current country", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDetectedCountryCode(isoCode: String) {
        val matchedCountry = allCountries.find { it.cca2.equals(isoCode, ignoreCase = true) }
        if (matchedCountry != null) {
            Toast.makeText(requireContext(), "Detected: ${matchedCountry.name?.common}", Toast.LENGTH_SHORT).show()
            openCountryDetails(matchedCountry)
        } else {
            Toast.makeText(requireContext(), "Detected Country Code: $isoCode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCountryDetails(country: CountryItem) {
        val intent = Intent(requireContext(), CountryDetailActivity::class.java).apply {
            putExtra("COUNTRY_ITEM", country)
        }
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarCountries.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}