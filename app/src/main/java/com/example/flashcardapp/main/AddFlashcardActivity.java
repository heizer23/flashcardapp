// File: AddFlashcardActivity.java
package com.example.flashcardapp.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.flashcardapp.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcardapp.viewmodel.AddFlashcardViewModel;

public class AddFlashcardActivity extends AppCompatActivity {

    private EditText etQuestion, etAnswer;
    private Button btnSave;

    // Removed direct FlashcardDAO field
    // private FlashcardDAO flashcardDAO;

    // New: reference our ViewModel
    private AddFlashcardViewModel addFlashcardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);

        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        btnSave = findViewById(R.id.btn_save);

        // Removed old DAO init:
        // flashcardDAO = new FlashcardDAO(this);
        // flashcardDAO.open();

        // Now we set up the ViewModel
        addFlashcardViewModel = new ViewModelProvider(this).get(AddFlashcardViewModel.class);
        addFlashcardViewModel.initialize(new FlashcardDAO(this));

        // Observe saveComplete to react when insert is finished
        addFlashcardViewModel.getSaveComplete().observe(this, isComplete -> {
            if (isComplete != null && isComplete) {
                Toast.makeText(this, "Flashcard saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                // Delegate to ViewModel
                addFlashcardViewModel.saveFlashcard(question, answer);
            } else {
                Toast.makeText(this, "Please enter both question and answer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Removed old close call:
        // flashcardDAO.close();
        super.onDestroy();
    }
}
