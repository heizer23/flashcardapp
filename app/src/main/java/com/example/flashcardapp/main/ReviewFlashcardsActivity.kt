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
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.R
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.FlashcardRoomDatabase
import com.example.flashcardapp.viewmodel.ReviewFlashcardsViewModel

class ReviewFlashcardsActivity : AppCompatActivity() {

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

    private lateinit var reviewViewModel: ReviewFlashcardsViewModel

    private val editFlashcardLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_flashcards)

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

        val repo = FlashcardRepository(FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao())

        reviewViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReviewFlashcardsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ReviewFlashcardsViewModel(repo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        })[ReviewFlashcardsViewModel::class.java]

        reviewViewModel.currentFlashcard.observe(this) { fc ->
            if (fc == null) {
                Toast.makeText(this, "No flashcards due for review!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                tvAnswer.visibility = View.GONE
                btnShowAnswer.visibility = View.VISIBLE
                tvQuestion.text = fc.question

                findViewById<View>(R.id.low_confidence_buttons).visibility = View.GONE
                findViewById<View>(R.id.high_confidence_buttons).visibility = View.GONE
            }
        }

        reviewViewModel.lastInterval.observe(this) { interval ->
            val formattedInterval = TimeUtils.formatInterval(interval)
            Toast.makeText(this, "Next review in: $formattedInterval", Toast.LENGTH_LONG).show()
        }

        reviewViewModel.totalFlashcardsForSelectedTopics.observe(this) { count ->
            reviewViewModel.todaysReviewedCount.observe(this) { seenCount ->
                tvTotalQuestions.text = "$seenCount / $count"
            }
        }

        reviewViewModel.questionsMovedCount.observe(this) { count ->
            tvQuestionsMoved.text = count.toString()
        }

        reviewViewModel.pastCount.observe(this) { count ->
            tvPast.text = count.toString()
        }

        reviewViewModel.futureCount.observe(this) { count ->
            tvFuture.text = count.toString()
        }

        reviewViewModel.score.observe(this) { newScore ->
        }

        reviewViewModel.fetchNextFlashcard()

        tvQuestion.setOnClickListener { openEditQuestion() }

        btnShowAnswer.setOnClickListener {
            tvAnswer.visibility = View.VISIBLE
            tvAnswer.text = reviewViewModel.currentFlashcard.value?.answer
            btnShowAnswer.visibility = View.GONE

            findViewById<View>(R.id.low_confidence_buttons).visibility = View.VISIBLE
            findViewById<View>(R.id.high_confidence_buttons).visibility = View.VISIBLE

            reviewViewModel.onShowAnswer()
        }

        btnForgot.setOnClickListener { reviewViewModel.handleConfidence(0) }
        btnStruggling.setOnClickListener { reviewViewModel.handleConfidence(1) }
        btnUnsure.setOnClickListener { reviewViewModel.handleConfidence(2) }
        btnOkay.setOnClickListener { reviewViewModel.handleConfidence(3) }
        btnGood.setOnClickListener { reviewViewModel.handleConfidence(4) }
        btnPerfect.setOnClickListener { reviewViewModel.handleConfidence(5) }
    }

    private fun openEditQuestion() {
        val currentFc = reviewViewModel.currentFlashcard.value ?: return
        val intent = Intent(this, EditFlashcardActivity::class.java)
        intent.putExtra("FLASHCARD_ID", currentFc.id)
        editFlashcardLauncher.launch(intent)
    }
}
