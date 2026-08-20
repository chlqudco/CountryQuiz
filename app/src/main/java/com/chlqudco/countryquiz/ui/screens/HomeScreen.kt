package com.chlqudco.countryquiz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.ProgressSnapshot
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.QuizSession
import com.chlqudco.countryquiz.ui.components.SectionTitle
import com.chlqudco.countryquiz.ui.components.StatCard
import com.chlqudco.countryquiz.ui.theme.Coral
import com.chlqudco.countryquiz.ui.theme.DeepNavy
import com.chlqudco.countryquiz.ui.theme.OceanGreen
import com.chlqudco.countryquiz.ui.theme.SunYellow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    progress: ProgressSnapshot,
    session: QuizSession?,
    dueReviewCount: Int,
    countryCount: Int,
    onDaily: () -> Unit,
    onResume: () -> Unit,
    onQuickQuiz: (QuizMode) -> Unit,
    onGameMode: (GameMode) -> Unit,
    onReview: () -> Unit,
    onAllModes: () -> Unit
) {
    val today = LocalDate.now()
    val dailyDone = progress.dailyCompletedDate == today.toString()
    val dateText = today.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "오늘은 어디로 떠날까요?", style = MaterialTheme.typography.headlineMedium)
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🌍", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        if (session != null && !session.finished) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onResume),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "이어 풀기", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${session.config.quizMode.shortTitle} · ${session.currentIndex + 1}/${session.questions.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(text = "계속 →", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(OceanGreen, DeepNavy)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (dailyDone) "오늘 학습 완료" else "DAILY JOURNEY",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "오늘의 세계국기\n10문제",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (dailyDone) "완료했어요. 다시 풀어 기록을 높여보세요." else "국기와 수도를 섞은 오늘만의 문제예요.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Spacer(Modifier.height(22.dp))
                        Button(
                            onClick = onDaily,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = DeepNavy
                            )
                        ) {
                            Text(text = if (dailyDone) "다시 도전하기" else "오늘의 퀴즈 시작")
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(title = "나의 학습", subtitle = "${countryCount}개 국가를 차근차근 정복해요")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "푼 문제",
                    value = progress.totalAnswered.toString(),
                    accent = OceanGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "정답률",
                    value = "${(progress.accuracy * 100).toInt()}%",
                    accent = SunYellow,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "최고 콤보",
                    value = progress.bestCombo.toString(),
                    accent = Coral,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionTitle(
                title = "빠른 시작",
                subtitle = "가장 많이 쓰는 퀴즈",
                trailing = {
                    Text(
                        text = "전체 보기",
                        modifier = Modifier.clickable(onClick = onAllModes),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickQuizCard(
                    symbol = "⚑",
                    title = "국기 퀴즈",
                    subtitle = "국기 → 국가",
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickQuiz(QuizMode.FLAG_TO_COUNTRY) }
                )
                QuickQuizCard(
                    symbol = "⌖",
                    title = "수도 퀴즈",
                    subtitle = "국가 → 수도",
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickQuiz(QuizMode.COUNTRY_TO_CAPITAL) }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickQuizCard(
                    symbol = "◷",
                    title = "타임어택",
                    subtitle = "60초 도전",
                    modifier = Modifier.weight(1f),
                    onClick = { onGameMode(GameMode.TIME_ATTACK) }
                )
                QuickQuizCard(
                    symbol = "♥",
                    title = "생존 모드",
                    subtitle = "목숨 3개",
                    modifier = Modifier.weight(1f),
                    onClick = { onGameMode(GameMode.SURVIVAL) }
                )
            }
            Spacer(Modifier.height(12.dp))
            QuickQuizCard(
                symbol = "≈",
                title = "닮은 국기 집중",
                subtitle = "헷갈리는 국기를 비교하며 학습",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onGameMode(GameMode.SIMILAR_FLAGS) }
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = dueReviewCount > 0, onClick = onReview),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (dueReviewCount > 0) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "↻", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "오늘의 복습", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (dueReviewCount > 0) "${dueReviewCount}개의 관계가 복습을 기다려요" else "아직 복습할 오답이 없어요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = if (dueReviewCount > 0) "시작 →" else "완료", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuickQuizCard(
    symbol: String,
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(136.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = symbol, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
