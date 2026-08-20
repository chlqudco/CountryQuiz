package com.chlqudco.countryquiz.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.chlqudco.countryquiz.model.GameMode
import com.chlqudco.countryquiz.model.QuizMode
import com.chlqudco.countryquiz.ui.components.AppBottomBar
import com.chlqudco.countryquiz.ui.screens.CountryBookScreen
import com.chlqudco.countryquiz.ui.screens.HomeScreen
import com.chlqudco.countryquiz.ui.screens.ModesScreen
import com.chlqudco.countryquiz.ui.screens.QuizScreen
import com.chlqudco.countryquiz.ui.screens.ResultScreen
import com.chlqudco.countryquiz.ui.screens.SettingsScreen
import com.chlqudco.countryquiz.ui.screens.SetupScreen
import com.chlqudco.countryquiz.ui.screens.StatisticsScreen
import kotlinx.coroutines.delay

@Composable
fun CountryQuizApp(
    state: AppUiState,
    viewModel: QuizViewModel
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setReviewNotifications(granted)
    }
    val rootScreens = setOf(
        AppScreen.HOME,
        AppScreen.MODES,
        AppScreen.COUNTRY_BOOK,
        AppScreen.STATISTICS,
        AppScreen.SETTINGS
    )

    BackHandler(enabled = state.screen != AppScreen.HOME) {
        when (state.screen) {
            AppScreen.QUIZ -> viewModel.navigate(AppScreen.HOME)
            AppScreen.RESULT -> viewModel.closeSession()
            AppScreen.SETUP -> viewModel.navigate(AppScreen.MODES)
            else -> viewModel.navigate(AppScreen.HOME)
        }
    }

    LaunchedEffect(state.screen, state.session?.remainingSeconds) {
        if (
            state.screen == AppScreen.QUIZ &&
            state.session?.config?.gameMode == GameMode.TIME_ATTACK &&
            state.session.finished.not()
        ) {
            delay(1_000)
            viewModel.tickTimer()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (state.screen in rootScreens) {
                AppBottomBar(selected = state.screen, onSelect = viewModel::navigate)
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state.screen) {
                AppScreen.HOME -> HomeScreen(
                    progress = state.progress,
                    session = state.session,
                    dueReviewCount = viewModel.dueReviewCount(),
                    countryCount = state.catalog.countries.size,
                    onDaily = viewModel::startDailyQuiz,
                    onResume = viewModel::resumeQuiz,
                    onQuickQuiz = viewModel::startQuickQuiz,
                    onGameMode = { viewModel.openSetup(QuizMode.MIXED, it) },
                    onReview = viewModel::startReview,
                    onAllModes = { viewModel.navigate(AppScreen.MODES) }
                )
                AppScreen.MODES -> ModesScreen(
                    onModeSelected = viewModel::openSetup,
                    onGameModeSelected = { viewModel.openSetup(QuizMode.MIXED, it) }
                )
                AppScreen.SETUP -> SetupScreen(
                    config = state.setupConfig,
                    onBack = { viewModel.navigate(AppScreen.MODES) },
                    onRegion = viewModel::setRegion,
                    onDifficulty = viewModel::setDifficulty,
                    onQuestionCount = viewModel::setQuestionCount,
                    onAnswerFormat = viewModel::setAnswerFormat,
                    onStart = viewModel::startConfiguredQuiz
                )
                AppScreen.QUIZ -> state.session?.let { session ->
                    QuizScreen(
                        session = session,
                        settings = state.progress.settings,
                        onAnswer = viewModel::answer,
                        onNext = viewModel::nextQuestion,
                        onExit = { viewModel.navigate(AppScreen.HOME) }
                    )
                }
                AppScreen.RESULT -> state.session?.let { session ->
                    ResultScreen(
                        session = session,
                        onRetry = viewModel::retryQuiz,
                        onHome = viewModel::closeSession,
                        onReview = viewModel::startReview
                    )
                }
                AppScreen.COUNTRY_BOOK -> CountryBookScreen(
                    catalog = state.catalog,
                    progress = state.progress,
                    search = state.countrySearch,
                    region = state.countryRegion,
                    onSearch = viewModel::setCountrySearch,
                    onRegion = viewModel::setCountryRegion,
                    onFavorite = viewModel::toggleFavorite
                )
                AppScreen.STATISTICS -> StatisticsScreen(
                    catalog = state.catalog,
                    progress = state.progress
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    catalog = state.catalog,
                    settings = state.progress.settings,
                    onSettings = viewModel::updateSettings,
                    onReviewNotifications = { enabled ->
                        if (
                            enabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setReviewNotifications(enabled)
                        }
                    }
                )
            }
        }
    }
}
