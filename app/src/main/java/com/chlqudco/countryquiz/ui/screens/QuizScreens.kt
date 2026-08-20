package com.chlqudco.countryquiz.ui.screens

import android.view.SoundEffectConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.chlqudco.countryquiz.model.ChoiceType
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.QuizChoice
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.QuizQuestion
import com.chlqudco.countryquiz.model.QuizSession
import com.chlqudco.countryquiz.model.UserSettings
import com.chlqudco.countryquiz.ui.components.FlagImage
import com.chlqudco.countryquiz.ui.theme.Coral
import com.chlqudco.countryquiz.ui.theme.OceanGreen
import com.chlqudco.countryquiz.ui.theme.SunYellow
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    session: QuizSession,
    settings: UserSettings,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onExit: () -> Unit
) {
    val question = session.currentQuestion ?: return
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(session.selectedChoiceId, session.currentIndex) {
        if (session.config.gameMode == GameMode.TIME_ATTACK && session.selectedChoiceId != null) {
            delay(650)
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← 나가기",
                modifier = Modifier.clickable(onClick = onExit),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(text = session.config.gameMode.title, style = MaterialTheme.typography.titleMedium)
            SessionBadge(session)
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { (session.currentIndex + 1f) / session.questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${session.currentIndex + 1} / ${session.questions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${question.mode.shortTitle} · ${session.score}점",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(26.dp))
        Text(
            text = question.prompt,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        if (question.promptFlagIso != null) {
            Spacer(Modifier.height(20.dp))
            FlagImage(
                iso2 = question.promptFlagIso,
                contentDescription = if (session.isAnswered) "${question.countryName} 국기" else "국기 문제 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                cornerRadius = 18.dp
            )
        }
        Spacer(Modifier.height(24.dp))
        when (question.choiceType) {
            ChoiceType.FLAG -> FlagChoices(
                question = question,
                selectedChoiceId = session.selectedChoiceId,
                onChoice = { choice ->
                    if (settings.soundEnabled) view.playSoundEffect(SoundEffectConstants.CLICK)
                    if (settings.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAnswer(choice.id)
                }
            )
            ChoiceType.BOOLEAN -> BooleanChoices(
                question = question,
                selectedChoiceId = session.selectedChoiceId,
                onChoice = { choice ->
                    if (settings.soundEnabled) view.playSoundEffect(SoundEffectConstants.CLICK)
                    if (settings.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAnswer(choice.id)
                }
            )
            ChoiceType.TEXT -> TextChoices(
                question = question,
                selectedChoiceId = session.selectedChoiceId,
                onChoice = { choice ->
                    if (settings.soundEnabled) view.playSoundEffect(SoundEffectConstants.CLICK)
                    if (settings.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAnswer(choice.id)
                }
            )
            ChoiceType.INPUT -> InputAnswer(
                question = question,
                submittedAnswer = session.selectedChoiceId,
                onSubmit = { answer ->
                    if (settings.soundEnabled) view.playSoundEffect(SoundEffectConstants.CLICK)
                    if (settings.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAnswer(answer)
                }
            )
        }
        if (session.isAnswered) {
            Spacer(Modifier.height(18.dp))
            FeedbackCard(
                question = question,
                selectedAnswer = session.selectedChoiceId.orEmpty(),
                correct = session.answers.lastOrNull { it.questionId == question.id }?.correct == true
            )
            if (session.config.gameMode != GameMode.TIME_ATTACK) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Text(
                        text = if (session.currentIndex == session.questions.lastIndex || session.lives == 0) "결과 보기" else "다음 문제"
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun ResultScreen(
    session: QuizSession,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onReview: () -> Unit
) {
    val answered = session.answers.size
    val accuracy = if (answered == 0) 0 else (session.correctCount * 100 / answered)
    val message = when {
        accuracy >= 90 -> "세계 여행 전문가네요!"
        accuracy >= 70 -> "멋진 여행이었어요!"
        accuracy >= 50 -> "조금만 더 가면 숙련자예요"
        else -> "오답을 복습하면 금방 늘어요"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(SunYellow.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (accuracy >= 80) "🏆" else "🌍", style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(18.dp))
        Text(text = "퀴즈 완료", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "최종 점수", style = MaterialTheme.typography.bodyLarge)
                Text(text = session.score.toString(), style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ResultMetric(label = "정답", value = "${session.correctCount}/$answered", color = OceanGreen)
                    ResultMetric(label = "정답률", value = "$accuracy%", color = SunYellow)
                    ResultMetric(label = "최고 콤보", value = session.bestCombo.toString(), color = Coral)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        val wrongCount = answered - session.correctCount
        if (wrongCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReview),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "↻", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "틀린 $wrongCount 문제 복습하기", style = MaterialTheme.typography.titleMedium)
                        Text(text = "다른 형태로 다시 연결해 보세요", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = "→", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(17.dp)
        ) {
            Text(text = "같은 설정으로 다시 풀기")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(17.dp)
        ) {
            Text(text = "홈으로")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SessionBadge(session: QuizSession) {
    Surface(
        shape = RoundedCornerShape(50),
        color = when (session.config.gameMode) {
            GameMode.TIME_ATTACK -> SunYellow.copy(alpha = 0.22f)
            GameMode.SURVIVAL -> Coral.copy(alpha = 0.18f)
            GameMode.SIMILAR_FLAGS -> OceanGreen.copy(alpha = 0.16f)
            else -> MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = when (session.config.gameMode) {
                GameMode.TIME_ATTACK -> "${session.remainingSeconds}초"
                GameMode.SURVIVAL -> "♥ ${session.lives}"
                GameMode.SIMILAR_FLAGS -> "≈ ${session.combo}"
                else -> "연속 ${session.combo}"
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun TextChoices(
    question: QuizQuestion,
    selectedChoiceId: String?,
    onChoice: (QuizChoice) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.choices.forEachIndexed { index, choice ->
            ChoiceCard(
                choice = choice,
                index = index,
                question = question,
                selectedChoiceId = selectedChoiceId,
                onChoice = onChoice
            ) {
                Text(
                    text = choice.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun FlagChoices(
    question: QuizQuestion,
    selectedChoiceId: String?,
    onChoice: (QuizChoice) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.choices.chunked(2).forEach { rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowChoices.forEachIndexed { index, choice ->
                    ChoiceCard(
                        choice = choice,
                        index = question.choices.indexOf(choice),
                        question = question,
                        selectedChoiceId = selectedChoiceId,
                        modifier = Modifier
                            .weight(1f)
                            .height(126.dp),
                        onChoice = onChoice
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FlagImage(
                                iso2 = choice.flagIso.orEmpty(),
                                contentDescription = if (selectedChoiceId == null) {
                                    "국기 선택지 ${question.choices.indexOf(choice) + 1}"
                                } else {
                                    "${choice.label} 국기"
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp)
                            )
                            if (selectedChoiceId != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = choice.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                if (rowChoices.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BooleanChoices(
    question: QuizQuestion,
    selectedChoiceId: String?,
    onChoice: (QuizChoice) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        question.choices.forEachIndexed { index, choice ->
            ChoiceCard(
                choice = choice,
                index = index,
                question = question,
                selectedChoiceId = selectedChoiceId,
                modifier = Modifier
                    .weight(1f)
                    .height(116.dp),
                onChoice = onChoice
            ) {
                Text(
                    text = choice.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun InputAnswer(
    question: QuizQuestion,
    submittedAnswer: String?,
    onSubmit: (String) -> Unit
) {
    var answer by rememberSaveable(question.id) { mutableStateOf(submittedAnswer.orEmpty()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val submit = {
        val value = answer.trim()
        if (value.isNotEmpty() && submittedAnswer == null) {
            keyboardController?.hide()
            onSubmit(value)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = submittedAnswer == null,
            singleLine = true,
            label = { Text("정답 입력") },
            placeholder = { Text("한글 또는 영어") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            shape = RoundedCornerShape(18.dp)
        )
        Button(
            onClick = submit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = answer.isNotBlank() && submittedAnswer == null,
            shape = RoundedCornerShape(17.dp)
        ) {
            Text("정답 확인")
        }
    }
}

@Composable
private fun ChoiceCard(
    choice: QuizChoice,
    index: Int,
    question: QuizQuestion,
    selectedChoiceId: String?,
    modifier: Modifier = Modifier,
    onChoice: (QuizChoice) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val answered = selectedChoiceId != null
    val correct = choice.id == question.correctChoiceId
    val selected = choice.id == selectedChoiceId
    val containerColor = when {
        answered && correct -> Color(0xFFDDF4E8)
        answered && selected -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        answered && correct -> OceanGreen
        answered && selected -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !answered) { onChoice(choice) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (selected || correct && answered) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (question.choiceType == ChoiceType.TEXT) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = ('A'.code + index).toChar().toString(), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.size(12.dp))
            }
            content()
            if (answered && (correct || selected)) {
                Text(
                    text = if (correct) "✓" else "×",
                    color = if (correct) OceanGreen else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(question: QuizQuestion, selectedAnswer: String, correct: Boolean) {
    val expectedAnswer = when (question.mode) {
        QuizMode.FLAG_TO_COUNTRY,
        QuizMode.CAPITAL_TO_COUNTRY,
        QuizMode.COUNTRY_TO_FLAG,
        QuizMode.FLAG_COUNTRY_OX -> question.countryName
        else -> question.capitalName
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (correct) Color(0xFFDDF4E8) else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlagImage(
                iso2 = question.countryIso,
                contentDescription = "${question.countryName} 국기",
                modifier = Modifier.size(width = 70.dp, height = 48.dp),
                cornerRadius = 8.dp
            )
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (correct) "정답이에요!" else "정답을 확인해 보세요",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${question.countryName} · ${question.capitalName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (question.choiceType == ChoiceType.INPUT && !correct) {
                    Text(
                        text = "입력 $selectedAnswer · 정답 $expectedAnswer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
