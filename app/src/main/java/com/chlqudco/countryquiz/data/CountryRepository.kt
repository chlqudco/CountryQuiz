package com.chlqudco.countryquiz.data

import android.content.Context
import com.chlqudco.countryquiz.model.Country
import com.chlqudco.countryquiz.model.CountryCatalog
import com.chlqudco.countryquiz.model.Region
import org.json.JSONArray
import org.json.JSONObject

class CountryRepository(private val context: Context) {
    fun loadCatalog(): CountryCatalog {
        val json = context.assets.open("countries.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val countries = root.getJSONArray("countries").toCountries()
        return CountryCatalog(
            version = root.getString("version"),
            source = root.getString("source"),
            sourceUrl = root.getString("sourceUrl"),
            countries = countries
        )
    }

    private fun JSONArray.toCountries(): List<Country> = buildList {
        repeat(length()) { index ->
            val item = getJSONObject(index)
            add(
                Country(
                    iso2 = item.getString("iso2"),
                    countryKo = item.getString("countryKo"),
                    countryEn = item.optString("countryEn"),
                    countryAliases = item.getJSONArray("countryAliases").toStringList(),
                    capitalKo = item.getString("capitalKo"),
                    capitalEn = item.optString("capitalEn"),
                    capitalAliases = item.getJSONArray("capitalAliases").toStringList(),
                    capitalRaw = item.optString("capitalRaw"),
                    flagResName = item.getString("flagResName"),
                    region = Region.fromLabel(item.getString("region")),
                    difficulty = item.getInt("difficulty"),
                    population = item.optLong("population"),
                    area = item.optDouble("area"),
                    quizEnabled = item.optBoolean("quizEnabled", true)
                )
            )
        }
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        repeat(length()) { add(getString(it)) }
    }
}
