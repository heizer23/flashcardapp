package com.example.flashcardapp.data

import androidx.room.Entity

@Entity(primaryKeys = ["flashcardId", "topicId"])
data class FlashcardTopicCrossRef(
    val flashcardId: Int,
    val topicId: Int
)