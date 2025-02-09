package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var question: String = "",      // changes: update (remove ?) 
    var answer: String = "",        // changes: update
    var easinessFactor: Double = 2.5, // changes: update
    var repetition: Int = 0,        // changes: update
    var interval: Int = 1,          // changes: update
    var nextReview: Long = System.currentTimeMillis(), // changes: update
    var searchTerm: String = "",    // changes: update
    var userNote: String = ""       // changes: update
) {
    // Secondary constructor (unchanged)
    constructor(question: String, answer: String) : this(
        0, question, answer, 2.5, 0, 1, System.currentTimeMillis(), "", ""
    )
}
