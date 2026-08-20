package com.chlqudco.countryquiz.quiz

object FlagSimilarity {
    private val groups = listOf(
        setOf("RO", "TD"),
        setOf("ID", "MC", "PL"),
        setOf("IE", "CI"),
        setOf("IT", "MX"),
        setOf("NL", "LU"),
        setOf("AU", "NZ"),
        setOf("BH", "QA"),
        setOf("CO", "EC", "VE"),
        setOf("GN", "ML", "SN"),
        setOf("DK", "FI", "IS", "NO", "SE"),
        setOf("CR", "KP", "TH"),
        setOf("RU", "RS", "SI", "SK", "HR"),
        setOf("JO", "KW", "PS", "SD", "AE"),
        setOf("CN", "MA", "TR", "TN", "VN"),
        setOf("CU", "PR", "CL"),
        setOf("GT", "HN", "NI", "SV"),
        setOf("CZ", "PH"),
        setOf("GR", "UY"),
        setOf("JP", "BD"),
        setOf("NO", "IS"),
        setOf("AT", "LV", "LB"),
        setOf("BE", "DE"),
        setOf("NE", "IN"),
        setOf("GH", "ET", "SN"),
        setOf("LR", "MY", "US")
    )

    val countryCodes: Set<String> = groups.flatten().toSet()

    fun score(answerIso: String, candidateIso: String): Int {
        val matches = groups.mapIndexedNotNull { index, group ->
            index.takeIf { answerIso in group && candidateIso in group }
        }
        return matches.minOrNull() ?: Int.MAX_VALUE
    }
}
