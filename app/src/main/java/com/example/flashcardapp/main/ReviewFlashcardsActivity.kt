package com.example.flashcardapp.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard
import java.util.HashSet

class ReviewFlashcardsActivity : AppCompatActivity() {

    private var totalQuestionsCount = 0
    private var questionsMovedCount = 0
    private var score = 0 // Current session score

    private lateinit var tvTotalQuestions: TextView
    private lateinit var tvQuestionsMoved: TextView
    private lateinit var tvPast: TextView
    private lateinit var tvFuture: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvAnswer: TextView
    private lateinit var btnShowAnswer: Button
    private lateinit var btnForgot: Button
    private lateinit var btnStruggling: Button
    private lateinit var btnUnsure: Button
    private lateinit var btnOkay: Button
    private lateinit var btnGood: Button
    private lateinit var btnPerfect: Button

    private lateinit var flashcardDAO: FlashcardDAO
    private var currentFlashcard: Flashcard? = null

    private var answerStartTime: Long = 0

    private val seenFlashcards = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_flashcards)

        // Initialize views
        tvTotalQuestions = findViewById(R.id.tv_total_questions)
        tvQuestionsMoved = findViewById(R.id.tv_questions_moved)
        tvPast = findViewById(R.id.tv_past_questions)
        tvFuture = findViewById(R.id.tv_future_questions)
        tvQuestion = findViewById(R.id.tv_question)
        tvAnswer = findViewById(R.id.tv_answer)
        btnShowAnswer = findViewById(R.id.btn_show_answer)
        btnForgot = findViewById(R.id.btn_forgot)
        btnStruggling = findViewById(R.id.btn_struggling)
        btnUnsure = findViewById(R.id.btn_unsure)
        btnOkay = findViewById(R.id.btn_okay)
        btnGood = findViewById(R.id.btn_good)
        btnPerfect = findViewById(R.id.btn_perfect)

        findViewById<View>(R.id.low_confidence_buttons).visibility = View.GONE
        findViewById<View>(R.id.high_confidence_buttons).visibility = View.GONE

        flashcardDAO = FlashcardDAO(this)
        flashcardDAO.open()

        // Start the review process (ASYNC now)
        showNextFlashcard()

        tvQuestion.setOnClickListener {
            openEditQuestion()
        }

        btnShowAnswer.setOnClickListener {
            if (currentFlashcard != null) {
                tvAnswer.text = currentFlashcard!!.answer
                tvAnswer.visibility = View.VISIBLE
                btnShowAnswer.visibility = View.GONE

                // Start timer for answer duration
                answerStartTime = System.nanoTime()

                // Show confidence buttons
                findViewById<View>(R.id.low_confidence_buttons).visibility = View.VISIBLE
                findViewById<View>(R.id.high_confidence_buttons).visibility = View.VISIBLE
            }
        }

        // Confidence button listeners
        btnForgot.setOnClickListener { handleConfidence(0) }
        btnStruggling.setOnClickListener { handleConfidence(1) }
        btnUnsure.setOnClickListener { handleConfidence(2) }
        btnOkay.setOnClickListener { handleConfidence(3) }
        btnGood.setOnClickListener { handleConfidence(4) }
        btnPerfect.setOnClickListener { handleConfidence(5) }
    }

    // Fetches the next due flashcard on a background thread via Coroutines
    private fun showNextFlashcard() {
        flashcardDAO.getNextDueFlashcardAsync(System.currentTimeMillis()) { nextCard ->
            if (nextCard != null) {
                currentFlashcard = nextCard

                tvQuestion.text = currentFlashcard!!.question
                tvAnswer.visibility = View.GONE
                findViewById<View>(R.id.low_confidence_buttons).visibility = View.GONE
                findViewById<View>(R.id.high_confidence_buttons).visibility = View.GONE
                btnShowAnswer.visibility = View.VISIBLE

                seenFlashcards.add(currentFlashcard!!.id)
                totalQuestionsCount++
                updateCounters()
            } else {
                // No flashcards left
                Toast.makeText(this@ReviewFlashcardsActivity, "No flashcards due for review!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleConfidence(quality: Int) {
        val currentTime = System.currentTimeMillis()
        val answerDuration = (System.nanoTime() - answerStartTime) / 1_000_000
        currentFlashcard?.let {
            val previousReviewTime = it.nextReview - it.interval

            // DB logging, not fully implemented
            flashcardDAO.insertReviewHistory(
                it.id,
                quality,
                currentTime,
                previousReviewTime,
                it.interval,
                "normal",
                answerDuration
            )

            // Update the flashcard after review logic
            updateFlashcardAfterReview(it, quality)
        }
    }

    // Moved the actual update call into a coroutine-based method
    private fun updateFlashcardAfterReview(flashcard: Flashcard, quality: Int) {
        var interval = flashcard.interval
        var repetition = flashcard.repetition
        val currentTime = System.currentTimeMillis()
        val lastReviewTime = flashcard.nextReview - interval * 1000L

        when (quality) {
            0 -> {
                interval = 1
                repetition = 0
            }
            1 -> {
                interval = 10
                repetition = 0
            }
            2 -> {
                interval = 20
                repetition = 0
            }
            3 -> {
                interval = 30
                repetition += 1
            }
            4 -> {
                interval = 1600
                repetition += 1
            }
            5 -> {
                val timeLapsed = (currentTime - lastReviewTime) / 1000
                interval = (timeLapsed * 2 + 3600).toInt()
                repetition += 1
            }
            else -> {
                interval = 30
                repetition = 0
            }
        }

        val nextReview = currentTime + interval * 1000L
        flashcard.interval = interval
        flashcard.nextReview = nextReview
        flashcard.repetition = repetition

        flashcardDAO.updateFlashcardAsync(flashcard) {
            // Once updated, show a toast and move on
            val timePushed = flashcard.nextReview - System.currentTimeMillis()
            val timeDifference = TimeUtils.formatTimeDifference(timePushed)
            Toast.makeText(this, "Next: $timeDifference", Toast.LENGTH_LONG).show()

            if (timePushed > 24 * 60 * 60 * 1000L) {
                questionsMovedCount++
            }
            score += quality

            updateCounters()
            showNextFlashcard()
        }
    }

    private fun updateCounters() {
        // Grab the counts asynchronously
        flashcardDAO.getPastAndFutureQuestionsCountAsync { counts ->
            tvTotalQuestions.text = seenFlashcards.size.toString()
            tvQuestionsMoved.text = questionsMovedCount.toString()
            tvPast.text = counts[0].toString()
            tvFuture.text = counts[1].toString()
        }
    }

    private val editFlashcardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                currentFlashcard?.let {
                    currentFlashcard = flashcardDAO.getFlashcard(it.id)
                }
            }
        }

    private fun openEditQuestion() {
        currentFlashcard?.let {
            val intent = Intent(this, EditFlashcardActivity::class.java)
            intent.putExtra("FLASHCARD_ID", it.id)
            editFlashcardLauncher.launch(intent)
        }
    }

    override fun onDestroy() {
        flashcardDAO.close()
        super.onDestroy()
    }
}
