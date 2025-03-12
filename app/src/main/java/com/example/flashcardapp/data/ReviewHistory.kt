package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "review_history",
    foreignKeys = [ForeignKey(
        entity = Flashcard::class,
        parentColumns = ["id"],
        childColumns = ["question_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ReviewHistory(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var question_id: Int = 0,
    var confidence_level: Int = 0,
    var timestamp: Long = System.currentTimeMillis(),
    var time_since_last_seen: Long = 0,
    var interval: Int = 0,
    var review_type: String = "",
    var answer_duration: Int = 0
)
