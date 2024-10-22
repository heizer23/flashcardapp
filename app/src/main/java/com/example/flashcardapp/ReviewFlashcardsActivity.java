// File: ReviewFlashcardsActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ReviewFlashcardsActivity extends AppCompatActivity {

    private TextView tvQuestion, tvAnswer;
    private Button btnShowAnswer;
    private Button btnForgot, btnStruggling, btnUnsure, btnOkay, btnGood, btnPerfect;
    private FlashcardDAO flashcardDAO;
    private List<Flashcard> dueFlashcards;
    private int currentIndex = 0;
    private Flashcard currentFlashcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_flashcards);

        // Initialize views
        tvQuestion = findViewById(R.id.tv_question);
        tvAnswer = findViewById(R.id.tv_answer);
        btnShowAnswer = findViewById(R.id.btn_show_answer);
        btnForgot = findViewById(R.id.btn_forgot);
        btnStruggling = findViewById(R.id.btn_struggling);
        btnUnsure = findViewById(R.id.btn_unsure);
        btnOkay = findViewById(R.id.btn_okay);
        btnGood = findViewById(R.id.btn_good);
        btnPerfect = findViewById(R.id.btn_perfect);

        // Hide buttons by default
        findViewById(R.id.low_confidence_buttons).setVisibility(View.GONE);
        findViewById(R.id.high_confidence_buttons).setVisibility(View.GONE);

        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        dueFlashcards = getDueFlashcards();
        if (dueFlashcards.isEmpty()) {
            Toast.makeText(this, "No flashcards due for review.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            showNextFlashcard();
        }

        btnShowAnswer.setOnClickListener(v -> {
            tvAnswer.setText(currentFlashcard.getAnswer());
            tvAnswer.setVisibility(View.VISIBLE);

            // Show confidence buttons
            findViewById(R.id.low_confidence_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.high_confidence_buttons).setVisibility(View.VISIBLE);
        });

        // Confidence button listeners
        btnForgot.setOnClickListener(v -> handleConfidence(0));
        btnStruggling.setOnClickListener(v -> handleConfidence(1));
        btnUnsure.setOnClickListener(v -> handleConfidence(2));
        btnOkay.setOnClickListener(v -> handleConfidence(3));
        btnGood.setOnClickListener(v -> handleConfidence(4));
        btnPerfect.setOnClickListener(v -> handleConfidence(5));
    }

    private void showNextFlashcard() {
        currentFlashcard = dueFlashcards.get(currentIndex);
        tvQuestion.setText(currentFlashcard.getQuestion());
        tvAnswer.setVisibility(View.GONE);
        findViewById(R.id.low_confidence_buttons).setVisibility(View.GONE);
        findViewById(R.id.high_confidence_buttons).setVisibility(View.GONE);
    }

    private void handleConfidence(int quality) {
        updateFlashcardAfterReview(currentFlashcard, quality);
        currentIndex++;
        if (currentIndex < dueFlashcards.size()) {
            showNextFlashcard();
        } else {
            Toast.makeText(this, "Review session completed!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private List<Flashcard> getDueFlashcards() {
        List<Flashcard> allFlashcards = flashcardDAO.getAllFlashcards();
        List<Flashcard> due = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (Flashcard flashcard : allFlashcards) {
            if (flashcard.getNextReview() <= currentTime) {
                due.add(flashcard);
            }
        }
        return due;
    }

    private void updateFlashcardAfterReview(Flashcard flashcard, int quality) {
        double ef = flashcard.getEasinessFactor();
        int repetition = flashcard.getRepetition();
        int interval = flashcard.getInterval();

        ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (ef < 1.3) {
            ef = 1.3;
        }

        if (quality < 3) {
            repetition = 0;
            interval = 1;
        } else {
            repetition += 1;
            if (repetition == 1) {
                interval = 1;
            } else if (repetition == 2) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * ef);
            }
        }

        long nextReview = System.currentTimeMillis() + interval * 24 * 60 * 60 * 1000L; // Interval in days

        flashcard.setEasinessFactor(ef);
        flashcard.setRepetition(repetition);
        flashcard.setInterval(interval);
        flashcard.setNextReview(nextReview);

        flashcardDAO.updateFlashcard(flashcard);
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
