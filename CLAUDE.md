# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.example.flashcardapp.YourTestClass"

# Clean build
./gradlew clean
```

## Project Overview

Android flashcard app (Kotlin + Java) using the MVVM architecture with:
- **View layer**: Activities in `main/` package
- **ViewModel layer**: ViewModels in `viewmodel/` package
- **Data layer**: Room database, DAO, Repository in `data/` package

## Architecture

**Data flow**: Activities → ViewModels → Repository → Room DAO → SQLite

**Key components:**
- `FlashcardRoomDatabase` (version 16) — singleton Room database, has migration chain from v12→16
- `FlashcardRepository` — single repository for all DB operations; always use `Dispatchers.IO` via `withContext`
- `FlashcardDao` — Room DAO with topic-aware queries (selected topics filtering)
- `ChatGPTHelper` — singleton object calling OpenAI `gpt-4-turbo` via OkHttp; API key loaded from `assets/config.properties`

**Spaced repetition**: `ReviewFlashcardsActivity` + `ReviewFlashcardsViewModel` implement the review loop. Flashcards have `level`, `interval`, `repetition`, `nextReview` fields. `TimeUtils.java` handles interval formatting.

**Topics**: Many-to-many between `Flashcard` and `Topic` via `FlashcardTopicCrossRef`. Many queries have a "for selected topics" variant that filters by `topics.selected = 1`.

## Android SDK Versions

- `compileSdk 36`
- `targetSdk 33` ← intentionally below current; raising to 35+ requires reviewing permission and behavior changes
- `minSdk 24`

## Database Migrations

When changing the Room schema, increment `version` in `@Database` and add a `MIGRATION_N_(N+1)` object in `FlashcardRoomDatabase`, then register it in the `.addMigrations(...)` call. Never use `fallbackToDestructiveMigration()`.

## API Key

The OpenAI API key is read at runtime from `app/src/main/assets/config.properties` (key: `API_KEY`). This file is not committed. Create it locally before building features that use `ChatGPTHelper`.
