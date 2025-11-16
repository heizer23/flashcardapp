package com.example.flashcardapp.data

// changes: create
// Simple data class to hold topic plus count of flashcards
data class TopicWithCount(
    val id: Int,
    val name: String,
    var selected: Boolean,
    val cardCount: Int
)
