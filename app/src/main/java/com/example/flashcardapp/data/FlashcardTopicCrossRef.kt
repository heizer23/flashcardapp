package com.example.flashcardapp.data

import androidx.room.Entity

@Entity(tableName = "flashcard_topic_cross_ref", primaryKeys = ["flashcardId", "topicId"])
 data class FlashcardTopicCrossRef(
    val flashcardId: Int,
    val topicId: Int
)
