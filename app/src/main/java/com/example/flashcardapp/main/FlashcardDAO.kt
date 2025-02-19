package com.example.flashcardapp.main

import android.content.Context
import android.util.Log
import com.example.flashcardapp.data.*
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

    // Now we implement clearing old cross-references.
    fun clearTopicsForFlashcard(flashcardId: Int) { // changes: update
        Log.d("FlashcardDAO", "Clearing topics for flashcardId=$flashcardId")
        // Here we do an actual delete in the DB
        roomDao.deleteCrossRefsForFlashcard(flashcardId)
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

    // Now we implement getTopicsForFlashcard by joining
    fun getTopicsForFlashcard(flashcardId: Int): List<Topic> { // changes: update
        return roomDao.getTopicsForFlashcard(flashcardId)
    }

    fun getAllTopics(): List<Topic> {
        return roomDao.getAllTopics()
    }

    fun getTopicByName(topicName: String): Topic? {
        return roomDao.getTopicByName(topicName)
    }

    fun deleteFlashcard(flashcardId: Int) {
        val flashcard = roomDao.getFlashcardById(flashcardId)
        flashcard?.let {
            roomDao.deleteFlashcard(it)
        }
    }

    fun getFutureFlashcards(): List<Flashcard> {
        // Synchronous version (kept for reference)
        return roomDao.getFutureFlashcards(System.currentTimeMillis())
    }

    fun getPastFlashcards(): List<Flashcard> {
        // Synchronous version (kept for reference)
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
        Log.d("FlashcardDAO", "insertReviewHistory not fully implemented.")
    }

    // Async methods below...

    fun getNextDueFlashcardAsync(currentTime: Long, onResult: (Flashcard?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = getNextDueFlashcard(currentTime)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun updateFlashcardAsync(flashcard: Flashcard, onComplete: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            updateFlashcard(flashcard)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun getPastAndFutureQuestionsCountAsync(onResult: (IntArray) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val counts = getPastAndFutureQuestionsCount()
            withContext(Dispatchers.Main) {
                onResult(counts)
            }
        }
    }

    fun getFutureFlashcardsAsync(onResult: (List<Flashcard>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val list = getFutureFlashcards()
            withContext(Dispatchers.Main) {
                onResult(list)
            }
        }
    }

    fun getPastFlashcardsAsync(onResult: (List<Flashcard>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val list = getPastFlashcards()
            withContext(Dispatchers.Main) {
                onResult(list)
            }
        }
    }
}
