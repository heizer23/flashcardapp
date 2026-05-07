package com.example.flashcardapp.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.R
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.FlashcardRoomDatabase
import com.example.flashcardapp.viewmodel.AddFlashcardViewModel
import com.example.flashcardapp.viewmodel.AddFlashcardViewModelFactory

class AddFlashcardActivity : BaseActivity() {

    private lateinit var etQuestion: EditText
    private lateinit var etAnswer: EditText
    private lateinit var btnSave: Button

    private lateinit var addFlashcardViewModel: AddFlashcardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_flashcard)

        etQuestion = findViewById(R.id.et_question)
        etAnswer = findViewById(R.id.et_answer)
        btnSave = findViewById(R.id.btn_save)

        // Build repository
        val repository = FlashcardRepository(
            FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao()
        )
        // Create ViewModel using a custom Factory
        addFlashcardViewModel = ViewModelProvider(
            this,
            AddFlashcardViewModelFactory(repository)
        )[AddFlashcardViewModel::class.java]

        // Observe when saving is complete
        addFlashcardViewModel.saveComplete.observe(this) { isComplete ->
            if (isComplete == true) {
                Toast.makeText(this, "Flashcard saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnSave.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            val answer = etAnswer.text.toString().trim()
            if (question.isNotEmpty() && answer.isNotEmpty()) {
                addFlashcardViewModel.saveFlashcard(question, answer)
            } else {
                Toast.makeText(
                    this,
                    "Please enter both question and answer.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
