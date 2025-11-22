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

    @Query("SELECT * FROM flashcards WHERE nextReview <= :currentTime ORDER BY level ASC, nextReview DESC LIMIT 1")
    fun getNextDueFlashcard(currentTime: Long): Flashcard?

    @Query("SELECT DISTINCT f.* FROM flashcards f JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id JOIN topics t ON t.id = xref.topicId WHERE t.selected = 1 AND f.nextReview <= :currentTime ORDER BY f.level ASC, f.nextReview DESC LIMIT 1")
    fun getNextDueFlashcardForSelectedTopics(currentTime: Long): Flashcard?

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

    @Query("DELETE FROM flashcard_topic_cross_ref WHERE flashcardId = :flashcardId")
    fun deleteCrossRefsForFlashcard(flashcardId: Int)

    @Query("SELECT t.* FROM topics t INNER JOIN flashcard_topic_cross_ref fcr ON t.id = fcr.topicId WHERE fcr.flashcardId = :flashcardId")
    fun getTopicsForFlashcard(flashcardId: Int): List<Topic>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReview <= :currentTime")
    fun getPastCount(currentTime: Long): Int

    @Query("SELECT COUNT(DISTINCT f.id) FROM flashcards f JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id JOIN topics t ON t.id = xref.topicId WHERE t.selected = 1 AND f.nextReview <= :currentTime")
    fun getPastCountForSelectedTopics(currentTime: Long): Int

    @Query("SELECT COUNT(DISTINCT f.id) FROM flashcards f JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id JOIN topics t ON t.id = xref.topicId WHERE t.selected = 1 AND f.nextReview <= :currentTime AND f.level = :level")
    fun getDueFlashcardsCountForLevel(currentTime: Long, level: Int): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReview > :currentTime")
    fun getFutureCount(currentTime: Long): Int

    @Query("SELECT COUNT(DISTINCT f.id) FROM flashcards f JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id JOIN topics t ON t.id = xref.topicId WHERE t.selected = 1 AND f.nextReview > :currentTime")
    fun getFutureCountForSelectedTopics(currentTime: Long): Int

    @Query("SELECT MAX(timestamp) FROM review_history WHERE question_id = :flashcardId")
    fun getLastReviewedTimestamp(flashcardId: Int): Long?

    @Query("SELECT COUNT(id) FROM review_history WHERE question_id = :flashcardId")
    fun getReviewedCount(flashcardId: Int): Int

    @Query("SELECT DISTINCT f.* FROM flashcards f\n           JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id\n           JOIN topics t ON t.id = xref.topicId\n           WHERE t.selected = 1 AND f.nextReview <= :currentTime\n           ORDER BY f.level ASC, f.nextReview DESC")
    fun getPastFlashcardsForSelectedTopics(currentTime: Long): List<Flashcard>

    @Query("SELECT DISTINCT f.* FROM flashcards f\n           JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id\n           JOIN topics t ON t.id = xref.topicId\n           WHERE t.selected = 1 AND f.nextReview > :currentTime\n           ORDER BY f.level ASC, f.nextReview ASC")
    fun getFutureFlashcardsForSelectedTopics(currentTime: Long): List<Flashcard>

    @Query("SELECT DISTINCT f.* FROM flashcards f\n           JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id\n           JOIN topics t ON t.id = xref.topicId\n           WHERE t.selected = 1\n           ORDER BY f.level ASC, f.nextReview ASC")
    fun getAllFlashcardsForSelectedTopics(): List<Flashcard>

    @Query("SELECT COUNT(DISTINCT f.id) FROM flashcards f JOIN flashcard_topic_cross_ref xref ON xref.flashcardId = f.id JOIN topics t ON t.id = xref.topicId WHERE t.selected = 1")
    fun getTotalFlashcardsForSelectedTopics(): Int

    @Insert
    fun insertReviewHistory(reviewHistory: ReviewHistory): Long

    @Query("SELECT COUNT(DISTINCT question_id) FROM review_history WHERE timestamp >= :startOfDay")
    fun getTodaysReviewedFlashcardCount(startOfDay: Long): Int

    // changes: create new single query for topics plus count
    @Query("""
        SELECT t.id, t.name, t.selected, COUNT(x.flashcardId) AS cardCount
          FROM topics t
          LEFT JOIN flashcard_topic_cross_ref x
                 ON x.topicId = t.id
      GROUP BY t.id
    """)
    fun getAllTopicsWithCount(): List<TopicWithCount>
}