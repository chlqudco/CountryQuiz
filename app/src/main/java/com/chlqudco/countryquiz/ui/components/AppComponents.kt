package com.chlqudco.countryquiz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chlqudco.countryquiz.data.FlagResources
import com.chlqudco.countryquiz.ui.AppScreen

@Composable
fun FlagImage(
    iso2: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val resourceId = remember(iso2) {
        FlagResources.id(iso2)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .border(1.dp, Color(0x22000000), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            Image(
                painter = painterResource(resourceId),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(text = isoToEmoji(iso2), style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MasteryBar(
    value: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(CircleShape),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun AppBottomBar(
    selected: AppScreen,
    onSelect: (AppScreen) -> Unit
) {
    val items = listOf(
        Triple(AppScreen.HOME, "⌂", "홈"),
        Triple(AppScreen.MODES, "?", "퀴즈"),
        Triple(AppScreen.COUNTRY_BOOK, "◎", "국가 도감"),
        Triple(AppScreen.STATISTICS, "▥", "통계"),
        Triple(AppScreen.SETTINGS, "⚙", "설정")
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                selected = selected == screen,
                onClick = { onSelect(screen) },
                icon = { Text(text = icon, fontWeight = FontWeight.Bold) },
                label = { Text(text = label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

private fun isoToEmoji(iso2: String): String {
    return iso2.uppercase().mapNotNull { character ->
        if (character in 'A'..'Z') {
            String(Character.toChars(0x1F1E6 + character.code - 'A'.code))
        } else {
            null
        }
    }.joinToString("")
}
