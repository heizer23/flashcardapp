package com.example.flashcardapp.main

import android.content.Context
import android.util.Log
import com.example.flashcardapp.data.*

// Coroutines imports:
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlashcardDAO(context: Context) {

    private val roomDao: FlashcardDao

    init {
        val db = FlashcardRoomDatabase.getDatabase(context.applicationContext)
        roomDao = db.flashcardDao()
    }

    fun open() {
        // changes: delete
        // No operation. Room manages DB lifecycle.
    }

    fun close() {
        // changes: delete
        // No operation.
    }

    fun createFlashcard(flashcard: Flashcard): Flashcard {
        val newId = roomDao.insertFlashcard(flashcard)
        flashcard.id = newId.toInt()
        return flashcard
    }

    fun getFlashcard(id: Int): Flashcard? {
        return roomDao.getFlashcardById(id)
    }

    fun getNextDueFlashcard(currentTime: Long): Flashcard? {
        return roomDao.getNextDueFlashcard(currentTime)
    }

    fun updateFlashcard(flashcard: Flashcard) {
        roomDao.updateFlashcard(flashcard)
    }

    fun clearTopicsForFlashcard(flashcardId: Int) {
        Log.d("FlashcardDAO", "clearTopicsForFlashcard not implemented in new Room DAO.")
    }

    fun insertTopic(topicName: String): Topic {
        val existing = roomDao.getTopicByName(topicName)
        if (existing != null) {
            return existing
        }
        val topic = Topic(name = topicName)
        val newId = roomDao.insertTopic(topic)
        topic.id = newId.toInt()
        return topic
    }

    fun updateTopicSelection(topicId: Int, isSelected: Boolean) {
        roomDao.updateTopicSelection(topicId, isSelected)
    }

    fun associateFlashcardWithTopic(flashcardId: Int, topicId: Int) {
        roomDao.insertCrossRef(
            FlashcardTopicCrossRef(flashcardId, topicId)
        )
    }

    fun getTopicsForFlashcard(flashcardId: Int): List<Topic> {
        Log.d("FlashcardDAO", "getTopicsForFlashcard not implemented with Room relations.")
        return emptyList()
    }

    fun getAllTopics(): List<Topic> {
        return roomDao.getAllTopics()
    }

    fun getTopicByName(topicName: String): Topic? {
        return roomDao.getTopicByName(topicName)
    }

    fun deleteFlashcard(flashcardId: Int) {
        val flashcard = roomDao.getFlashcardById(flashcardId)
        if (flashcard != null) {
            roomDao.deleteFlashcard(flashcard)
        }
    }

    fun getFutureFlashcards(): List<Flashcard> {
        return roomDao.getFutureFlashcards(System.currentTimeMillis())
    }

    fun getPastFlashcards(): List<Flashcard> {
        return roomDao.getPastFlashcards(System.currentTimeMillis())
    }

    fun getAllFlashcards(): List<Flashcard> {
        return roomDao.getAllFlashcards()
    }

    fun getPastAndFutureQuestionsCount(): IntArray {
        val currentTime = System.currentTimeMillis()
        val pastCount = roomDao.getPastCount(currentTime)
        val futureCount = roomDao.getFutureCount(currentTime)
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
        // Not implemented in the new Room DAO. You'd add an entity for review_history.
        Log.d("FlashcardDAO", "insertReviewHistory not fully implemented.")
    }

    // -----------------------------------------------------------------
    // Below are the new coroutine-based async methods for DB operations
    // -----------------------------------------------------------------

    /**
     * Launches a coroutine on Dispatchers.IO to fetch the next due flashcard.
     * Result is returned on Dispatchers.Main.
     */
    fun getNextDueFlashcardAsync(currentTime: Long, onResult: (Flashcard?) -> Unit) { // changes: create
        CoroutineScope(Dispatchers.IO).launch {
            val result = getNextDueFlashcard(currentTime)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    /**
     * Launches a coroutine on Dispatchers.IO to update a flashcard.
     * Calls onComplete() on Dispatchers.Main when done.
     */
    fun updateFlashcardAsync(flashcard: Flashcard, onComplete: () -> Unit = {}) { // changes: create
        CoroutineScope(Dispatchers.IO).launch {
            updateFlashcard(flashcard)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Launches a coroutine on Dispatchers.IO to fetch the count of past and future questions.
     * Result is returned on Dispatchers.Main.
     */
    fun getPastAndFutureQuestionsCountAsync(onResult: (IntArray) -> Unit) { // changes: create
        CoroutineScope(Dispatchers.IO).launch {
            val counts = getPastAndFutureQuestionsCount()
            withContext(Dispatchers.Main) {
                onResult(counts)
            }
        }
    }
}
