package com.chlqudco.countryquiz.quiz

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryDataIntegrityTest {
    @Test
    fun everyCountryHasUniqueIsoCapitalAndFlag() {
        val assetFile = resolve("src/main/assets/countries.json", "app/src/main/assets/countries.json")
        val drawableDirectory = resolve("src/main/res/drawable-nodpi", "app/src/main/res/drawable-nodpi")
        val json = assetFile.readText(Charsets.UTF_8)
        val isoCodes = Regex("\"iso2\"\\s*:\\s*\"([A-Z]{2})\"").findAll(json).map { it.groupValues[1] }.toList()
        val capitals = Regex("\"capitalKo\"\\s*:\\s*\"([^\"]+)\"").findAll(json).map { it.groupValues[1] }.toList()
        val flagCodes = drawableDirectory.listFiles().orEmpty().mapNotNull {
            Regex("flag_([a-z]{2})\\.[a-z0-9]+").matchEntire(it.name)?.groupValues?.get(1)?.uppercase()
        }.toSet()

        assertEquals(199, isoCodes.size)
        assertEquals(isoCodes.size, isoCodes.distinct().size)
        assertEquals(isoCodes.size, capitals.size)
        assertTrue(capitals.none { it.isBlank() })
        assertEquals(isoCodes.toSet(), flagCodes)
        assertTrue(isoCodes.containsAll(listOf("KR", "KP", "TW")))
    }

    private fun resolve(vararg candidates: String): File {
        return candidates.map(::File).firstOrNull { it.exists() }
            ?: error("Required test file not found: ${candidates.joinToString()}")
    }
}
