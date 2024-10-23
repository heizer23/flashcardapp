// File: FlashcardTopicCrossRef.java
package com.example.flashcardapp;

public class FlashcardTopicCrossRef {
    private int flashcardId;
    private int topicId;

    // Constructors
    public FlashcardTopicCrossRef() {}

    public FlashcardTopicCrossRef(int flashcardId, int topicId) {
        this.flashcardId = flashcardId;
        this.topicId = topicId;
    }

    // Getters and Setters
    public int getFlashcardId() { return flashcardId; }
    public void setFlashcardId(int flashcardId) { this.flashcardId = flashcardId; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }
}
