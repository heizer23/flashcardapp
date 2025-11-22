package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var question: String = "",
    var answer: String = "",
    var repetition: Int = 0,
    var interval: Int = 1,
    var nextReview: Long = System.currentTimeMillis(),
    var lastAnswer: Boolean = false,
    var level: Int = 0,
    var mainItem: Boolean = false
) {
    // Secondary constructor
    constructor(question: String, answer: String) : this(
        0, question, answer, 0, 1, System.currentTimeMillis(), false, 0, false
    )

    // We add a transient property for topics so it won't affect the DB schema.
    @Transient
    var topicNames: List<String> = emptyList()
}
