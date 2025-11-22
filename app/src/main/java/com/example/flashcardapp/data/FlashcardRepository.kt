package com.example.flashcardapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class FlashcardRepository(private val flashcardDao: FlashcardDao) {

    suspend fun createFlashcard(flashcard: Flashcard): Flashcard = withContext(Dispatchers.IO) {
        val newId = flashcardDao.insertFlashcard(flashcard)
        flashcard.id = newId.toInt()
        flashcard
    }

    suspend fun insertFlashcard(flashcard: Flashcard): Long = withContext(Dispatchers.IO) {
        flashcardDao.insertFlashcard(flashcard)
    }

    suspend fun updateFlashcard(flashcard: Flashcard) = withContext(Dispatchers.IO) {
        flashcardDao.updateFlashcard(flashcard)
    }

    suspend fun deleteFlashcard(flashcard: Flashcard) = withContext(Dispatchers.IO) {
        flashcardDao.deleteFlashcard(flashcard)
    }

    suspend fun getFlashcardById(id: Int): Flashcard? = withContext(Dispatchers.IO) {
        flashcardDao.getFlashcardById(id)
    }

    suspend fun getAllFlashcards(): List<Flashcard> = withContext(Dispatchers.IO) {
        flashcardDao.getAllFlashcards()
    }

    suspend fun getNextDueFlashcard(currentTime: Long): Flashcard? = withContext(Dispatchers.IO) {
        flashcardDao.getNextDueFlashcard(currentTime)
    }

    suspend fun getNextDueFlashcardForSelectedTopics(currentTime: Long): Flashcard? = withContext(Dispatchers.IO) {
        flashcardDao.getNextDueFlashcardForSelectedTopics(currentTime)
    }

    suspend fun getFutureFlashcards(currentTime: Long): List<Flashcard> =
        withContext(Dispatchers.IO) {
            flashcardDao.getFutureFlashcards(currentTime)
        }

    suspend fun getPastFlashcards(currentTime: Long): List<Flashcard> =
        withContext(Dispatchers.IO) {
            flashcardDao.getPastFlashcards(currentTime)
        }

    suspend fun insertTopic(topic: Topic): Topic = withContext(Dispatchers.IO) {
        val existing = flashcardDao.getTopicByName(topic.name)
        if (existing != null) {
            existing
        } else {
            val newId = flashcardDao.insertTopic(topic)
            topic.id = newId.toInt()
            topic
        }
    }

    suspend fun getAllTopics(): List<Topic> = withContext(Dispatchers.IO) {
        flashcardDao.getAllTopics()
    }

    suspend fun getTopicByName(topicName: String): Topic? = withContext(Dispatchers.IO) {
        flashcardDao.getTopicByName(topicName)
    }

    suspend fun updateTopicSelection(topicId: Int, isSelected: Boolean) =
        withContext(Dispatchers.IO) {
            flashcardDao.updateTopicSelection(topicId, isSelected)
        }

    suspend fun associateFlashcardWithTopic(flashcardId: Int, topicId: Int) =
        withContext(Dispatchers.IO) {
            flashcardDao.insertCrossRef(FlashcardTopicCrossRef(flashcardId, topicId))
        }

    suspend fun getPastAndFutureQuestionsCount(currentTime: Long): IntArray =
        withContext(Dispatchers.IO) {
            val past = flashcardDao.getPastCount(currentTime)
            val future = flashcardDao.getFutureCount(currentTime)
            intArrayOf(past, future)
        }

    suspend fun getPastAndFutureQuestionsCountForSelectedTopics(currentTime: Long): IntArray =
        withContext(Dispatchers.IO) {
            val past = flashcardDao.getPastCountForSelectedTopics(currentTime)
            val future = flashcardDao.getFutureCountForSelectedTopics(currentTime)
            intArrayOf(past, future)
        }

    suspend fun getDueFlashcardsCountForLevel(currentTime: Long, level: Int): Int = withContext(Dispatchers.IO) {
        flashcardDao.getDueFlashcardsCountForLevel(currentTime, level)
    }

    suspend fun getLastReviewedTimestamp(flashcardId: Int): Long? = withContext(Dispatchers.IO) {
        flashcardDao.getLastReviewedTimestamp(flashcardId)
    }

    suspend fun getReviewedCount(flashcardId: Int): Int = withContext(Dispatchers.IO) {
        flashcardDao.getReviewedCount(flashcardId)
    }

    suspend fun getPastFlashcardsForSelectedTopics(currentTime: Long): List<Flashcard> =
        withContext(Dispatchers.IO) {
            flashcardDao.getPastFlashcardsForSelectedTopics(currentTime)
        }

    suspend fun getFutureFlashcardsForSelectedTopics(currentTime: Long): List<Flashcard> =
        withContext(Dispatchers.IO) {
            flashcardDao.getFutureFlashcardsForSelectedTopics(currentTime)
        }

    suspend fun getAllFlashcardsForSelectedTopics(): List<Flashcard> = withContext(Dispatchers.IO) {
        flashcardDao.getAllFlashcardsForSelectedTopics()
    }

    suspend fun getTotalFlashcardsForSelectedTopics(): Int = withContext(Dispatchers.IO) {
        flashcardDao.getTotalFlashcardsForSelectedTopics()
    }

    suspend fun insertReviewHistory(reviewHistory: ReviewHistory) = withContext(Dispatchers.IO) {
        flashcardDao.insertReviewHistory(reviewHistory)
    }

    suspend fun getTodaysReviewedFlashcardCount(): Int = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        flashcardDao.getTodaysReviewedFlashcardCount(startOfDay)
    }

    // changes: create or update function to handle crossRef deletion, topic creation, and linking
    suspend fun deleteCrossRefsForFlashcard(flashcardId: Int) = withContext(Dispatchers.IO) {
        flashcardDao.deleteCrossRefsForFlashcard(flashcardId)
    }

    suspend fun getTopicsForFlashcard(flashcardId: Int): List<Topic> = withContext(Dispatchers.IO) {
        flashcardDao.getTopicsForFlashcard(flashcardId)
    }

    suspend fun getAllTopicsWithCount(): List<TopicWithCount> = withContext(Dispatchers.IO) {
        flashcardDao.getAllTopicsWithCount()
    }

    // changes: create new function to update a flashcard and its topics in one go
    suspend fun updateFlashcardWithTopics(
        flashcard: Flashcard,
        topicNames: List<String>
    ) = withContext(Dispatchers.IO) {
        // Update the main flashcard
        flashcardDao.updateFlashcard(flashcard)

        // Clear old cross references
        flashcardDao.deleteCrossRefsForFlashcard(flashcard.id)

        // For each topic name, find or create topic, then insert crossRef
        for (rawName in topicNames) {
            val trimmedName = rawName.trim()
            if (trimmedName.isNotEmpty()) {
                val existingTopic = flashcardDao.getTopicByName(trimmedName)
                val topic = if (existingTopic == null) {
                    val topicObj = Topic(name = trimmedName)
                    val newId = flashcardDao.insertTopic(topicObj)
                    topicObj.id = newId.toInt()
                    topicObj
                } else {
                    existingTopic
                }
                flashcardDao.insertCrossRef(FlashcardTopicCrossRef(flashcard.id, topic.id))
            }
        }
    }
}