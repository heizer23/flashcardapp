package com.example.flashcardapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFlashcardActivity extends AppCompatActivity {

    private EditText etQuestion, etAnswer, etSearchTerm, etUserNote, etTopics;
    private Button btnUpdate;
    private FlashcardDAO flashcardDAO;
    private Flashcard flashcard;
    private List<Topic> associatedTopics;

    // Cache for topics
    private Map<String, Topic> topicCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_flashcard);

        // Initialize views
        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        etSearchTerm = findViewById(R.id.et_search_term);
        etUserNote = findViewById(R.id.et_user_note);
        etTopics = findViewById(R.id.et_topics);
        btnUpdate = findViewById(R.id.btn_update);

        // Initialize DAO
        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();



        // Preload the topic cache with all existing topics from the database
        preloadTopicCache();

        // Retrieve flashcard ID from Intent
        int flashcardId = getIntent().getIntExtra("FLASHCARD_ID", -1);
        if (flashcardId != -1) {
            flashcard = flashcardDAO.getFlashcard(flashcardId);
            if (flashcard != null) {
                // Pre-fill fields with flashcard data
                etQuestion.setText(flashcard.getQuestion());
                etAnswer.setText(flashcard.getAnswer());
                etSearchTerm.setText(flashcard.getSearchTerm());
                etUserNote.setText(flashcard.getUserNote());

                // Get topics associated with the flashcard and display them as comma-separated values
                associatedTopics = flashcardDAO.getTopicsForFlashcard(flashcard.getId());
                StringBuilder topicList = new StringBuilder();
                for (Topic topic : associatedTopics) {
                    topicList.append(topic.getName()).append(", ");
                }

                // Remove last comma and space if topics exist
                if (topicList.length() > 0) {
                    topicList.setLength(topicList.length() - 2);
                }
                etTopics.setText(topicList.toString());
            }
        }

        // Set up update button
        btnUpdate.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();
            String searchTerm = etSearchTerm.getText().toString().trim();
            String userNote = etUserNote.getText().toString().trim();
            String topics = etTopics.getText().toString().trim();  // Comma-separated topics

            if (!question.isEmpty() && !answer.isEmpty()) {
                // Update flashcard fields
                flashcard.setQuestion(question);
                flashcard.setAnswer(answer);
                flashcard.setSearchTerm(searchTerm);
                flashcard.setUserNote(userNote);
                flashcardDAO.updateFlashcard(flashcard);

                // Clear existing topic associations for the flashcard
                flashcardDAO.clearTopicsForFlashcard(flashcard.getId());

                // Handle topics using cache (convert comma-separated list into individual topics)
                String[] topicArray = topics.split(",");
                for (String topicName : topicArray) {
                    topicName = topicName.trim();
                    if (!topicName.isEmpty()) {
                        Topic topic = getOrInsertTopicFromCache(topicName);
                        flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId());
                    }
                }

                Toast.makeText(this, "Flashcard updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please enter both question and answer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Preload existing topics into the cache
    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
            topicCache.put(topic.getName(), topic);
        }
    }

    // Get or insert topic from cache
    private Topic getOrInsertTopicFromCache(String topicName) {
        // First check in the cache
        if (topicCache.containsKey(topicName)) {
            return topicCache.get(topicName);
        }

        // If not found in cache, check the database
        Topic topic = flashcardDAO.getTopicByName(topicName);
        if (topic == null) {
            // If not found in the database, insert the new topic
            topic = flashcardDAO.insertTopic(topicName);
        }

        // Add the topic to the cache
        topicCache.put(topicName, topic);

        return topic;
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
