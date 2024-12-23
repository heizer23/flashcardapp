// File: MainActivity.java
package com.example.flashcardapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnAddFlashcard, btnReviewFlashcards, btnListFlashcards, btnImportExport, btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         btnAddFlashcard = findViewById(R.id.btn_action);
         btnReviewFlashcards = findViewById(R.id.btn_review_flashcards);
         btnListFlashcards = findViewById(R.id.btn_list_flashcards);
         btnImportExport = findViewById(R.id.btn_import_export);
         btnCreate = findViewById(R.id.btn_create);

         btnAddFlashcard.setOnClickListener(v -> {
            // FlashcardDAO flashcardDAO = new FlashcardDAO(this);
            // flashcardDAO.open();
            // flashcardDAO.deleteAllData();

           //  Intent intent = new Intent(MainActivity.this, AddFlashcardActivity.class);
             Intent intent = new Intent(MainActivity.this, TopicSelectionActivity.class);
              startActivity(intent);
         });




         btnReviewFlashcards.setOnClickListener(v -> {
             Intent intent = new Intent(MainActivity.this, ReviewFlashcardsActivity.class);
             startActivity(intent);
         });

         btnListFlashcards.setOnClickListener(v -> {
             Intent intent = new Intent(MainActivity.this, ListFlashcardsActivity.class);
             startActivity(intent);
         });

         btnImportExport.setOnClickListener(v -> {
             Intent intent = new Intent(MainActivity.this, ImportFlashcardsActivity.class);
             startActivity(intent);
         });

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GenerateQuestionsActivity.class);
            startActivity(intent);
        });
    }
}
