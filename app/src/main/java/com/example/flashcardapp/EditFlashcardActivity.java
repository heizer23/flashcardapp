// File: EditFlashcardActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditFlashcardActivity extends AppCompatActivity {

    private EditText etQuestion, etAnswer;
    private Button btnUpdate;
    private FlashcardDAO flashcardDAO;
    private Flashcard flashcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_flashcard);

        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        btnUpdate = findViewById(R.id.btn_update);

        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        int flashcardId = getIntent().getIntExtra("FLASHCARD_ID", -1);
        if (flashcardId != -1) {
            flashcard = flashcardDAO.getFlashcard(flashcardId);
            if (flashcard != null) {
                etQuestion.setText(flashcard.getQuestion());
                etAnswer.setText(flashcard.getAnswer());
            }
        }

        btnUpdate.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                flashcard.setQuestion(question);
                flashcard.setAnswer(answer);
                flashcardDAO.updateFlashcard(flashcard);
                Toast.makeText(this, "Flashcard updated!", Toast.LENGTH_SHORT).show();
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
