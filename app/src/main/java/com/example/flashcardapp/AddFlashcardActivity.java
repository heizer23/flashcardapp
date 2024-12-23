// File: AddFlashcardActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.flashcardapp.data.Flashcard;

import androidx.appcompat.app.AppCompatActivity;

public class AddFlashcardActivity extends AppCompatActivity {

    private EditText etQuestion, etAnswer;
    private Button btnSave;
    private FlashcardDAO flashcardDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);

        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        btnSave = findViewById(R.id.btn_save);

        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        btnSave.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                Flashcard flashcard = new Flashcard(question, answer);
                flashcardDAO.createFlashcard(flashcard);
                Toast.makeText(this, "Flashcard saved!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please enter both question and answer.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
