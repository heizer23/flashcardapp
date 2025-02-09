package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_history")
data class ReviewHistory(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var question_id: Int = 0,              // changes: update (remove ?)
    var confidence_level: Int = 0,         // changes: update
    var timestamp: Long = System.currentTimeMillis(), // changes: update
    var time_since_last_seen: Long = 0,    // changes: update
    var interval: Int = 0,                // changes: update
    var review_type: String = "",         // changes: update
    var answer_duration: Int = 0          // changes: update
)
