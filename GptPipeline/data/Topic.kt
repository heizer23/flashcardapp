package com.example.flashcardapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String = "",
    var selected: Boolean = false
) {
    // Secondary constructor to support usage like new Topic(topicName)
    constructor(name: String) : this(0, name, false)
}