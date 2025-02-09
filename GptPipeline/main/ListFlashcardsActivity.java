// File: ListFlashcardsActivity.java
package com.example.flashcardapp.main;

import android.os.Bundle;
import android.widget.ToggleButton;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Flashcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListFlashcardsActivity extends AppCompatActivity {

    private ToggleButton toggleReviewTime;
    private FlashcardDAO flashcardDAO;
    private List<Flashcard> flashcards;
    private FlashcardAdapter flashcardAdapter; // Assuming you're using a RecyclerView adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_flashcards);

        // Initialize views
        toggleReviewTime = findViewById(R.id.toggle_review_time);
        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        // Set up the adapter and the RecyclerView
        flashcards = new ArrayList<>();
        flashcardAdapter = new FlashcardAdapter(this, flashcards); // Adjust the constructor as per your adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view_flashcards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(flashcardAdapter);

        // Load initial data (future questions ascending)
        loadFlashcards(true);

        // Set toggle button listener to switch between past and future
        toggleReviewTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show past questions, descending order
                loadFlashcards(false);
            } else {
                // Show future questions, ascending order
                loadFlashcards(true);
            }
        });
    }

    private void loadFlashcards(boolean showFuture) {
        // Clear the current list
        flashcards.clear();

        if (showFuture) {
            // Load questions in the future, ordered ascending
            flashcards.addAll(flashcardDAO.getFutureFlashcards());
        } else {
            // Load questions in the past, ordered descending
            flashcards.addAll(flashcardDAO.getPastFlashcards());
        }

        // Notify the adapter that the data has changed
        flashcardAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}