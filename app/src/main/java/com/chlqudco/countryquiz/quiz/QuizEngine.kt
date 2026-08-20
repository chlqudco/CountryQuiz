package com.chlqudco.countryquiz.quiz

import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.AnswerFormat
import com.chlqudco.countryquiz.model.Country
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.QuizChoice
import com.chlqudco.countryquiz.model.QuizConfig
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.QuizQuestion
import com.chlqudco.countryquiz.model.Region
import com.chlqudco.countryquiz.model.ReviewTarget
import kotlin.random.Random

class QuizEngine {
    fun generateQuestions(
        countries: List<Country>,
        config: QuizConfig,
        reviewTargets: List<ReviewTarget> = emptyList(),
        seed: Long = System.nanoTime()
    ): List<QuizQuestion> {
        val random = Random(seed)
        val enabled = countries.filter { it.quizEnabled }
        val modeEligible = if (config.gameMode == GameMode.SIMILAR_FLAGS) {
            enabled.filter { it.iso2 in FlagSimilarity.countryCodes }
        } else {
            enabled
        }
        val filtered = modeEligible.filter {
            (config.region == Region.ALL || it.region == config.region) &&
                it.difficulty <= config.difficulty.maxCountryDifficulty
        }.ifEnoughOr(modeEligible.filter { config.region == Region.ALL || it.region == config.region })
            .ifEnoughOr(modeEligible)
        val count = when (config.gameMode) {
            GameMode.TIME_ATTACK -> 60
            GameMode.SURVIVAL -> 40
            GameMode.DAILY -> 10
            GameMode.REVIEW -> minOf(config.questionCount, reviewTargets.size.coerceAtLeast(1))
            GameMode.STANDARD,
            GameMode.SIMILAR_FLAGS -> config.questionCount
        }
        val concreteModes = when {
            config.gameMode == GameMode.SIMILAR_FLAGS && config.answerFormat == AnswerFormat.TYPING -> {
                listOf(QuizMode.FLAG_TO_COUNTRY)
            }
            config.gameMode == GameMode.SIMILAR_FLAGS -> {
                listOf(QuizMode.FLAG_TO_COUNTRY, QuizMode.COUNTRY_TO_FLAG)
            }
            config.answerFormat == AnswerFormat.TYPING -> QuizMode.entries.filter {
                it != QuizMode.MIXED && it.supportsTyping
            }
            else -> QuizMode.entries.filter { it != QuizMode.MIXED }
        }
        val used = mutableSetOf<String>()
        val questions = mutableListOf<QuizQuestion>()
        val shuffledTargets = reviewTargets.shuffled(random)

        repeat(count) { index ->
            val target = if (config.gameMode == GameMode.REVIEW && shuffledTargets.isNotEmpty()) {
                shuffledTargets[index % shuffledTargets.size]
            } else {
                null
            }
            val mode = target?.mode?.takeIf { it != QuizMode.MIXED }
                ?: if (config.quizMode == QuizMode.MIXED) concreteModes[index % concreteModes.size] else config.quizMode
            val preferredCountry = target?.let { wanted -> filtered.firstOrNull { it.iso2 == wanted.countryIso } }
            val candidates = filtered.shuffled(random)
            val country = preferredCountry
                ?: candidates.firstOrNull { used.add("${it.iso2}|${mode.name}") }
                ?: candidates[index % candidates.size]
            used.add("${country.iso2}|${mode.name}")
            questions += createQuestion(
                country = country,
                mode = mode,
                pool = filtered,
                random = random,
                idPrefix = "$seed-$index",
                answerFormat = config.answerFormat
            )
        }
        return questions
    }

    private fun createQuestion(
        country: Country,
        mode: QuizMode,
        pool: List<Country>,
        random: Random,
        idPrefix: String,
        answerFormat: AnswerFormat
    ): QuizQuestion {
        return when (mode) {
            QuizMode.FLAG_TO_COUNTRY -> connectionQuestion(
                country = country,
                mode = mode,
                prompt = "이 국기는 어느 나라일까요?",
                promptFlagIso = country.iso2,
                choiceType = ChoiceType.TEXT,
                pool = pool,
                random = random,
                idPrefix = idPrefix,
                label = { it.countryKo },
                acceptedAnswers = { it.countryAnswers() },
                answerFormat = answerFormat
            )
            QuizMode.COUNTRY_TO_FLAG -> connectionQuestion(
                country = country,
                mode = mode,
                prompt = "${country.countryKo}의 국기를 찾아보세요",
                promptFlagIso = null,
                choiceType = ChoiceType.FLAG,
                pool = pool,
                random = random,
                idPrefix = idPrefix,
                label = { it.countryKo },
                acceptedAnswers = { it.countryAnswers() },
                answerFormat = answerFormat
            )
            QuizMode.COUNTRY_TO_CAPITAL -> connectionQuestion(
                country = country,
                mode = mode,
                prompt = "${country.countryKo}의 수도는 어디일까요?",
                promptFlagIso = null,
                choiceType = ChoiceType.TEXT,
                pool = pool,
                random = random,
                idPrefix = idPrefix,
                label = { it.capitalKo },
                acceptedAnswers = { it.capitalAnswers() },
                answerFormat = answerFormat
            )
            QuizMode.CAPITAL_TO_COUNTRY -> connectionQuestion(
                country = country,
                mode = mode,
                prompt = "${country.capitalKo}가 수도인 나라는?",
                promptFlagIso = null,
                choiceType = ChoiceType.TEXT,
                pool = pool,
                random = random,
                idPrefix = idPrefix,
                label = { it.countryKo },
                acceptedAnswers = { it.countryAnswers() },
                answerFormat = answerFormat
            )
            QuizMode.FLAG_TO_CAPITAL -> connectionQuestion(
                country = country,
                mode = mode,
                prompt = "이 나라의 수도는 어디일까요?",
                promptFlagIso = country.iso2,
                choiceType = ChoiceType.TEXT,
                pool = pool,
                random = random,
                idPrefix = idPrefix,
                label = { it.capitalKo },
                acceptedAnswers = { it.capitalAnswers() },
                answerFormat = answerFormat
            )
            QuizMode.COUNTRY_CAPITAL_OX -> countryCapitalOx(country, pool, random, idPrefix)
            QuizMode.FLAG_COUNTRY_OX -> flagCountryOx(country, pool, random, idPrefix)
            QuizMode.MIXED -> createQuestion(
                country,
                QuizMode.FLAG_TO_COUNTRY,
                pool,
                random,
                idPrefix,
                answerFormat
            )
        }
    }

    private fun connectionQuestion(
        country: Country,
        mode: QuizMode,
        prompt: String,
        promptFlagIso: String?,
        choiceType: ChoiceType,
        pool: List<Country>,
        random: Random,
        idPrefix: String,
        label: (Country) -> String,
        acceptedAnswers: (Country) -> List<String>,
        answerFormat: AnswerFormat
    ): QuizQuestion {
        val effectiveChoiceType = if (answerFormat == AnswerFormat.TYPING && mode.supportsTyping) {
            ChoiceType.INPUT
        } else {
            choiceType
        }
        val preferSimilarFlags = mode == QuizMode.FLAG_TO_COUNTRY ||
            mode == QuizMode.COUNTRY_TO_FLAG ||
            mode == QuizMode.FLAG_TO_CAPITAL
        val choices = if (effectiveChoiceType == ChoiceType.INPUT) {
            emptyList()
        } else {
            val distractors = distractors(country, pool, label, random, preferSimilarFlags)
            (distractors + country).map {
                QuizChoice(
                    id = it.iso2,
                    label = label(it),
                    flagIso = if (effectiveChoiceType == ChoiceType.FLAG) it.iso2 else null
                )
            }.shuffled(random)
        }
        return QuizQuestion(
            id = "$idPrefix-${mode.name}-${country.iso2}",
            mode = mode,
            prompt = prompt,
            promptFlagIso = promptFlagIso,
            choiceType = effectiveChoiceType,
            choices = choices,
            correctChoiceId = country.iso2,
            acceptedAnswers = if (effectiveChoiceType == ChoiceType.INPUT) {
                acceptedAnswers(country).filter { it.isNotBlank() }.distinct()
            } else {
                emptyList()
            },
            countryIso = country.iso2,
            countryName = country.countryKo,
            capitalName = country.capitalKo,
            capitalDetails = country.capitalRaw
        )
    }

    private fun countryCapitalOx(
        country: Country,
        pool: List<Country>,
        random: Random,
        idPrefix: String
    ): QuizQuestion {
        val correctStatement = random.nextBoolean()
        val shownCapital = if (correctStatement) {
            country.capitalKo
        } else {
            distractors(country, pool, { it.capitalKo }, random).first().capitalKo
        }
        return oxQuestion(
            country = country,
            mode = QuizMode.COUNTRY_CAPITAL_OX,
            prompt = "${country.countryKo}의 수도는 $shownCapital 이다",
            promptFlagIso = null,
            correctStatement = correctStatement,
            idPrefix = idPrefix
        )
    }

    private fun flagCountryOx(
        country: Country,
        pool: List<Country>,
        random: Random,
        idPrefix: String
    ): QuizQuestion {
        val correctStatement = random.nextBoolean()
        val shownCountry = if (correctStatement) {
            country.countryKo
        } else {
            distractors(country, pool, { it.countryKo }, random, preferSimilarFlags = true).first().countryKo
        }
        return oxQuestion(
            country = country,
            mode = QuizMode.FLAG_COUNTRY_OX,
            prompt = "이 국기는 $shownCountry 의 국기이다",
            promptFlagIso = country.iso2,
            correctStatement = correctStatement,
            idPrefix = idPrefix
        )
    }

    private fun oxQuestion(
        country: Country,
        mode: QuizMode,
        prompt: String,
        promptFlagIso: String?,
        correctStatement: Boolean,
        idPrefix: String
    ): QuizQuestion = QuizQuestion(
        id = "$idPrefix-${mode.name}-${country.iso2}",
        mode = mode,
        prompt = prompt,
        promptFlagIso = promptFlagIso,
        choiceType = ChoiceType.BOOLEAN,
        choices = listOf(
            QuizChoice(id = "true", label = "O"),
            QuizChoice(id = "false", label = "X")
        ),
        correctChoiceId = correctStatement.toString(),
        countryIso = country.iso2,
        countryName = country.countryKo,
        capitalName = country.capitalKo,
        capitalDetails = country.capitalRaw
    )

    private fun distractors(
        answer: Country,
        pool: List<Country>,
        label: (Country) -> String,
        random: Random,
        preferSimilarFlags: Boolean = false
    ): List<Country> {
        val ordered = pool.filter { it.iso2 != answer.iso2 }
            .sortedBy { candidate ->
                distractorRank(answer, candidate, preferSimilarFlags)
            }
            .groupBy { candidate ->
                distractorRank(answer, candidate, preferSimilarFlags)
            }
            .toSortedMap()
            .values
            .flatMap { it.shuffled(random) }
        val seen = mutableSetOf(label(answer))
        return ordered.filter { seen.add(label(it)) }.take(3)
    }

    private fun distractorRank(answer: Country, candidate: Country, preferSimilarFlags: Boolean): Int {
        if (preferSimilarFlags && FlagSimilarity.score(answer.iso2, candidate.iso2) != Int.MAX_VALUE) {
            return 0
        }
        return when {
            candidate.region == answer.region && candidate.difficulty == answer.difficulty -> 1
            candidate.region == answer.region -> 2
            candidate.difficulty == answer.difficulty -> 3
            else -> 4
        }
    }

    private fun Country.countryAnswers(): List<String> = countryAliases + countryKo + countryEn

    private fun Country.capitalAnswers(): List<String> = capitalAliases + capitalKo + capitalEn

    private fun List<Country>.ifEnoughOr(fallback: List<Country>): List<Country> = if (size >= 4) this else fallback
}
