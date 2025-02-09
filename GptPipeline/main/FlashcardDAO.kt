package com.example.flashcardapp.main

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.Topic

class FlashcardDAO(context: Context) {

    private var database: SQLiteDatabase? = null
    private var dbHelper: FlashcardDatabaseHelper? = null

    private val flashcardColumns = arrayOf(
        FlashcardDatabaseHelper.COLUMN_ID,
        FlashcardDatabaseHelper.COLUMN_QUESTION,
        FlashcardDatabaseHelper.COLUMN_ANSWER,
        FlashcardDatabaseHelper.COLUMN_E_FACTOR,
        FlashcardDatabaseHelper.COLUMN_REPETITION,
        FlashcardDatabaseHelper.COLUMN_INTERVAL,
        FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW,
        FlashcardDatabaseHelper.COLUMN_SEARCH_TERM, // Add search term
        FlashcardDatabaseHelper.COLUMN_USER_NOTE     // Add user note
    )

    private val topicColumns = arrayOf(
        FlashcardDatabaseHelper.COLUMN_TOPIC_ID,
        FlashcardDatabaseHelper.COLUMN_TOPIC_NAME
    )

    init {
        dbHelper = FlashcardDatabaseHelper(context)
    }

    fun open() {
        database = dbHelper?.writableDatabase
    }

    fun close() {
        dbHelper?.close()
    }

    // Method to create a new flashcard
    fun createFlashcard(flashcard: Flashcard): Flashcard {
        val values = ContentValues().apply {
            put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.question)
            put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.answer)
            put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.easinessFactor)
            put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.repetition)
            put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.interval)
            put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.nextReview)
            put(FlashcardDatabaseHelper.COLUMN_SEARCH_TERM, flashcard.searchTerm)
            put(FlashcardDatabaseHelper.COLUMN_USER_NOTE, flashcard.userNote)
        }

        val insertId = database?.insert(FlashcardDatabaseHelper.TABLE_FLASHCARDS, null, values)
        if (insertId != null) {
            flashcard.id = insertId.toInt()
        }
        return flashcard
    }

    // Method to retrieve a flashcard by its ID
    fun getFlashcard(id: Int): Flashcard? {
        val cursor = database?.query(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            flashcardColumns,
            FlashcardDatabaseHelper.COLUMN_ID + " = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var flashcard: Flashcard? = null
        if (cursor != null && cursor.moveToFirst()) {
            flashcard = cursorToFlashcard(cursor)
            cursor.close()
        }
        return flashcard
    }

    fun getNextDueFlashcard(currentTime: Long): Flashcard? {
        var flashcard: Flashcard? = null
        val query = "SELECT * FROM view_filtered_flashcards " +
            " WHERE " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " <= ?" +
            " ORDER BY " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " DESC " +
            " LIMIT 1"

        val cursor = database?.rawQuery(query, arrayOf(currentTime.toString()))
        if (cursor != null && cursor.moveToFirst()) {
            flashcard = cursorToFlashcard(cursor)
        }
        cursor?.close()
        return flashcard
    }

    // Method to update an existing flashcard
    fun updateFlashcard(flashcard: Flashcard) {
        val values = ContentValues().apply {
            put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.question)
            put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.answer)
            put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.easinessFactor)
            put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.repetition)
            put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.interval)
            put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.nextReview)
            put(FlashcardDatabaseHelper.COLUMN_SEARCH_TERM, flashcard.searchTerm)
            put(FlashcardDatabaseHelper.COLUMN_USER_NOTE, flashcard.userNote)
        }

        database?.update(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            values,
            FlashcardDatabaseHelper.COLUMN_ID + " = ?",
            arrayOf(flashcard.id.toString())
        )
    }

    // Method to clear all topic associations for a flashcard
    fun clearTopicsForFlashcard(flashcardId: Int) {
        val rowsDeleted = database?.delete(
            FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF,
            FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID + " = ?",
            arrayOf(flashcardId.toString())
        )
        Log.d("Database", "Rows deleted: $rowsDeleted")
    }

    // Method to insert a topic
    fun insertTopic(topicName: String): Topic {
        val values = ContentValues().apply {
            put(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME, topicName)
        }

        val topicId = database?.insertWithOnConflict(
            FlashcardDatabaseHelper.TABLE_TOPICS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        ) ?: -1

        val topic = Topic(topicName)
        topic.id = topicId.toInt()
        return topic
    }

    fun updateTopicSelection(topicId: Int, isSelected: Boolean) {
        val values = ContentValues().apply {
            put(FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED, if (isSelected) 1 else 0)
        }
        database?.update(
            FlashcardDatabaseHelper.TABLE_TOPICS,
            values,
            FlashcardDatabaseHelper.COLUMN_TOPIC_ID + " = ?",
            arrayOf(topicId.toString())
        )
    }

    // Method to associate a flashcard with a topic
    fun associateFlashcardWithTopic(flashcardId: Int, topicId: Int) {
        val values = ContentValues().apply {
            put(FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID, flashcardId)
            put(FlashcardDatabaseHelper.COLUMN_TOPIC_ID_REF, topicId)
        }
        database?.insert(FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF, null, values)
    }

    // Get all topics for a flashcard
    fun getTopicsForFlashcard(flashcardId: Int): List<Topic> {
        val topics = mutableListOf<Topic>()
        val query = ("SELECT * FROM " + FlashcardDatabaseHelper.TABLE_TOPICS + " t" +
                " INNER JOIN " + FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF + " c" +
                " ON t." + FlashcardDatabaseHelper.COLUMN_TOPIC_ID + " = c." + FlashcardDatabaseHelper.COLUMN_TOPIC_ID_REF +
                " WHERE c." + FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID + " = ?")
        val cursor = database?.rawQuery(query, arrayOf(flashcardId.toString()))

        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    val topic = Topic()
                    topic.id = it.getInt(it.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_TOPIC_ID))
                    topic.name = it.getString(it.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME))
                    topics.add(topic)
                } while (it.moveToNext())
            }
            it.close()
        }
        return topics
    }

    // Method to retrieve all topics
    fun getAllTopics(): List<Topic> {
        val topics = mutableListOf<Topic>()
        val cursor = database?.query(
            FlashcardDatabaseHelper.TABLE_TOPICS,
            arrayOf(
                FlashcardDatabaseHelper.COLUMN_TOPIC_ID,
                FlashcardDatabaseHelper.COLUMN_TOPIC_NAME,
                FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED
            ),
            null,
            null,
            null,
            null,
            null
        )

        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    @SuppressLint("Range")
                    val topicId = it.getInt(it.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_ID))
                    @SuppressLint("Range")
                    val topicName = it.getString(it.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME))
                    @SuppressLint("Range")
                    val topicSelected = it.getInt(it.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED)) == 1
                    topics.add(Topic(topicId, topicName, topicSelected))
                } while (it.moveToNext())
            }
            it.close()
        }
        return topics
    }

    // Method to retrieve a topic by name
    fun getTopicByName(topicName: String): Topic? {
        var topic: Topic? = null
        val cursor = database?.query(
            FlashcardDatabaseHelper.TABLE_TOPICS,
            arrayOf(
                FlashcardDatabaseHelper.COLUMN_TOPIC_ID,
                FlashcardDatabaseHelper.COLUMN_TOPIC_NAME,
                FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED
            ),
            FlashcardDatabaseHelper.COLUMN_TOPIC_NAME + " = ?",
            arrayOf(topicName),
            null,
            null,
            null
        )

        cursor?.let {
            if (it.moveToFirst()) {
                @SuppressLint("Range")
                val topicId = it.getInt(it.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_ID))
                @SuppressLint("Range")
                val retrievedTopicName = it.getString(it.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME))
                @SuppressLint("Range")
                val topicSelected = it.getInt(it.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED)) == 1
                topic = Topic(topicId, retrievedTopicName, topicSelected)
            }
            it.close()
        }

        return topic
    }

    // Helper method to convert a cursor to a Flashcard object
    private fun cursorToFlashcard(cursor: Cursor): Flashcard {
        val flashcard = Flashcard()
        flashcard.id = cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_ID))
        flashcard.question = cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_QUESTION))
        flashcard.answer = cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_ANSWER))
        flashcard.easinessFactor = cursor.getDouble(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_E_FACTOR))
        flashcard.repetition = cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_REPETITION))
        flashcard.interval = cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_INTERVAL))
        flashcard.nextReview = cursor.getLong(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW))
        flashcard.searchTerm = cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_SEARCH_TERM))
        flashcard.userNote = cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_USER_NOTE))

        val topics = getTopicsForFlashcard(flashcard.id)
        flashcard.topics = topics
        return flashcard
    }

    fun deleteFlashcard(flashcardId: Int) {
        // Delete the flashcard from the flashcards table
        database?.delete(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            FlashcardDatabaseHelper.COLUMN_ID + " = ?",
            arrayOf(flashcardId.toString())
        )

        // Optionally, clear the associated topics if any
        database?.delete(
            FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF,
            FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID + " = ?",
            arrayOf(flashcardId.toString())
        )
    }

    // Get flashcards with next_review in the future (ascending)
    fun getFutureFlashcards(): List<Flashcard> {
        val flashcards = mutableListOf<Flashcard>()
        val query = "SELECT * FROM view_filtered_flashcards WHERE " +
            FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " > ?" +
            " ORDER BY " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " ASC"

        val cursor = database?.rawQuery(query, arrayOf(System.currentTimeMillis().toString()))
        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    flashcards.add(cursorToFlashcard(it))
                } while (it.moveToNext())
            }
            it.close()
        }
        return flashcards
    }

    fun getPastFlashcards(): List<Flashcard> {
        val flashcards = mutableListOf<Flashcard>()
        val query = "SELECT * FROM view_filtered_flashcards WHERE " +
            FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " <= ?" +
            " ORDER BY " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " DESC"

        val cursor = database?.rawQuery(query, arrayOf(System.currentTimeMillis().toString()))
        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    flashcards.add(cursorToFlashcard(it))
                } while (it.moveToNext())
            }
            it.close()
        }
        return flashcards
    }

    fun getAllFlashcards(): List<Flashcard> {
        val flashcards = mutableListOf<Flashcard>()
        val cursor = database?.query(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            flashcardColumns,
            null,
            null,
            null,
            null,
            FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " ASC"
        )
        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    val flashcard = cursorToFlashcard(it)
                    flashcards.add(flashcard)
                } while (it.moveToNext())
            }
            it.close()
        }
        return flashcards
    }

    fun getPastAndFutureQuestionsCount(): IntArray {
        val currentTime = System.currentTimeMillis()
        val query = "SELECT " +
                "SUM(CASE WHEN nextReview <= ? THEN 1 ELSE 0 END) AS pastCount, " +
                "SUM(CASE WHEN nextReview > ? THEN 1 ELSE 0 END) AS futureCount " +
                "FROM view_filtered_flashcards"

        val cursor = database?.rawQuery(query, arrayOf(currentTime.toString(), currentTime.toString()))
        cursor?.moveToFirst()
        @SuppressLint("Range")
        val pastCount = cursor?.getInt(cursor.getColumnIndex("pastCount")) ?: 0
        @SuppressLint("Range")
        val futureCount = cursor?.getInt(cursor.getColumnIndex("futureCount")) ?: 0
        cursor?.close()

        return intArrayOf(pastCount, futureCount)
    }

    fun insertReviewHistory(
        questionId: Int,
        confidenceLevel: Int,
        timestamp: Long,
        timeSinceLastSeen: Long,
        interval: Int,
        reviewType: String,
        answerDuration: Long
    ) {
        val values = ContentValues().apply {
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_QUESTION_ID, questionId)
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_CONFIDENCE_LEVEL, confidenceLevel)
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_TIMESTAMP, timestamp)
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_TIME_SINCE_LAST_SEEN, timeSinceLastSeen)
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_INTERVAL, interval)
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_REVIEW_TYPE, reviewType)
            put(FlashcardDatabaseHelper.COLUMN_HISTORY_ANSWER_DURATION, answerDuration)
        }
        database?.insert(FlashcardDatabaseHelper.TABLE_REVIEW_HISTORY, null, values)
    }
}
