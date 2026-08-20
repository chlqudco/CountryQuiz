package com.chlqudco.countryquiz.quiz

import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.QuizQuestion
import java.text.Normalizer
import java.util.Locale

object AnswerNormalizer {
    private val marks = Regex("\\p{M}+")
    private val separators = Regex("[^\\p{L}\\p{N}]")

    fun normalize(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFKD)
        .lowercase(Locale.ROOT)
        .replace(marks, "")
        .replace(separators, "")
}

object AnswerEvaluator {
    fun isCorrect(question: QuizQuestion, submittedAnswer: String): Boolean {
        if (question.choiceType != ChoiceType.INPUT) {
            return submittedAnswer == question.correctChoiceId
        }
        val normalized = AnswerNormalizer.normalize(submittedAnswer)
        return normalized.isNotEmpty() && question.acceptedAnswers.any {
            AnswerNormalizer.normalize(it) == normalized
        }
    }
}
