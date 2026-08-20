package com.chlqudco.countryquiz.quiz

import com.chlqudco.countryquiz.model.Country
import com.chlqudco.countryquiz.model.AnswerFormat
import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.Difficulty
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.QuizConfig
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.Region
import com.chlqudco.countryquiz.model.ReviewTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizEngineTest {
    private val engine = QuizEngine()
    private val countries = buildList {
        repeat(8) { add(country(index = it, region = Region.ASIA)) }
        repeat(8) { add(country(index = it + 8, region = Region.EUROPE)) }
    }

    @Test
    fun connectionQuestionsContainOneCorrectChoiceAndUniqueOptions() {
        val questions = engine.generateQuestions(
            countries = countries,
            config = QuizConfig(
                quizMode = QuizMode.COUNTRY_TO_CAPITAL,
                difficulty = Difficulty.HARD,
                questionCount = 10
            ),
            seed = 7L
        )

        assertEquals(10, questions.size)
        questions.forEach { question ->
            assertEquals(4, question.choices.size)
            assertEquals(4, question.choices.map { it.label }.distinct().size)
            assertEquals(1, question.choices.count { it.id == question.correctChoiceId })
        }
    }

    @Test
    fun regionAndDifficultyFiltersAreApplied() {
        val questions = engine.generateQuestions(
            countries = countries,
            config = QuizConfig(
                quizMode = QuizMode.FLAG_TO_COUNTRY,
                region = Region.EUROPE,
                difficulty = Difficulty.HARD,
                questionCount = 5
            ),
            seed = 11L
        )

        assertEquals(5, questions.size)
        assertTrue(questions.all { question ->
            countries.first { it.iso2 == question.countryIso }.region == Region.EUROPE
        })
    }

    @Test
    fun mixedQuizCreatesEveryConcreteMode() {
        val questions = engine.generateQuestions(
            countries = countries,
            config = QuizConfig(
                quizMode = QuizMode.MIXED,
                gameMode = GameMode.DAILY,
                difficulty = Difficulty.HARD
            ),
            seed = 19L
        )

        assertEquals(10, questions.size)
        assertTrue(questions.none { it.mode == QuizMode.MIXED })
        assertEquals(questions.size, questions.map { it.id }.distinct().size)
        assertTrue(questions.map { it.mode }.distinct().size >= 7)
    }

    @Test
    fun reviewTargetsKeepCountryAndRelationship() {
        val target = ReviewTarget(countryIso = countries[3].iso2, mode = QuizMode.CAPITAL_TO_COUNTRY)
        val questions = engine.generateQuestions(
            countries = countries,
            config = QuizConfig(
                quizMode = QuizMode.MIXED,
                gameMode = GameMode.REVIEW,
                difficulty = Difficulty.HARD,
                questionCount = 1
            ),
            reviewTargets = listOf(target),
            seed = 23L
        )

        assertEquals(1, questions.size)
        assertEquals(target.countryIso, questions.single().countryIso)
        assertEquals(target.mode, questions.single().mode)
    }

    @Test
    fun typingQuestionsHaveAliasesInsteadOfChoices() {
        val questions = engine.generateQuestions(
            countries = countries,
            config = QuizConfig(
                quizMode = QuizMode.MIXED,
                difficulty = Difficulty.HARD,
                questionCount = 8,
                answerFormat = AnswerFormat.TYPING
            ),
            seed = 29L
        )

        assertEquals(8, questions.size)
        assertTrue(questions.all { it.choiceType == ChoiceType.INPUT })
        assertTrue(questions.all { it.choices.isEmpty() && it.acceptedAnswers.isNotEmpty() })
        assertTrue(questions.all { it.mode.supportsTyping })
    }

    @Test
    fun similarFlagModeUsesConfusionGroupsForOptions() {
        val similarCountries = listOf(
            "RO", "TD", "ID", "MC", "PL", "IE", "CI", "IT", "MX", "NL", "LU", "AU", "NZ", "BH", "QA"
        ).mapIndexed { index, iso -> country(index, Region.EUROPE, iso) }
        val questions = engine.generateQuestions(
            countries = similarCountries,
            config = QuizConfig(
                quizMode = QuizMode.MIXED,
                gameMode = GameMode.SIMILAR_FLAGS,
                difficulty = Difficulty.HARD,
                questionCount = 10
            ),
            seed = 31L
        )

        assertEquals(10, questions.size)
        assertTrue(questions.all { it.mode == QuizMode.FLAG_TO_COUNTRY || it.mode == QuizMode.COUNTRY_TO_FLAG })
        assertTrue(questions.all { question ->
            question.choices.any { choice ->
                choice.id != question.countryIso &&
                    FlagSimilarity.score(question.countryIso, choice.id) != Int.MAX_VALUE
            }
        })
    }

    private fun country(index: Int, region: Region, fixedIso: String? = null): Country {
        val iso = fixedIso ?: ('A'.code + index / 26).toChar().toString() + ('A'.code + index % 26).toChar()
        return Country(
            iso2 = iso,
            countryKo = "국가$index",
            countryEn = "Country $index",
            countryAliases = listOf("국가$index"),
            capitalKo = "수도$index",
            capitalEn = "Capital $index",
            capitalAliases = listOf("수도$index"),
            capitalRaw = "수도$index(Capital $index)",
            flagResName = "flag_${iso.lowercase()}",
            region = region,
            difficulty = 3,
            population = index.toLong(),
            area = index.toDouble(),
            quizEnabled = true
        )
    }
}
