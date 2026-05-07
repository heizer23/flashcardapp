package com.example.flashcardapp.main

import android.os.Bundle
import android.widget.Button
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.FlashcardRoomDatabase
import com.example.flashcardapp.viewmodel.ImportFlashcardsViewModel
import com.example.flashcardapp.viewmodel.ImportFlashcardsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GenerateQuestionsActivity : BaseActivity() {

    private lateinit var btnGenerate: Button
    private lateinit var btnSave: Button
    private lateinit var rvGeneratedQuestions: RecyclerView
    private lateinit var adapter: ChatGPTQuestionAdapter
    private lateinit var viewModel: ImportFlashcardsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_questions)

        btnGenerate = findViewById(R.id.btn_generate)
        btnSave = findViewById(R.id.btn_save)
        rvGeneratedQuestions = findViewById(R.id.rv_generated_questions)

        // Create repository & ViewModel via factory
        val repository = FlashcardRepository(
            FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao()
        )
        val factory = ImportFlashcardsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ImportFlashcardsViewModel::class.java]

        adapter = ChatGPTQuestionAdapter(mutableListOf(), null)
        rvGeneratedQuestions.layoutManager = LinearLayoutManager(this)
        rvGeneratedQuestions.adapter = adapter

        viewModel.getGeneratedQuestions().observe(this) { generatedQuestions ->
            adapter.updateData(generatedQuestions)
        }

        btnGenerate.setOnClickListener {
            // Fetch existing Qs, then generate new ones
            viewModel.fetchExistingQuestions(object : ImportFlashcardsViewModel.OnLoadCallback {
                override fun onLoaded(list: List<Flashcard>) {
                    val promptGenerateQuestions = getString(R.string.prompt_generate_questions_activity)

                    // Actually generate new questions
                   /* viewModel.generateQuestions(
                        list,
                        promptGenerateQuestions,
                        this@GenerateQuestionsActivity,
                        object : ImportFlashcardsViewModel.OnGenerateCallback {
                            override fun onSuccess() {
                                runOnUiThread {
                                    adapter.notifyDataSetChanged()
                                    Toast.makeText(
                                        this@GenerateQuestionsActivity,
                                        "Questions generated successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(error: String) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@GenerateQuestionsActivity,
                                        "Error: $error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )*/
                }
            })
        }

        btnSave.setOnClickListener {
            val selectedQuestions = adapter.getSelectedQuestions()
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.saveFlashcards(selectedQuestions)
            }
            Toast.makeText(this, "Selected questions saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
