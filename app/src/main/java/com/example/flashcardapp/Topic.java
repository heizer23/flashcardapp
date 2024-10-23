// File: Topic.java
package com.example.flashcardapp;

public class Topic {
    private int id;
    private String name;

    // Constructors
    public Topic() {
        // Default constructor
    }

    public Topic(String name) {
        this.name = name;
    }

    public Topic(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
