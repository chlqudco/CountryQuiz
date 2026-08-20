package com.chlqudco.countryquiz.quiz

import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.QuizQuestion
import com.chlqudco.countryquiz.model.QuizMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerEvaluatorTest {
    @Test
    fun typingAcceptsSpacingPunctuationCaseAndAliases() {
        val question = question(
            acceptedAnswers = listOf("워싱턴 D.C.", "Washington, D.C.", "São Tomé")
        )

        assertTrue(AnswerEvaluator.isCorrect(question, "워싱턴DC"))
        assertTrue(AnswerEvaluator.isCorrect(question, " washington dc "))
        assertTrue(AnswerEvaluator.isCorrect(question, "sao tome"))
    }

    @Test
    fun typingRejectsBlankPartialAndDifferentAnswers() {
        val question = question(acceptedAnswers = listOf("워싱턴 D.C.", "Washington, D.C."))

        assertFalse(AnswerEvaluator.isCorrect(question, ""))
        assertFalse(AnswerEvaluator.isCorrect(question, "워싱턴"))
        assertFalse(AnswerEvaluator.isCorrect(question, "뉴욕"))
    }

    @Test
    fun choiceQuestionsStillCompareChoiceIdentifiers() {
        val question = question(acceptedAnswers = emptyList()).copy(
            choiceType = ChoiceType.TEXT,
            correctChoiceId = "US"
        )

        assertTrue(AnswerEvaluator.isCorrect(question, "US"))
        assertFalse(AnswerEvaluator.isCorrect(question, "미국"))
    }

    private fun question(acceptedAnswers: List<String>): QuizQuestion = QuizQuestion(
        id = "typing-US",
        mode = QuizMode.COUNTRY_TO_CAPITAL,
        prompt = "미국의 수도는?",
        choiceType = ChoiceType.INPUT,
        choices = emptyList(),
        correctChoiceId = "US",
        acceptedAnswers = acceptedAnswers,
        countryIso = "US",
        countryName = "미국",
        capitalName = "워싱턴 D.C.",
        capitalDetails = "워싱턴 D.C.(Washington, D.C.)"
    )
}
