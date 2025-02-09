// File: Flashcard.java
package com.example.flashcardapp.data;


import com.example.flashcardapp.data.Topic;

import java.util.List;

public class Flashcard {
    private int id;
    private String question;
    private String answer;
    private double easinessFactor;
    private int repetition;
    private int interval;
    private long nextReview;
    private String searchTerm;  // New field for search term
    private String userNote;    // New field for user note

    private List<Topic> topics;

    // Constructors
    public Flashcard() {
        // Default values
        this.easinessFactor = 2.5;
        this.repetition = 0;
        this.interval = 1;
        this.nextReview = System.currentTimeMillis();
    }

    public Flashcard(String question, String answer) {
        this();
        this.question = question;
        this.answer = answer;
    }

    public Flashcard(String question, String answer, String searchTerm, String userNote) {
        this(question, answer);
        this.searchTerm = searchTerm;
        this.userNote = userNote;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public double getEasinessFactor() { return easinessFactor; }
    public void setEasinessFactor(double easinessFactor) { this.easinessFactor = easinessFactor; }

    public int getRepetition() { return repetition; }
    public void setRepetition(int repetition) { this.repetition = repetition; }

    public int getInterval() { return interval; }
    public void setInterval(int interval) { this.interval = interval; }

    public long getNextReview() { return nextReview; }
    public void setNextReview(long nextReview) { this.nextReview = nextReview; }

    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }

    public String getUserNote() { return userNote; }
    public void setUserNote(String userNote) { this.userNote = userNote; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }
}
