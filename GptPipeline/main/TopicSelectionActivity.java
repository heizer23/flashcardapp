// File: TopicSelectionActivity.java
package com.example.flashcardapp.main;

import android.os.Bundle;
import android.widget.Button;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Topic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TopicSelectionActivity extends AppCompatActivity {

    private TopicSelectionAdapter adapter;
    private FlashcardDAO flashcardDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);

        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        RecyclerView rvTopics = findViewById(R.id.rv_topics);
        Button btnSave = findViewById(R.id.btn_save);

        rvTopics.setLayoutManager(new LinearLayoutManager(this));

        // Load all topics from the database
        List<Topic> allTopics = flashcardDAO.getAllTopics();

        // Initialize the adapter with the topics list
        adapter = new TopicSelectionAdapter(allTopics);
        rvTopics.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            saveSelectedTopicsToDatabase(adapter.getSelectedTopics());
            finish(); // Close activity after saving
        });
    }

    private void saveSelectedTopicsToDatabase(List<Topic> topics) {
        for (Topic topic : topics) {
            flashcardDAO.updateTopicSelection(topic.getId(), topic.isSelected());
        }
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
