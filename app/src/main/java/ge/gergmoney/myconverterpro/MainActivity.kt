package ge.gergmoney.myconverterpro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ge.gergmoney.myconverterpro.extension.EndPoints
import ge.gergmoney.myconverterpro.extension.Resource
import ge.gergmoney.myconverterpro.extension.Utility
import ge.gergmoney.myconverterpro.model.Rates
import ge.gergmoney.myconverterpro.databinding.ActivityMainBinding
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var selectedItem1: String? = "AFN"
    private var selectedItem2: String? = "AFN"
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.Theme_MyConverterPro_NoActionBar);
        super.onCreate(savedInstanceState)
        Utility.makeStatusBarTransparent(this)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val isChecked = intent.getBooleanExtra("isChecked", mainViewModel.isPopularCountriesChecked.value ?: false)
        mainViewModel.changeCheckBox(isChecked)

        initSpinner()
        setUpClickListener()
    }


    private fun initSpinner() {
        val spinner1 = binding.spnFirstCountry

        spinner1.setItems(getAllCountries())

        spinner1.setOnClickListener {
            Utility.hideKeyboard(this)
        }

        spinner1.setOnItemSelectedListener { _, _, _, item ->
            val countryCode = getCountryCode(item.toString())
            val currencySymbol = getSymbol(countryCode)
            selectedItem1 = currencySymbol
            binding.txtFirstCurrencyName.text = selectedItem1
        }

        val spinner2 = binding.spnSecondCountry

        spinner1.setOnClickListener {
            Utility.hideKeyboard(this)
        }

        spinner2.setItems(getAllCountries())

        spinner2.setOnItemSelectedListener { _, _, _, item ->
            val countryCode = getCountryCode(item.toString())
            val currencySymbol = getSymbol(countryCode)
            selectedItem2 = currencySymbol
            binding.txtSecondCurrencyName.text = selectedItem2
        }

    }

    private fun getSymbol(countryCode: String?): String? {
        val availableLocales = Locale.getAvailableLocales()
        for (i in availableLocales.indices) {
            if (availableLocales[i].country == countryCode
            ) return Currency.getInstance(availableLocales[i]).currencyCode
        }
        return ""
    }

    private fun getCountryCode(countryName: String) =
        Locale.getISOCountries().find { Locale("", it).displayCountry == countryName }

    private fun getAllCountries(): MutableList<String> {
        val isChecked = mainViewModel.isPopularCountriesChecked.value
        val locales = Locale.getAvailableLocales()
        val countries = mutableListOf<String>()
        for (locale in locales) {
            val country = locale.displayCountry
            if (country.trim { it <= ' ' }.isNotEmpty() && !countries.contains(country)) {
                countries.add(country)
            }
        }
        countries.sort()

        return if (isChecked == true) {
            countries.filter { mutableListOf(
                "Russia",
                "Austria",
                "Belarus",
                "China",
                "Germany",
                "France",
                "Georgia",
                "Poland",
                "Spain",
                "Ukraine",
                "United Kingdom",
                "United States"
            ).contains(it) }.toMutableList()
        }  else {
            countries
        }
    }

    private fun setUpClickListener() {
        binding.btnConvert.setOnClickListener {

            val numberToConvert = binding.etFirstCurrency.text.toString()

            if (numberToConvert.isEmpty() || numberToConvert == "0") {
                Snackbar.make(
                    binding.mainLayout,
                    "Input a value in the first text field, result will be shown in the second text field",
                    Snackbar.LENGTH_LONG
                )
                    .withColor(ContextCompat.getColor(this, R.color.dark_red))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                    .show()
            } else if (!Utility.isNetworkAvailable(this)) {
                Snackbar.make(
                    binding.mainLayout,
                    "You are not connected to the internet",
                    Snackbar.LENGTH_LONG
                )
                    .withColor(ContextCompat.getColor(this, R.color.dark_red))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                    .show()
            } else {
                doConversion()
            }
        }

        // TODO CONTACTS FORM
        binding.txtContact.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        // TODO CONTACTS FORM
        binding.txtSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                putExtra("isChecked", mainViewModel.isPopularCountriesChecked.value)
            }
            startActivity(intent)
        }

    }

    private fun doConversion() {

        Utility.hideKeyboard(this)
        binding.prgLoading.visibility = View.VISIBLE
        binding.btnConvert.visibility = View.GONE

        val apiKey = EndPoints.API_KEY
        val from = selectedItem1.toString()
        val to = selectedItem2.toString()
        val amount = binding.etFirstCurrency.text.toString().toDouble()

        mainViewModel.getConvertedData(apiKey, from, to, amount)
        observeUi()

    }

    @SuppressLint("SetTextI18n")
    private fun observeUi() {
        mainViewModel.data.observe(this) { result ->

            when (result.status) {
                Resource.Status.SUCCESS -> {
                    if (result.data?.status == "success") {

                        val map: Map<String, Rates>

                        map = result.data.rates

                        map.keys.forEach {
                            val rateForAmount = map[it]?.rate_for_amount
                            mainViewModel.convertedRate.value = rateForAmount
                            val formattedString =
                                String.format("%,.2f", mainViewModel.convertedRate.value)
                            binding.etSecondCurrency.setText(formattedString)
                        }



                        binding.prgLoading.visibility = View.GONE
                        binding.btnConvert.visibility = View.VISIBLE
                    } else if (result.data?.status == "fail") {
                        val layout = binding.mainLayout
                        Snackbar.make(
                            layout,
                            "Ooops! something went wrong, Try again",
                            Snackbar.LENGTH_LONG
                        )
                            .withColor(ContextCompat.getColor(this, R.color.dark_red))
                            .setTextColor(ContextCompat.getColor(this, R.color.white))
                            .show()

                        binding.prgLoading.visibility = View.GONE
                        binding.btnConvert.visibility = View.VISIBLE
                    }
                }
                Resource.Status.ERROR -> {
                    val layout = binding.mainLayout
                    Snackbar.make(
                        layout,
                        "Oopps! Something went wrong, Try again",
                        Snackbar.LENGTH_LONG
                    )
                        .withColor(ContextCompat.getColor(this, R.color.dark_red))
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .show()
                    binding.prgLoading.visibility = View.GONE
                    binding.btnConvert.visibility = View.VISIBLE
                }

                Resource.Status.LOADING -> {
                    binding.prgLoading.visibility = View.VISIBLE
                    binding.btnConvert.visibility = View.GONE
                }
            }
        }
    }

    private fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
        this.view.setBackgroundColor(colorInt)
        return this
    }

}