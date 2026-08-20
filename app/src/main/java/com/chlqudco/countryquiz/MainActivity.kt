package com.chlqudco.countryquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chlqudco.countryquiz.ui.CountryQuizApp
import com.chlqudco.countryquiz.ui.QuizViewModel
import com.chlqudco.countryquiz.ui.theme.CountryQuizTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            CountryQuizTheme(darkTheme = state.progress.settings.darkMode) {
                CountryQuizApp(state = state, viewModel = viewModel)
            }
        }
    }
}
