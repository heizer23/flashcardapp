package com.example.flashcardapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Flashcard::class, Topic::class, FlashcardTopicCrossRef::class, ReviewHistory::class],
    version = 13,
    exportSchema = false
)
abstract class FlashcardRoomDatabase : RoomDatabase() {

    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: FlashcardRoomDatabase? = null

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // --- flashcards ---
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS flashcards_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "question TEXT NOT NULL DEFAULT '', " +
                            "answer TEXT NOT NULL DEFAULT '', " +
                            "easinessFactor REAL NOT NULL DEFAULT 2.5, " +
                            "repetition INTEGER NOT NULL DEFAULT 0, " +
                            "interval INTEGER NOT NULL DEFAULT 1, " +
                            "nextReview INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), " +
                            "searchTerm TEXT NOT NULL DEFAULT '', " +
                            "userNote TEXT NOT NULL DEFAULT '' " +
                            ");"
                )
                database.execSQL(
                    "INSERT INTO flashcards_new (id, question, answer, easinessFactor, repetition, interval, nextReview, searchTerm, userNote) " +
                            "SELECT id, COALESCE(question, ''), COALESCE(answer, ''), " +
                            "COALESCE(easinessFactor, 2.5), COALESCE(repetition, 0), " +
                            "COALESCE(interval, 1), " +
                            "COALESCE(nextReview, strftime('%s','now')*1000), " +
                            "COALESCE(searchTerm, ''), COALESCE(userNote, '') " +
                            "FROM flashcards;"
                )
                database.execSQL("DROP TABLE flashcards;")
                database.execSQL("ALTER TABLE flashcards_new RENAME TO flashcards;")

                // --- review_history ---
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS review_history_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "question_id INTEGER NOT NULL DEFAULT 0, " +
                            "confidence_level INTEGER NOT NULL DEFAULT 0, " +
                            "timestamp INTEGER NOT NULL DEFAULT (strftime('%s','now')*1000), " +
                            "time_since_last_seen INTEGER NOT NULL DEFAULT 0, " +
                            "interval INTEGER NOT NULL DEFAULT 0, " +
                            "review_type TEXT NOT NULL DEFAULT '', " +
                            "answer_duration INTEGER NOT NULL DEFAULT 0 " +
                            ");"
                )
                database.execSQL(
                    "INSERT INTO review_history_new (id, question_id, confidence_level, timestamp, time_since_last_seen, interval, review_type, answer_duration) " +
                            "SELECT id, COALESCE(question_id, 0), COALESCE(confidence_level, 0), " +
                            "COALESCE(timestamp, strftime('%s','now')*1000), " +
                            "COALESCE(time_since_last_seen, 0), COALESCE(interval, 0), " +
                            "COALESCE(review_type, ''), COALESCE(answer_duration, 0) " +
                            "FROM review_history;"
                )
                database.execSQL("DROP TABLE review_history;")
                database.execSQL("ALTER TABLE review_history_new RENAME TO review_history;")

                // --- topics ---
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS topics_new (" +
                            "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL DEFAULT '', " +
                            "selected INTEGER NOT NULL DEFAULT 0 " +
                            ");"
                )
                database.execSQL(
                    "INSERT INTO topics_new (id, name, selected) " +
                            "SELECT id, COALESCE(name, ''), COALESCE(selected, 0) FROM topics;"
                )
                database.execSQL("DROP TABLE topics;")
                database.execSQL("ALTER TABLE topics_new RENAME TO topics;")

                // --- flashcard_topic_cross_ref (rename columns or annotate entity) ---
                // Option A: rename DB columns to match Kotlin property names
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS flashcard_topic_cross_ref_new (" +
                            "flashcardId INTEGER NOT NULL, " +
                            "topicId INTEGER NOT NULL, " +
                            "PRIMARY KEY(flashcardId, topicId) " +
                            ");"
                )
                database.execSQL(
                    "INSERT INTO flashcard_topic_cross_ref_new (flashcardId, topicId) " +
                            "SELECT COALESCE(flashcard_id, 0), COALESCE(topic_id, 0) FROM flashcard_topic_cross_ref;"
                )
                database.execSQL("DROP TABLE flashcard_topic_cross_ref;")
                database.execSQL("ALTER TABLE flashcard_topic_cross_ref_new RENAME TO flashcard_topic_cross_ref;")

                // OR Option B: keep 'flashcard_id'/'topic_id' columns and use @ColumnInfo in FlashcardTopicCrossRef.
            }
        }

        fun getDatabase(context: Context): FlashcardRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlashcardRoomDatabase::class.java,
                    "flashcards.db"
                )
                    .addMigrations(MIGRATION_12_13)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
