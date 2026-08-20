package com.chlqudco.countryquiz.model

enum class Region(val label: String) {
    ALL("전체"),
    ASIA("아시아"),
    EUROPE("유럽"),
    AFRICA("아프리카"),
    NORTH_AMERICA("북아메리카"),
    SOUTH_AMERICA("남아메리카"),
    OCEANIA("오세아니아");

    companion object {
        fun fromLabel(label: String): Region = entries.firstOrNull { it.label == label } ?: ALL
    }
}

enum class Difficulty(val label: String, val maxCountryDifficulty: Int) {
    EASY("초급", 1),
    NORMAL("중급", 2),
    HARD("고급", 3)
}

enum class QuizMode(val title: String, val shortTitle: String, val description: String) {
    FLAG_TO_COUNTRY("국기 보고 국가 맞히기", "국기 → 국가", "국기를 보고 올바른 국가명을 선택해요"),
    COUNTRY_TO_FLAG("국가 보고 국기 맞히기", "국가 → 국기", "국가명과 일치하는 국기를 찾아요"),
    COUNTRY_TO_CAPITAL("국가 보고 수도 맞히기", "국가 → 수도", "국가의 수도를 선택해요"),
    CAPITAL_TO_COUNTRY("수도 보고 국가 맞히기", "수도 → 국가", "수도가 속한 국가를 찾아요"),
    FLAG_TO_CAPITAL("국기 보고 수도 맞히기", "국기 → 수도", "국기를 보고 수도를 연결해요"),
    COUNTRY_CAPITAL_OX("국가·수도 OX", "국가·수도 OX", "국가와 수도의 연결이 맞는지 판단해요"),
    FLAG_COUNTRY_OX("국기·국가 OX", "국기·국가 OX", "국기와 국가명의 연결을 판단해요"),
    MIXED("혼합 모의고사", "혼합 퀴즈", "국기와 수도 문제를 골고루 풀어요");

    val supportsTyping: Boolean
        get() = this == FLAG_TO_COUNTRY ||
            this == COUNTRY_TO_CAPITAL ||
            this == CAPITAL_TO_COUNTRY ||
            this == FLAG_TO_CAPITAL ||
            this == MIXED
}

enum class GameMode(val title: String, val description: String) {
    STANDARD("기본 퀴즈", "문제 수를 정해 차분하게 풀어요"),
    TIME_ATTACK("60초 타임어택", "60초 동안 최대한 많이 맞혀요"),
    SURVIVAL("생존 모드", "목숨 3개로 끝까지 도전해요"),
    DAILY("오늘의 10문제", "매일 달라지는 혼합 문제를 풀어요"),
    REVIEW("오답 복습", "틀렸거나 복습할 문제를 다시 풀어요"),
    SIMILAR_FLAGS("닮은 국기 집중", "헷갈리기 쉬운 국기를 나란히 비교해요")
}

enum class AnswerFormat(val title: String, val description: String) {
    MULTIPLE_CHOICE("선택형", "보기 4개에서 정답을 골라요"),
    TYPING("주관식", "한글이나 영어로 직접 입력해요")
}

enum class ChoiceType {
    TEXT,
    FLAG,
    BOOLEAN,
    INPUT
}

data class Country(
    val iso2: String,
    val countryKo: String,
    val countryEn: String,
    val countryAliases: List<String>,
    val capitalKo: String,
    val capitalEn: String,
    val capitalAliases: List<String>,
    val capitalRaw: String,
    val flagResName: String,
    val region: Region,
    val difficulty: Int,
    val population: Long,
    val area: Double,
    val quizEnabled: Boolean
)

data class CountryCatalog(
    val version: String,
    val source: String,
    val sourceUrl: String,
    val countries: List<Country>
)

data class QuizChoice(
    val id: String,
    val label: String,
    val flagIso: String? = null
)

data class QuizQuestion(
    val id: String,
    val mode: QuizMode,
    val prompt: String,
    val promptFlagIso: String? = null,
    val choiceType: ChoiceType,
    val choices: List<QuizChoice>,
    val correctChoiceId: String,
    val acceptedAnswers: List<String> = emptyList(),
    val countryIso: String,
    val countryName: String,
    val capitalName: String,
    val capitalDetails: String
)

data class QuizConfig(
    val quizMode: QuizMode = QuizMode.MIXED,
    val gameMode: GameMode = GameMode.STANDARD,
    val region: Region = Region.ALL,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val questionCount: Int = 10,
    val answerFormat: AnswerFormat = AnswerFormat.MULTIPLE_CHOICE
)

data class ReviewTarget(
    val countryIso: String,
    val mode: QuizMode
)

data class MasteryRecord(
    val countryIso: String,
    val mode: QuizMode,
    val attempts: Int = 0,
    val correctCount: Int = 0,
    val masteryLevel: Int = 0,
    val wrongCount: Int = 0,
    val lastAskedAt: Long = 0,
    val nextReviewAt: Long = 0,
    val lastWasCorrect: Boolean = false,
    val correctStreak: Int = 0,
    val reviewIntervalDays: Int = 0
) {
    val accuracy: Float
        get() = if (attempts == 0) 0f else correctCount.toFloat() / attempts
}

data class UserSettings(
    val darkMode: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reviewNotificationsEnabled: Boolean = false
)

data class ProgressSnapshot(
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val completedSessions: Int = 0,
    val bestCombo: Int = 0,
    val lastPlayedAt: Long = 0,
    val dailyCompletedDate: String = "",
    val records: Map<String, MasteryRecord> = emptyMap(),
    val favorites: Set<String> = emptySet(),
    val settings: UserSettings = UserSettings()
) {
    val accuracy: Float
        get() = if (totalAnswered == 0) 0f else totalCorrect.toFloat() / totalAnswered

    fun recordKey(countryIso: String, mode: QuizMode): String = "$countryIso|${mode.name}"

    fun record(countryIso: String, mode: QuizMode): MasteryRecord? = records[recordKey(countryIso, mode)]

    fun countryMastery(countryIso: String): Int {
        val countryRecords = records.values.filter { it.countryIso == countryIso && it.attempts > 0 }
        return if (countryRecords.isEmpty()) 0 else countryRecords.map { it.masteryLevel }.average().toInt()
    }
}

data class SessionAnswer(
    val questionId: String,
    val selectedChoiceId: String,
    val correct: Boolean,
    val answeredAt: Long
)

data class QuizSession(
    val config: QuizConfig,
    val questions: List<QuizQuestion>,
    val currentIndex: Int = 0,
    val answers: List<SessionAnswer> = emptyList(),
    val selectedChoiceId: String? = null,
    val score: Int = 0,
    val correctCount: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val lives: Int = 3,
    val remainingSeconds: Int = if (config.gameMode == GameMode.TIME_ATTACK) 60 else 0,
    val finished: Boolean = false
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val isAnswered: Boolean
        get() = selectedChoiceId != null
}
