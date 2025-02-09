package com.example.flashcardapp.data

import androidx.room.*

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFlashcard(flashcard: Flashcard): Long

    @Update
    fun updateFlashcard(flashcard: Flashcard)

    @Delete
    fun deleteFlashcard(flashcard: Flashcard)

    @Query("SELECT * FROM flashcards WHERE id = :id")
    fun getFlashcardById(id: Int): Flashcard?

    @Query("SELECT * FROM flashcards ORDER BY nextReview ASC")
    fun getAllFlashcards(): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE nextReview <= :currentTime ORDER BY nextReview DESC LIMIT 1")
    fun getNextDueFlashcard(currentTime: Long): Flashcard?

    @Query("SELECT * FROM flashcards WHERE nextReview > :currentTime ORDER BY nextReview ASC")
    fun getFutureFlashcards(currentTime: Long): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE nextReview <= :currentTime ORDER BY nextReview DESC")
    fun getPastFlashcards(currentTime: Long): List<Flashcard>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTopic(topic: Topic): Long

    @Query("SELECT * FROM topics")
    fun getAllTopics(): List<Topic>

    @Query("SELECT * FROM topics WHERE name = :topicName LIMIT 1")
    fun getTopicByName(topicName: String): Topic?

    @Query("UPDATE topics SET selected = :isSelected WHERE id = :topicId")
    fun updateTopicSelection(topicId: Int, isSelected: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCrossRef(crossRef: FlashcardTopicCrossRef)

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReview <= :currentTime")
    fun getPastCount(currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReview > :currentTime")
    fun getFutureCount(currentTime: Long): Int
}
