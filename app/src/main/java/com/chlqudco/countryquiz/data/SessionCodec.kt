package com.chlqudco.countryquiz.data

import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.AnswerFormat
import com.chlqudco.countryquiz.model.Difficulty
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.QuizChoice
import com.chlqudco.countryquiz.model.QuizConfig
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.QuizQuestion
import com.chlqudco.countryquiz.model.QuizSession
import com.chlqudco.countryquiz.model.Region
import com.chlqudco.countryquiz.model.SessionAnswer
import org.json.JSONArray
import org.json.JSONObject

object SessionCodec {
    fun encode(session: QuizSession): String = JSONObject().apply {
        put("config", session.config.toJson())
        put("questions", JSONArray().apply { session.questions.forEach { put(it.toJson()) } })
        put("currentIndex", session.currentIndex)
        put("answers", JSONArray().apply { session.answers.forEach { put(it.toJson()) } })
        session.selectedChoiceId?.let { put("selectedChoiceId", it) }
        put("score", session.score)
        put("correctCount", session.correctCount)
        put("combo", session.combo)
        put("bestCombo", session.bestCombo)
        put("lives", session.lives)
        put("remainingSeconds", session.remainingSeconds)
        put("finished", session.finished)
    }.toString()

    fun decode(value: String): QuizSession = JSONObject(value).toSession()

    private fun QuizConfig.toJson(): JSONObject = JSONObject().apply {
        put("quizMode", quizMode.name)
        put("gameMode", gameMode.name)
        put("region", region.name)
        put("difficulty", difficulty.name)
        put("questionCount", questionCount)
        put("answerFormat", answerFormat.name)
    }

    private fun QuizQuestion.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("mode", mode.name)
        put("prompt", prompt)
        promptFlagIso?.let { put("promptFlagIso", it) }
        put("choiceType", choiceType.name)
        put("choices", JSONArray().apply { choices.forEach { put(it.toJson()) } })
        put("correctChoiceId", correctChoiceId)
        put("acceptedAnswers", JSONArray(acceptedAnswers))
        put("countryIso", countryIso)
        put("countryName", countryName)
        put("capitalName", capitalName)
        put("capitalDetails", capitalDetails)
    }

    private fun QuizChoice.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        flagIso?.let { put("flagIso", it) }
    }

    private fun SessionAnswer.toJson(): JSONObject = JSONObject().apply {
        put("questionId", questionId)
        put("selectedChoiceId", selectedChoiceId)
        put("correct", correct)
        put("answeredAt", answeredAt)
    }

    private fun JSONObject.toSession(): QuizSession {
        val questionValues = getJSONArray("questions")
        val questions = buildList {
            repeat(questionValues.length()) { add(questionValues.getJSONObject(it).toQuestion()) }
        }
        val answerValues = optJSONArray("answers") ?: JSONArray()
        val answers = buildList {
            repeat(answerValues.length()) { add(answerValues.getJSONObject(it).toAnswer()) }
        }
        return QuizSession(
            config = getJSONObject("config").toConfig(),
            questions = questions,
            currentIndex = optInt("currentIndex"),
            answers = answers,
            selectedChoiceId = optString("selectedChoiceId").takeIf { it.isNotBlank() },
            score = optInt("score"),
            correctCount = optInt("correctCount"),
            combo = optInt("combo"),
            bestCombo = optInt("bestCombo"),
            lives = optInt("lives", 3),
            remainingSeconds = optInt("remainingSeconds"),
            finished = optBoolean("finished")
        )
    }

    private fun JSONObject.toConfig(): QuizConfig = QuizConfig(
        quizMode = QuizMode.valueOf(getString("quizMode")),
        gameMode = GameMode.valueOf(getString("gameMode")),
        region = Region.valueOf(getString("region")),
        difficulty = Difficulty.valueOf(getString("difficulty")),
        questionCount = getInt("questionCount"),
        answerFormat = runCatching {
            AnswerFormat.valueOf(optString("answerFormat", AnswerFormat.MULTIPLE_CHOICE.name))
        }.getOrDefault(AnswerFormat.MULTIPLE_CHOICE)
    )

    private fun JSONObject.toQuestion(): QuizQuestion {
        val choiceValues = getJSONArray("choices")
        val choices = buildList {
            repeat(choiceValues.length()) { index ->
                val item = choiceValues.getJSONObject(index)
                add(
                    QuizChoice(
                        id = item.getString("id"),
                        label = item.getString("label"),
                        flagIso = item.optString("flagIso").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
        val answerValues = optJSONArray("acceptedAnswers") ?: JSONArray()
        val acceptedAnswers = buildList {
            repeat(answerValues.length()) { add(answerValues.getString(it)) }
        }
        return QuizQuestion(
            id = getString("id"),
            mode = QuizMode.valueOf(getString("mode")),
            prompt = getString("prompt"),
            promptFlagIso = optString("promptFlagIso").takeIf { it.isNotBlank() },
            choiceType = ChoiceType.valueOf(getString("choiceType")),
            choices = choices,
            correctChoiceId = getString("correctChoiceId"),
            acceptedAnswers = acceptedAnswers,
            countryIso = getString("countryIso"),
            countryName = getString("countryName"),
            capitalName = getString("capitalName"),
            capitalDetails = optString("capitalDetails")
        )
    }

    private fun JSONObject.toAnswer(): SessionAnswer = SessionAnswer(
        questionId = getString("questionId"),
        selectedChoiceId = getString("selectedChoiceId"),
        correct = getBoolean("correct"),
        answeredAt = getLong("answeredAt")
    )
}
