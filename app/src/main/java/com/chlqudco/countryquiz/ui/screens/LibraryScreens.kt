package com.chlqudco.countryquiz.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chlqudco.countryquiz.model.Country
import com.chlqudco.countryquiz.model.CountryCatalog
import com.chlqudco.countryquiz.model.ProgressSnapshot
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.Region
import com.chlqudco.countryquiz.model.UserSettings
import com.chlqudco.countryquiz.ui.components.FlagImage
import com.chlqudco.countryquiz.ui.components.MasteryBar
import com.chlqudco.countryquiz.ui.components.SectionTitle
import com.chlqudco.countryquiz.ui.components.StatCard
import com.chlqudco.countryquiz.ui.theme.Coral
import com.chlqudco.countryquiz.ui.theme.OceanGreen
import com.chlqudco.countryquiz.ui.theme.SkyBlue
import com.chlqudco.countryquiz.ui.theme.SunYellow

@Composable
fun CountryBookScreen(
    catalog: CountryCatalog,
    progress: ProgressSnapshot,
    search: String,
    region: Region,
    onSearch: (String) -> Unit,
    onRegion: (Region) -> Unit,
    onFavorite: (String) -> Unit
) {
    val filteredCountries = catalog.countries.filter { country ->
        val matchesRegion = region == Region.ALL || country.region == region
        val query = search.trim().lowercase()
        val matchesSearch = query.isEmpty() ||
            country.countryKo.lowercase().contains(query) ||
            country.countryEn.lowercase().contains(query) ||
            country.capitalKo.lowercase().contains(query) ||
            country.countryAliases.any { it.lowercase().contains(query) }
        matchesRegion && matchesSearch
    }.sortedWith(
        compareByDescending<Country> { progress.favorites.contains(it.iso2) }
            .thenBy { it.countryKo }
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "국가 도감", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "국기와 수도를 한눈에 살펴보세요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = search,
                onValueChange = onSearch,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("국가 또는 수도 검색") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Text("⌕") }
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Region.entries) { item ->
                    FilterChip(
                        selected = region == item,
                        onClick = { onRegion(item) },
                        label = { Text(item.label) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${filteredCountries.size}개 국가·지역",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(filteredCountries, key = { it.iso2 }) { country ->
            CountryRow(
                country = country,
                mastery = progress.countryMastery(country.iso2),
                favorite = progress.favorites.contains(country.iso2),
                onFavorite = { onFavorite(country.iso2) }
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun StatisticsScreen(
    catalog: CountryCatalog,
    progress: ProgressSnapshot
) {
    val modeStats = QuizMode.entries.filter { it != QuizMode.MIXED }.mapNotNull { mode ->
        val records = progress.records.values.filter { it.mode == mode }
        val attempts = records.sumOf { it.attempts }
        if (attempts == 0) null else Triple(mode, attempts, records.sumOf { it.correctCount })
    }
    val countryByIso = catalog.countries.associateBy { it.iso2 }
    val weakCountries = progress.records.values
        .groupBy { it.countryIso }
        .mapNotNull { (iso, records) ->
            val attempts = records.sumOf { it.attempts }
            val correct = records.sumOf { it.correctCount }
            val wrong = records.sumOf { it.wrongCount }
            countryByIso[iso]?.let { country ->
                WeakCountry(country, attempts, correct, wrong)
            }
        }
        .filter { it.wrong > 0 }
        .sortedWith(compareByDescending<WeakCountry> { it.wrong }.thenBy { it.accuracy })
        .take(5)
    val mastered = catalog.countries.count { progress.countryMastery(it.iso2) >= 3 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(text = "학습 통계", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "정확도와 취약한 연결을 확인하세요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "전체 문제",
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
                    label = "숙련 국가",
                    value = mastered.toString(),
                    accent = SkyBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionTitle(title = "관계별 정답률")
                    if (modeStats.isEmpty()) {
                        EmptyStatText("퀴즈를 풀면 관계별 통계가 쌓여요")
                    } else {
                        modeStats.forEach { (mode, attempts, correct) ->
                            val accuracy = correct.toFloat() / attempts
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = mode.shortTitle, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${(accuracy * 100).toInt()}% · ${attempts}문제",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                MasteryBar(value = accuracy)
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionTitle(title = "자주 틀린 국가", subtitle = "오답 횟수가 많은 순서예요")
                    if (weakCountries.isEmpty()) {
                        EmptyStatText("아직 기록된 오답이 없어요")
                    } else {
                        weakCountries.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FlagImage(
                                    iso2 = item.country.iso2,
                                    contentDescription = "${item.country.countryKo} 국기",
                                    modifier = Modifier.size(width = 58.dp, height = 40.dp)
                                )
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.country.countryKo, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${item.country.capitalKo} · ${item.attempts}회 출제",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "오답 ${item.wrong}",
                                    color = Coral,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun SettingsScreen(
    catalog: CountryCatalog,
    settings: UserSettings,
    onSettings: (UserSettings) -> Unit,
    onReviewNotifications: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "설정", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "나에게 편한 학습 환경을 만들어요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            SettingsCard {
                SettingToggle(
                    title = "다크 모드",
                    subtitle = "어두운 화면으로 전환",
                    checked = settings.darkMode,
                    onChecked = { onSettings(settings.copy(darkMode = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingToggle(
                    title = "효과음",
                    subtitle = "정답 선택 시 소리 피드백",
                    checked = settings.soundEnabled,
                    onChecked = { onSettings(settings.copy(soundEnabled = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingToggle(
                    title = "진동",
                    subtitle = "선택 시 촉각 피드백",
                    checked = settings.hapticsEnabled,
                    onChecked = { onSettings(settings.copy(hapticsEnabled = it)) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingToggle(
                    title = "복습 알림",
                    subtitle = "매일 오전 9시에 복습할 문제가 있으면 알려드려요",
                    checked = settings.reviewNotificationsEnabled,
                    onChecked = onReviewNotifications
                )
            }
        }
        item {
            SectionTitle(title = "데이터 및 출처")
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                InfoRow(label = "수록 국가·지역", value = "${catalog.countries.size}개")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(label = "데이터 기준일", value = catalog.version)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(label = "실행 방식", value = "완전 오프라인")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(catalog.sourceUrl) }
                        .padding(vertical = 14.dp)
                ) {
                    Text(text = "국가·수도 정보 출처", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${catalog.source} ↗",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(text = "출처 안내", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "국가·수도 정보와 196개 국기는 외교부 공개데이터를 사용하며, 대한민국·북한·대만 정보와 국기는 학습 완성도를 위해 보완했습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "본 앱은 외교부가 제작하거나 공식 인증한 앱이 아닙니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun CountryRow(
    country: Country,
    mastery: Int,
    favorite: Boolean,
    onFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlagImage(
                iso2 = country.iso2,
                contentDescription = "${country.countryKo} 국기",
                modifier = Modifier.size(width = 78.dp, height = 54.dp)
            )
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = country.countryKo,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.size(7.dp))
                    Text(
                        text = country.iso2,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "수도 ${country.capitalKo} · ${country.region.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(7.dp))
                MasteryBar(value = mastery / 5f, modifier = Modifier.fillMaxWidth(0.75f))
            }
            Text(
                text = if (favorite) "★" else "☆",
                modifier = Modifier
                    .clickable(onClick = onFavorite)
                    .padding(8.dp),
                color = if (favorite) SunYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp), content = content)
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyStatText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(vertical = 18.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private data class WeakCountry(
    val country: Country,
    val attempts: Int,
    val correct: Int,
    val wrong: Int
) {
    val accuracy: Float
        get() = if (attempts == 0) 0f else correct.toFloat() / attempts
}
