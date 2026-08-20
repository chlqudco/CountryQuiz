package com.chlqudco.countryquiz.data

import android.content.Context
import androidx.core.content.edit
import com.chlqudco.countryquiz.model.MasteryRecord
import com.chlqudco.countryquiz.model.ProgressSnapshot
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ProgressStore(context: Context) {
    private val preferences = context.getSharedPreferences("country_quiz_progress", Context.MODE_PRIVATE)

    fun load(): ProgressSnapshot {
        val raw = preferences.getString(KEY_PROGRESS, null) ?: return ProgressSnapshot()
        return runCatching { JSONObject(raw).toSnapshot() }.getOrDefault(ProgressSnapshot())
    }

    fun recordAnswer(
        snapshot: ProgressSnapshot,
        countryIso: String,
        mode: QuizMode,
        correct: Boolean,
        now: Long = System.currentTimeMillis()
    ): ProgressSnapshot {
        val key = snapshot.recordKey(countryIso, mode)
        val previous = snapshot.records[key] ?: MasteryRecord(countryIso = countryIso, mode = mode)
        val nextLevel = if (correct) {
            (previous.masteryLevel + 1).coerceAtMost(5)
        } else {
            (previous.masteryLevel - 1).coerceAtLeast(0)
        }
        val nextStreak = if (correct) previous.correctStreak + 1 else 0
        val intervalDays = if (correct) {
            REVIEW_INTERVAL_DAYS[(nextStreak - 1).coerceIn(0, REVIEW_INTERVAL_DAYS.lastIndex)]
        } else {
            0
        }
        val updatedRecord = previous.copy(
            attempts = previous.attempts + 1,
            correctCount = previous.correctCount + if (correct) 1 else 0,
            masteryLevel = nextLevel,
            wrongCount = previous.wrongCount + if (correct) 0 else 1,
            lastAskedAt = now,
            nextReviewAt = now + TimeUnit.DAYS.toMillis(intervalDays.toLong()),
            lastWasCorrect = correct,
            correctStreak = nextStreak,
            reviewIntervalDays = intervalDays
        )
        return snapshot.copy(
            totalAnswered = snapshot.totalAnswered + 1,
            totalCorrect = snapshot.totalCorrect + if (correct) 1 else 0,
            lastPlayedAt = now,
            records = snapshot.records + (key to updatedRecord)
        ).also(::save)
    }

    fun recordSession(snapshot: ProgressSnapshot, bestCombo: Int, dailyDate: String?): ProgressSnapshot {
        return snapshot.copy(
            completedSessions = snapshot.completedSessions + 1,
            bestCombo = maxOf(snapshot.bestCombo, bestCombo),
            dailyCompletedDate = dailyDate ?: snapshot.dailyCompletedDate,
            lastPlayedAt = System.currentTimeMillis()
        ).also(::save)
    }

    fun toggleFavorite(snapshot: ProgressSnapshot, countryIso: String): ProgressSnapshot {
        val favorites = snapshot.favorites.toMutableSet().apply {
            if (!add(countryIso)) remove(countryIso)
        }
        return snapshot.copy(favorites = favorites).also(::save)
    }

    fun updateSettings(snapshot: ProgressSnapshot, settings: UserSettings): ProgressSnapshot {
        return snapshot.copy(settings = settings).also(::save)
    }

    fun dueReviewCount(snapshot: ProgressSnapshot, now: Long = System.currentTimeMillis()): Int {
        return snapshot.records.values.count { it.wrongCount > 0 && it.nextReviewAt <= now }
    }

    fun loadActiveSession(): String? = preferences.getString(KEY_SESSION, null)

    fun saveActiveSession(value: String) {
        preferences.edit { putString(KEY_SESSION, value) }
    }

    fun clearActiveSession() {
        preferences.edit { remove(KEY_SESSION) }
    }

    private fun save(snapshot: ProgressSnapshot) {
        preferences.edit { putString(KEY_PROGRESS, snapshot.toJson().toString()) }
    }

    private fun ProgressSnapshot.toJson(): JSONObject = JSONObject().apply {
        put("totalAnswered", totalAnswered)
        put("totalCorrect", totalCorrect)
        put("completedSessions", completedSessions)
        put("bestCombo", bestCombo)
        put("lastPlayedAt", lastPlayedAt)
        put("dailyCompletedDate", dailyCompletedDate)
        put("favorites", JSONArray(favorites.toList()))
        put("settings", JSONObject().apply {
            put("darkMode", settings.darkMode)
            put("soundEnabled", settings.soundEnabled)
            put("hapticsEnabled", settings.hapticsEnabled)
            put("reviewNotificationsEnabled", settings.reviewNotificationsEnabled)
        })
        put("records", JSONArray().apply {
            records.values.forEach { record ->
                put(JSONObject().apply {
                    put("countryIso", record.countryIso)
                    put("mode", record.mode.name)
                    put("attempts", record.attempts)
                    put("correctCount", record.correctCount)
                    put("masteryLevel", record.masteryLevel)
                    put("wrongCount", record.wrongCount)
                    put("lastAskedAt", record.lastAskedAt)
                    put("nextReviewAt", record.nextReviewAt)
                    put("lastWasCorrect", record.lastWasCorrect)
                    put("correctStreak", record.correctStreak)
                    put("reviewIntervalDays", record.reviewIntervalDays)
                })
            }
        })
    }

    private fun JSONObject.toSnapshot(): ProgressSnapshot {
        val recordMap = buildMap {
            val records = optJSONArray("records") ?: JSONArray()
            repeat(records.length()) { index ->
                val item = records.getJSONObject(index)
                val mode = runCatching { QuizMode.valueOf(item.getString("mode")) }.getOrNull() ?: return@repeat
                val record = MasteryRecord(
                    countryIso = item.getString("countryIso"),
                    mode = mode,
                    attempts = item.optInt("attempts"),
                    correctCount = item.optInt("correctCount"),
                    masteryLevel = item.optInt("masteryLevel"),
                    wrongCount = item.optInt("wrongCount"),
                    lastAskedAt = item.optLong("lastAskedAt"),
                    nextReviewAt = item.optLong("nextReviewAt"),
                    lastWasCorrect = item.optBoolean("lastWasCorrect"),
                    correctStreak = item.optInt("correctStreak"),
                    reviewIntervalDays = item.optInt("reviewIntervalDays")
                )
                put("${record.countryIso}|${record.mode.name}", record)
            }
        }
        val favoriteValues = optJSONArray("favorites") ?: JSONArray()
        val favorites = buildSet {
            repeat(favoriteValues.length()) { add(favoriteValues.getString(it)) }
        }
        val settingsJson = optJSONObject("settings") ?: JSONObject()
        return ProgressSnapshot(
            totalAnswered = optInt("totalAnswered"),
            totalCorrect = optInt("totalCorrect"),
            completedSessions = optInt("completedSessions"),
            bestCombo = optInt("bestCombo"),
            lastPlayedAt = optLong("lastPlayedAt"),
            dailyCompletedDate = optString("dailyCompletedDate"),
            records = recordMap,
            favorites = favorites,
            settings = UserSettings(
                darkMode = settingsJson.optBoolean("darkMode"),
                soundEnabled = settingsJson.optBoolean("soundEnabled", true),
                hapticsEnabled = settingsJson.optBoolean("hapticsEnabled", true),
                reviewNotificationsEnabled = settingsJson.optBoolean("reviewNotificationsEnabled")
            )
        )
    }

    private companion object {
        const val KEY_PROGRESS = "progress"
        const val KEY_SESSION = "active_session"
        val REVIEW_INTERVAL_DAYS = intArrayOf(1, 3, 7, 14, 30, 60)
    }
}
