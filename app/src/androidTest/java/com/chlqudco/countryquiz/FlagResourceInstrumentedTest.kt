package com.chlqudco.countryquiz

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chlqudco.countryquiz.data.FlagResources
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlagResourceInstrumentedTest {
    @Test
    fun everyBundledFlagCanBeDecoded() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val json = context.assets.open("countries.json").bufferedReader().use { it.readText() }
        val countries = JSONObject(json).getJSONArray("countries")

        assertEquals(199, countries.length())
        repeat(countries.length()) { index ->
            val iso2 = countries.getJSONObject(index).getString("iso2")
            val drawable = context.getDrawable(FlagResources.id(iso2))
            assertNotNull(iso2, drawable)
            assertTrue(iso2, drawable!!.intrinsicWidth > 0)
            assertTrue(iso2, drawable.intrinsicHeight > 0)
        }
    }
}
