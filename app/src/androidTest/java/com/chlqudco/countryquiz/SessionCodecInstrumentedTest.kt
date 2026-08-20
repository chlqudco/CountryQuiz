package com.chlqudco.countryquiz

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chlqudco.countryquiz.data.SessionCodec
import com.chlqudco.countryquiz.model.AnswerFormat
import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.QuizConfig
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.QuizQuestion
import com.chlqudco.countryquiz.model.QuizSession
import com.chlqudco.countryquiz.model.SessionAnswer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionCodecInstrumentedTest {
    @Test
    fun typingSessionRoundTripKeepsAnswerFormatAliasesAndSubmission() {
        val question = QuizQuestion(
            id = "typing-US",
            mode = QuizMode.FLAG_TO_COUNTRY,
            prompt = "이 국기는 어느 나라일까요?",
            promptFlagIso = "US",
            choiceType = ChoiceType.INPUT,
            choices = emptyList(),
            correctChoiceId = "US",
            acceptedAnswers = listOf("미국", "USA", "United States"),
            countryIso = "US",
            countryName = "미합중국",
            capitalName = "워싱턴 D.C.",
            capitalDetails = "워싱턴 D.C.(Washington, D.C.)"
        )
        val session = QuizSession(
            config = QuizConfig(
                quizMode = QuizMode.FLAG_TO_COUNTRY,
                answerFormat = AnswerFormat.TYPING
            ),
            questions = listOf(question),
            answers = listOf(
                SessionAnswer(
                    questionId = question.id,
                    selectedChoiceId = "UsA",
                    correct = true,
                    answeredAt = 1234L
                )
            ),
            selectedChoiceId = "UsA",
            score = 110,
            correctCount = 1
        )

        val restored = SessionCodec.decode(SessionCodec.encode(session))

        assertEquals(AnswerFormat.TYPING, restored.config.answerFormat)
        assertEquals(question.acceptedAnswers, restored.currentQuestion?.acceptedAnswers)
        assertEquals("UsA", restored.selectedChoiceId)
        assertEquals(true, restored.answers.single().correct)
        assertEquals(110, restored.score)
    }
}
