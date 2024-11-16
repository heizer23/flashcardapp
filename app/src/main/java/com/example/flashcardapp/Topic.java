// File: Topic.java
package com.example.flashcardapp;

public class Topic {
    private int id;
    private String name;
    private boolean selected; // New field for tracking selection state
    // Constructors
    public Topic() {
        // Default constructor
    }

    public Topic(String name) {
        this.name = name;
    }

    public Topic(int id, String name, boolean selected) {
        this.id = id;
        this.name = name;
        this.selected = selected;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isSelected() { return selected; } // New getter
    public void setSelected(boolean selected) { this.selected = selected; }
}
