// File: ListFlashcardsActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListFlashcardsActivity extends AppCompatActivity {

    private RecyclerView rvFlashcards;
    private FlashcardAdapter adapter;
    private FlashcardDAO flashcardDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_flashcards);

        rvFlashcards = findViewById(R.id.rv_flashcards);
        rvFlashcards.setLayoutManager(new LinearLayoutManager(this));

        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        List<Flashcard> flashcards = flashcardDAO.getAllFlashcards();
        adapter = new FlashcardAdapter(this, flashcards);
        rvFlashcards.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
