package com.chlqudco.countryquiz.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.chlqudco.countryquiz.data.CountryRepository
import com.chlqudco.countryquiz.data.ProgressStore
import com.chlqudco.countryquiz.data.SessionCodec
import com.chlqudco.countryquiz.model.AnswerFormat
import com.chlqudco.countryquiz.model.CountryCatalog
import com.chlqudco.countryquiz.model.Difficulty
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.ProgressSnapshot
import com.chlqudco.countryquiz.model.QuizConfig
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.model.QuizSession
import com.chlqudco.countryquiz.model.Region
import com.chlqudco.countryquiz.model.ReviewTarget
import com.chlqudco.countryquiz.model.SessionAnswer
import com.chlqudco.countryquiz.model.UserSettings
import com.chlqudco.countryquiz.notification.ReviewReminderScheduler
import com.chlqudco.countryquiz.quiz.AnswerEvaluator
import com.chlqudco.countryquiz.quiz.QuizEngine
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppScreen {
    HOME,
    MODES,
    SETUP,
    QUIZ,
    RESULT,
    COUNTRY_BOOK,
    STATISTICS,
    SETTINGS
}

data class AppUiState(
    val catalog: CountryCatalog,
    val progress: ProgressSnapshot,
    val screen: AppScreen = AppScreen.HOME,
    val setupConfig: QuizConfig = QuizConfig(),
    val session: QuizSession? = null,
    val countrySearch: String = "",
    val countryRegion: Region = Region.ALL
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CountryRepository(application)
    private val progressStore = ProgressStore(application)
    private val quizEngine = QuizEngine()
    private val catalog = repository.loadCatalog()
    private val restoredSession = progressStore.loadActiveSession()?.let {
        runCatching { SessionCodec.decode(it) }.getOrNull()
    }
    private val _uiState = MutableStateFlow(
        AppUiState(
            catalog = catalog,
            progress = progressStore.load(),
            screen = when {
                restoredSession?.finished == true -> AppScreen.RESULT
                restoredSession != null -> AppScreen.QUIZ
                else -> AppScreen.HOME
            },
            session = restoredSession
        )
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.progress.settings.reviewNotificationsEnabled) {
            ReviewReminderScheduler.schedule(application)
        } else {
            ReviewReminderScheduler.cancel(application)
        }
    }

    fun navigate(screen: AppScreen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun openSetup(quizMode: QuizMode, gameMode: GameMode = GameMode.STANDARD) {
        _uiState.update {
            it.copy(
                screen = AppScreen.SETUP,
                setupConfig = QuizConfig(
                    quizMode = quizMode,
                    gameMode = gameMode,
                    questionCount = when (gameMode) {
                        GameMode.DAILY -> 10
                        GameMode.SURVIVAL -> 20
                        GameMode.TIME_ATTACK -> 60
                        GameMode.SIMILAR_FLAGS -> 10
                        else -> 10
                    }
                )
            )
        }
    }

    fun setRegion(region: Region) {
        _uiState.update { it.copy(setupConfig = it.setupConfig.copy(region = region)) }
    }

    fun setDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(setupConfig = it.setupConfig.copy(difficulty = difficulty)) }
    }

    fun setQuestionCount(count: Int) {
        _uiState.update { it.copy(setupConfig = it.setupConfig.copy(questionCount = count)) }
    }

    fun setAnswerFormat(answerFormat: AnswerFormat) {
        _uiState.update { it.copy(setupConfig = it.setupConfig.copy(answerFormat = answerFormat)) }
    }

    fun startConfiguredQuiz() {
        startQuiz(_uiState.value.setupConfig)
    }

    fun startQuickQuiz(mode: QuizMode) {
        startQuiz(QuizConfig(quizMode = mode, difficulty = Difficulty.NORMAL, questionCount = 10))
    }

    fun startDailyQuiz() {
        startQuiz(
            QuizConfig(
                quizMode = QuizMode.MIXED,
                gameMode = GameMode.DAILY,
                difficulty = Difficulty.NORMAL,
                questionCount = 10
            ),
            seed = LocalDate.now().toEpochDay()
        )
    }

    fun startReview() {
        val state = _uiState.value
        val targets = reviewTargets(state.progress)
        if (targets.isEmpty()) return
        val config = QuizConfig(
            quizMode = QuizMode.MIXED,
            gameMode = GameMode.REVIEW,
            difficulty = Difficulty.HARD,
            questionCount = minOf(20, targets.size)
        )
        startQuiz(config, targets = targets)
    }

    fun resumeQuiz() {
        val session = _uiState.value.session ?: return
        _uiState.update { it.copy(screen = if (session.finished) AppScreen.RESULT else AppScreen.QUIZ) }
    }

    fun answer(choiceId: String) {
        val state = _uiState.value
        val session = state.session ?: return
        val question = session.currentQuestion ?: return
        if (session.finished || session.isAnswered) return
        val correct = AnswerEvaluator.isCorrect(question, choiceId)
        val nextCombo = if (correct) session.combo + 1 else 0
        val nextLives = if (!correct && session.config.gameMode == GameMode.SURVIVAL) {
            (session.lives - 1).coerceAtLeast(0)
        } else {
            session.lives
        }
        val nextScore = session.score + if (correct) 100 + (nextCombo.coerceAtMost(5) * 10) else 0
        val answer = SessionAnswer(
            questionId = question.id,
            selectedChoiceId = choiceId,
            correct = correct,
            answeredAt = System.currentTimeMillis()
        )
        val nextProgress = progressStore.recordAnswer(
            snapshot = state.progress,
            countryIso = question.countryIso,
            mode = question.mode,
            correct = correct
        )
        val nextSession = session.copy(
            answers = session.answers + answer,
            selectedChoiceId = choiceId,
            score = nextScore,
            correctCount = session.correctCount + if (correct) 1 else 0,
            combo = nextCombo,
            bestCombo = maxOf(session.bestCombo, nextCombo),
            lives = nextLives
        )
        progressStore.saveActiveSession(SessionCodec.encode(nextSession))
        _uiState.update { it.copy(progress = nextProgress, session = nextSession) }
    }

    fun nextQuestion() {
        val session = _uiState.value.session ?: return
        if (!session.isAnswered || session.finished) return
        val shouldFinish = session.currentIndex >= session.questions.lastIndex ||
            (session.config.gameMode == GameMode.SURVIVAL && session.lives <= 0)
        if (shouldFinish) {
            finishSession(session)
            return
        }
        val nextSession = session.copy(
            currentIndex = session.currentIndex + 1,
            selectedChoiceId = null
        )
        progressStore.saveActiveSession(SessionCodec.encode(nextSession))
        _uiState.update { it.copy(session = nextSession) }
    }

    fun tickTimer() {
        val state = _uiState.value
        val session = state.session ?: return
        if (state.screen != AppScreen.QUIZ || session.finished || session.config.gameMode != GameMode.TIME_ATTACK) return
        if (session.remainingSeconds <= 1) {
            finishSession(session.copy(remainingSeconds = 0))
            return
        }
        val nextSession = session.copy(remainingSeconds = session.remainingSeconds - 1)
        progressStore.saveActiveSession(SessionCodec.encode(nextSession))
        _uiState.update { it.copy(session = nextSession) }
    }

    fun retryQuiz() {
        val config = _uiState.value.session?.config ?: return
        if (config.gameMode == GameMode.REVIEW) startReview() else startQuiz(config)
    }

    fun closeSession() {
        progressStore.clearActiveSession()
        _uiState.update { it.copy(screen = AppScreen.HOME, session = null) }
    }

    fun setCountrySearch(value: String) {
        _uiState.update { it.copy(countrySearch = value) }
    }

    fun setCountryRegion(region: Region) {
        _uiState.update { it.copy(countryRegion = region) }
    }

    fun toggleFavorite(countryIso: String) {
        _uiState.update { state ->
            state.copy(progress = progressStore.toggleFavorite(state.progress, countryIso))
        }
    }

    fun updateSettings(settings: UserSettings) {
        _uiState.update { state ->
            state.copy(progress = progressStore.updateSettings(state.progress, settings))
        }
    }

    fun setReviewNotifications(enabled: Boolean) {
        _uiState.update { state ->
            val settings = state.progress.settings.copy(reviewNotificationsEnabled = enabled)
            state.copy(progress = progressStore.updateSettings(state.progress, settings))
        }
        if (enabled) {
            ReviewReminderScheduler.schedule(getApplication())
        } else {
            ReviewReminderScheduler.cancel(getApplication())
        }
    }

    fun dueReviewCount(now: Long = System.currentTimeMillis()): Int =
        progressStore.dueReviewCount(_uiState.value.progress, now)

    private fun startQuiz(
        config: QuizConfig,
        targets: List<ReviewTarget> = emptyList(),
        seed: Long = System.nanoTime()
    ) {
        val questions = quizEngine.generateQuestions(
            countries = catalog.countries,
            config = config,
            reviewTargets = targets,
            seed = seed
        )
        if (questions.isEmpty()) return
        val session = QuizSession(config = config, questions = questions)
        progressStore.saveActiveSession(SessionCodec.encode(session))
        _uiState.update {
            it.copy(
                screen = AppScreen.QUIZ,
                setupConfig = config,
                session = session
            )
        }
    }

    private fun finishSession(session: QuizSession) {
        val state = _uiState.value
        val finished = session.copy(finished = true)
        val dailyDate = if (session.config.gameMode == GameMode.DAILY) LocalDate.now().toString() else null
        val nextProgress = progressStore.recordSession(state.progress, session.bestCombo, dailyDate)
        progressStore.saveActiveSession(SessionCodec.encode(finished))
        _uiState.update {
            it.copy(
                screen = AppScreen.RESULT,
                progress = nextProgress,
                session = finished
            )
        }
    }

    private fun reviewTargets(progress: ProgressSnapshot): List<ReviewTarget> {
        val now = System.currentTimeMillis()
        return progress.records.values
            .filter { it.wrongCount > 0 }
            .sortedWith(
                compareBy<com.chlqudco.countryquiz.model.MasteryRecord> { it.lastWasCorrect && it.nextReviewAt > now }
                    .thenBy { it.accuracy }
                    .thenByDescending { it.wrongCount }
            )
            .map { ReviewTarget(it.countryIso, it.mode) }
    }
}
