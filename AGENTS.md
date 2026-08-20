# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android application built with Kotlin and Jetpack Compose. Production code lives under `app/src/main/java/com/chlqudco/countryquiz/`:

- `data/` loads the bundled catalog and persists progress.
- `model/` defines quiz, country, session, and settings models.
- `quiz/` contains question generation and answer evaluation.
- `ui/` contains the ViewModel, Compose screens, components, and theme.
- `notification/` schedules local review reminders.

Country data is bundled in `app/src/main/assets/countries.json`. Flag files use `flag_<iso2>` names in `app/src/main/res/drawable-nodpi/`. JVM tests are in `app/src/test/`; device tests are in `app/src/androidTest/`. Data-generation tooling is in `tools/`.

## Build, Test, and Development Commands

Run commands from the repository root on Windows:

- `.\gradlew.bat :app:assembleDebug` builds the debug APK.
- `.\gradlew.bat :app:testDebugUnitTest` runs JVM unit tests.
- `.\gradlew.bat :app:lintDebug` runs Android Lint.
- `.\gradlew.bat :app:connectedDebugAndroidTest` runs instrumentation tests on a connected emulator or device.
- `.\tools\Sync-CountryData.ps1` refreshes `countries.json` and `FlagResources.kt` from the configured public API and bundled flags.

Before submitting a substantial change, run unit tests, Lint, and the debug build. Run connected tests when changing resources, persistence, or session encoding.

## Coding Style & Naming Conventions

Use four-space indentation and standard Kotlin formatting. Name classes, enums, and composables in `PascalCase`; functions and properties in `camelCase`; constants in `UPPER_SNAKE_CASE`. Keep Compose screens focused and move reusable UI into `ui/components/`. Preserve the existing unidirectional state flow through `QuizViewModel`.

Do not add code comments or KDoc unless the task explicitly requests them. Keep changes minimal and do not expand existing comments unnecessarily.

## Testing Guidelines

Tests use JUnit 4 and AndroidX Test. Name files `*Test.kt` or `*InstrumentedTest.kt`, and use behavior-oriented test names such as `typingAcceptsSpacingPunctuationCaseAndAliases`. Add regression tests for quiz generation, answer normalization, catalog integrity, and session serialization.

## Commit & Pull Request Guidelines

The history currently contains only `Initial commit`, so no formal convention is established. Use short, imperative subjects such as `Add review reminder scheduling`. Keep commits focused. Pull requests should explain user-visible behavior, list verification commands, link relevant issues, and include screenshots for Compose UI changes.

## Security & Configuration

Keep `DATA_GO_KR_SERVICE_KEY` in `local.properties`; never commit that file or expose the key in app sources. When flags change, rerun the sync script and verify that catalog ISO codes, drawable resources, and generated mappings remain identical.
