package com.example.flashcardapp.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.R
import com.example.flashcardapp.data.FlashcardRoomDatabase
import com.example.flashcardapp.viewmodel.EditFlashcardViewModel
import com.example.flashcardapp.viewmodel.EditFlashcardViewModelFactory

class EditFlashcardActivity : BaseActivity() {

    private lateinit var etQuestion: EditText
    private lateinit var etAnswer: EditText
    private lateinit var etTopics: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnContext: Button
    private lateinit var btnRelatedQuestion: Button
    private lateinit var btnCopy: Button
    private lateinit var btnDelete: Button

    private lateinit var editFlashcardViewModel: EditFlashcardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_flashcard)

        etQuestion = findViewById(R.id.et_question)
        etAnswer = findViewById(R.id.et_answer)
        etTopics = findViewById(R.id.et_topics)
        btnUpdate = findViewById(R.id.btn_update)
        btnDelete = findViewById(R.id.btn_delete)
        btnCopy = findViewById(R.id.btn_copy)
        btnContext = findViewById(R.id.btn_context)
        btnRelatedQuestion = findViewById(R.id.btn_generate_related_question)

        // Retrieve flashcardId from intent
        val flashcardId = intent.getIntExtra("FLASHCARD_ID", -1)

        // Build repository & create EditFlashcardViewModel via Factory
        val repository = com.example.flashcardapp.data.FlashcardRepository(
            FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao()
        )
        val factory = EditFlashcardViewModelFactory(repository, flashcardId)
        editFlashcardViewModel = ViewModelProvider(this, factory)[EditFlashcardViewModel::class.java]

        // Observe the current flashcard
        editFlashcardViewModel.flashcardLiveData.observe(this) { fc ->
            if (fc != null) {
                // Populate the fields
                etQuestion.setText(fc.question)
                etAnswer.setText(fc.answer)
                // ADDING THIS LINE TO DISPLAY TOPICS
                etTopics.setText(fc.topicNames.joinToString(", "))
            } else {
                Toast.makeText(this, "No flashcard found.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        btnUpdate.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            val answer = etAnswer.text.toString().trim()
            val topics = etTopics.text.toString().trim()

            if (question.isNotEmpty() && answer.isNotEmpty()) {
                editFlashcardViewModel.updateFlashcard(
                    question,
                    answer,
                    topics
                ) {
                    Toast.makeText(
                        this,
                        "Flashcard updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } else {
                Toast.makeText(this, "Please enter both question and answer.", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            editFlashcardViewModel.deleteFlashcard {
                Toast.makeText(
                    this,
                    "Flashcard deleted.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        btnCopy.setOnClickListener { 
            val answerContent = etAnswer.text.toString()
            if (answerContent.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Answer", answerContent)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Answer copied to clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Answer is empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnContext.setOnClickListener {
            val question = " \"" + etQuestion.text.toString().trim() + "\" with answer: \"" +
                    etAnswer.text.toString().trim() + "\"."
            if (question.isNotEmpty()) {
                ChatGPTHelper.getContextForQuestion(question, this, object : ChatGPTHelper.OnChatGPTResponse {
                    override fun onSuccess(response: String) {
                        runOnUiThread {
                            etAnswer.setText(etAnswer.text.toString() + "\n\n" + response)
                        }
                    }

                    override fun onFailure(error: String) {
                        Log.e("EditFlashcardActivity", "Failed to get context: $error")
                        runOnUiThread {
                            Toast.makeText(
                                this@EditFlashcardActivity,
                                "Failed to get context",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            } else {
                Toast.makeText(
                    this,
                    "Please enter a question first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnRelatedQuestion.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                ChatGPTHelper.generateRelatedQuestion(question, object : ChatGPTHelper.OnChatGPTResponse {
                    override fun onSuccess(response: String) {
                        val intent = Intent(this@EditFlashcardActivity, EditFlashcardActivity::class.java)
                        intent.putExtra("relatedQuestion", response)
                        startActivity(intent)
                    }

                    override fun onFailure(error: String) {
                        Log.e("EditFlashcardActivity", "Failed to generate related question: $error")
                        runOnUiThread {
                            Toast.makeText(
                                this@EditFlashcardActivity,
                                "Failed to generate related question",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, this)
            } else {
                Toast.makeText(
                    this,
                    "Please enter a question first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
