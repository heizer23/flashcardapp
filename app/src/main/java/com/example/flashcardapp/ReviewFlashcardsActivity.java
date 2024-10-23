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

    // Updated the review algorithm to use faster intervals with seconds-based increments.
    private void updateFlashcardAfterReview(Flashcard flashcard, int quality) {
        double ef = flashcard.getEasinessFactor();  // Easiness factor
        int repetition = flashcard.getRepetition(); // Number of correct repetitions
        int interval = flashcard.getInterval();     // Interval in seconds

        // Parameters
        int minInterval = 5;   // Minimum interval in seconds
        int maxInterval = 86400 * 30; // Maximum interval (30 days in seconds)
        double minEf = 1.3;     // Minimum easiness factor
        double maxEf = 2.5;     // Maximum easiness factor

        // Adjust ef based on quality
        if (quality >= 3) {
            // Correct answer, increase ef slightly
            ef += 0.1 * (quality - 3);
            if (ef > maxEf) {
                ef = maxEf;
            }
        } else {
            // Incorrect answer, decrease ef
            ef -= 0.2 * (3 - quality);
            if (ef < minEf) {
                ef = minEf;
            }
        }

        // Update repetition and interval
        if (quality >= 3) {
            // Correct answer
            repetition += 1;

            // Calculate new interval
            if (repetition == 1) {
                interval = minInterval;
            } else {
                // Interval increases exponentially with ef and repetitions
                interval = (int) (minInterval * Math.pow(ef, repetition - 1));

                // Ensure interval doesn't exceed maximum allowed interval
                if (interval > maxInterval) {
                    interval = maxInterval;
                }
            }
        } else {
            // Incorrect answer
            repetition = 0;

            // Set interval based on quality (lower quality => shorter interval)
            interval = (int) (minInterval / (quality + 1));
        }

        // Calculate next review time in seconds
        long nextReview = System.currentTimeMillis() + interval * 1000L;

        // Update flashcard with the new values
        flashcard.setEasinessFactor(ef);
        flashcard.setRepetition(repetition);
        flashcard.setInterval(interval);
        flashcard.setNextReview(nextReview);

        // Save flashcard to database
        flashcardDAO.updateFlashcard(flashcard);
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
