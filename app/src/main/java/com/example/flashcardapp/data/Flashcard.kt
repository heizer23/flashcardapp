package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var question: String = "",
    var answer: String = "",
    var easinessFactor: Double = 2.5,
    var repetition: Int = 0,
    var interval: Int = 1,
    var nextReview: Long = System.currentTimeMillis(),
    var searchTerm: String = "",
    var userNote: String = "",
    var topics: List<Topic> = emptyList()
) {
    // Secondary constructor to support usage like new Flashcard(question, answer)
    constructor(question: String, answer: String) : this(
        0, question, answer, 2.5, 0, 1, System.currentTimeMillis(), "", "", emptyList()
    )
}