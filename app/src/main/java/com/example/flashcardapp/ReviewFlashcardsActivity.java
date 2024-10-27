// File: ReviewFlashcardsActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReviewFlashcardsActivity extends AppCompatActivity {

    private TextView tvQuestion, tvAnswer;
    private Button btnShowAnswer;
    private Button btnForgot, btnStruggling, btnUnsure, btnOkay, btnGood, btnPerfect;
    private FlashcardDAO flashcardDAO;
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

        // Start the review process
        showNextFlashcard();

        btnShowAnswer.setOnClickListener(v -> {
            if (currentFlashcard != null) {
                tvAnswer.setText(currentFlashcard.getAnswer());
                tvAnswer.setVisibility(View.VISIBLE);

                // Show confidence buttons
                findViewById(R.id.low_confidence_buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.high_confidence_buttons).setVisibility(View.VISIBLE);
            }
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
        // Fetch the next due flashcard from the database
        currentFlashcard = flashcardDAO.getNextDueFlashcard(System.currentTimeMillis());

        if (currentFlashcard != null) {
            // Display the flashcard
            tvQuestion.setText(currentFlashcard.getQuestion());
            tvAnswer.setVisibility(View.GONE);
            findViewById(R.id.low_confidence_buttons).setVisibility(View.GONE);
            findViewById(R.id.high_confidence_buttons).setVisibility(View.GONE);
        } else {
            // Show a Toast message if no flashcards are due
            Toast.makeText(this, "No flashcards due for review!", Toast.LENGTH_SHORT).show();
            finish(); // End the activity if there are no flashcards left
        }
    }

    private void handleConfidence(int quality) {
        long previousReviewTime = currentFlashcard.getNextReview(); // Store the previous next_review time

        // Update the flashcard after review (this will modify nextReview time)
        updateFlashcardAfterReview(currentFlashcard, quality);

        // Calculate the time difference between the new and old next_review times
        long timePushed = currentFlashcard.getNextReview()  - System.currentTimeMillis();

        // Use the utility method to format the time difference and show a Toast
        String timeDifference = TimeUtils.formatTimeDifference(timePushed);
        Toast.makeText(this, "Next review in: " + timeDifference, Toast.LENGTH_LONG).show();

        // Continue to the next flashcard or finish the session
        showNextFlashcard();
    }


    // Updated the review algorithm to use faster intervals with seconds-based increments.
    private void updateFlashcardAfterReview(Flashcard flashcard, int quality) {
        int interval = flashcard.getInterval(); // Current interval in seconds
        int repetition = flashcard.getRepetition(); // Current repetition count
        long currentTime = System.currentTimeMillis();
        long lastReviewTime = flashcard.getNextReview() - interval * 1000L; // Calculate when the last review was

        // Handle different quality values and set new interval
        switch (quality) {
            case 0:
                interval = 1; // 0 = 1s
                repetition = 0; // Reset repetition if wrong
                break;
            case 1:
                interval = 10; // 1 = 10s
                repetition = 0; // Reset repetition if wrong
                break;
            case 2:
                interval = 20; // 2 = 20s
                repetition = 0; // Reset repetition if wrong
                break;
            case 3:
                interval = 30; // 3 = 30s
                repetition += 1; // Increment repetition if right
                break;
            case 4:
                interval = 1600; // 4 = last interval * 2
                repetition += 1; // Increment repetition if right
                break;
            case 5:
                // 5 = time lapsed since the last time I saw the question * 2
                long timeLapsed = (currentTime - lastReviewTime) / 1000; // Convert time lapsed to seconds
                interval = (int) (timeLapsed * 2)+3600;
                repetition += 1; // Increment repetition if right
                break;
            default:
                // Fallback for unexpected values (just in case)
                interval = 30;
                repetition = 0; // Reset repetition just in case
                break;
        }

        // Calculate the next review time
        long nextReview = currentTime + interval * 1000L;

        // Update flashcard with the new interval, next review time, and repetition count
        flashcard.setInterval(interval);
        flashcard.setNextReview(nextReview);
        flashcard.setRepetition(repetition); // Save repetition count

        // Save flashcard to the database
        flashcardDAO.updateFlashcard(flashcard);
    }


    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
