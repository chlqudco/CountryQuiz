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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chlqudco.countryquiz.model.Difficulty
import com.chlqudco.countryquiz.model.AnswerFormat
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.QuizConfig
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.Region
import com.chlqudco.countryquiz.ui.components.SectionTitle
import com.chlqudco.countryquiz.ui.theme.Coral
import com.chlqudco.countryquiz.ui.theme.OceanGreen
import com.chlqudco.countryquiz.ui.theme.SkyBlue
import com.chlqudco.countryquiz.ui.theme.SunYellow

@Composable
fun ModesScreen(
    onModeSelected: (QuizMode) -> Unit,
    onGameModeSelected: (GameMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(text = "퀴즈 여행", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "오늘 집중할 연결을 골라보세요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            SectionTitle(title = "게임 모드", subtitle = "같은 문제도 다른 긴장감으로")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GameCard(
                    symbol = "◷",
                    title = "60초",
                    subtitle = "타임어택",
                    color = SunYellow,
                    modifier = Modifier.weight(1f),
                    onClick = { onGameModeSelected(GameMode.TIME_ATTACK) }
                )
                GameCard(
                    symbol = "♥",
                    title = "목숨 3개",
                    subtitle = "생존 모드",
                    color = Coral,
                    modifier = Modifier.weight(1f),
                    onClick = { onGameModeSelected(GameMode.SURVIVAL) }
                )
                GameCard(
                    symbol = "✦",
                    title = "골고루",
                    subtitle = "혼합 시험",
                    color = SkyBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(QuizMode.MIXED) }
                )
            }
        }
        item {
            GameCard(
                symbol = "≈",
                title = "닮은 국기 집중",
                subtitle = "비슷한 색과 구성을 비교하며 익혀요",
                color = OceanGreen,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onGameModeSelected(GameMode.SIMILAR_FLAGS) }
            )
        }
        item {
            Spacer(Modifier.height(6.dp))
            SectionTitle(title = "관계별 퀴즈", subtitle = "틀린 관계는 따로 기억하고 복습해요")
        }
        items(QuizMode.entries.filter { it != QuizMode.MIXED }) { mode ->
            ModeRow(mode = mode, onClick = { onModeSelected(mode) })
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun SetupScreen(
    config: QuizConfig,
    onBack: () -> Unit,
    onRegion: (Region) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onQuestionCount: (Int) -> Unit,
    onAnswerFormat: (AnswerFormat) -> Unit,
    onStart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Text(
                text = "← 돌아가기",
                modifier = Modifier.clickable(onClick = onBack),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = if (config.gameMode == GameMode.SIMILAR_FLAGS) config.gameMode.title else config.quizMode.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (config.gameMode == GameMode.SIMILAR_FLAGS) {
                    config.gameMode.description
                } else {
                    config.quizMode.description
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SettingBlock(title = "지역", subtitle = "출제할 대륙을 선택하세요") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Region.entries) { region ->
                        FilterChip(
                            selected = config.region == region,
                            onClick = { onRegion(region) },
                            label = { Text(region.label) }
                        )
                    }
                }
            }
        }

        item {
            SettingBlock(title = "난이도", subtitle = "초급은 친숙한 국가부터 시작해요") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { difficulty ->
                        FilterChip(
                            selected = config.difficulty == difficulty,
                            onClick = { onDifficulty(difficulty) },
                            label = { Text(difficulty.label) }
                        )
                    }
                }
            }
        }

        if (config.quizMode.supportsTyping) {
            item {
                SettingBlock(title = "정답 방식", subtitle = "주관식은 한글과 영어 정답을 모두 인식해요") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnswerFormat.entries.forEach { answerFormat ->
                            FilterChip(
                                selected = config.answerFormat == answerFormat,
                                onClick = { onAnswerFormat(answerFormat) },
                                label = { Text(answerFormat.title) }
                            )
                        }
                    }
                    Text(
                        text = config.answerFormat.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (config.gameMode == GameMode.STANDARD || config.gameMode == GameMode.SIMILAR_FLAGS) {
            item {
                SettingBlock(title = "문제 수", subtitle = "한 세션의 길이를 정하세요") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 20).forEach { count ->
                            FilterChip(
                                selected = config.questionCount == count,
                                onClick = { onQuestionCount(count) },
                                label = { Text("${count}문제") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = config.gameMode.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = config.gameMode.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = "${displayQuestionCount(config)}", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(text = "퀴즈 시작", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ModeRow(mode: QuizMode, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = modeSymbol(mode), style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mode.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = "→", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameCard(
    symbol: String,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(144.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = symbol, fontWeight = FontWeight.Bold)
            }
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

@Composable
private fun SettingBlock(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

private fun modeSymbol(mode: QuizMode): String = when (mode) {
    QuizMode.FLAG_TO_COUNTRY -> "⚑"
    QuizMode.COUNTRY_TO_FLAG -> "▤"
    QuizMode.COUNTRY_TO_CAPITAL -> "⌖"
    QuizMode.CAPITAL_TO_COUNTRY -> "◎"
    QuizMode.FLAG_TO_CAPITAL -> "⚑⌖"
    QuizMode.COUNTRY_CAPITAL_OX -> "OX"
    QuizMode.FLAG_COUNTRY_OX -> "⚑O"
    QuizMode.MIXED -> "✦"
}

private fun displayQuestionCount(config: QuizConfig): String = when (config.gameMode) {
    GameMode.TIME_ATTACK -> "60초"
    GameMode.SURVIVAL -> "최대 40문제"
    GameMode.DAILY -> "10문제"
    GameMode.REVIEW -> "${config.questionCount}문제"
    GameMode.STANDARD,
    GameMode.SIMILAR_FLAGS -> "${config.questionCount}문제"
}
