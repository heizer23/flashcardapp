package com.example.flashcardapp.main

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.FlashcardRoomDatabase
import com.example.flashcardapp.util.FlashcardUtils
import com.example.flashcardapp.viewmodel.ImportFlashcardsViewModel
import com.example.flashcardapp.viewmodel.ImportFlashcardsViewModelFactory

class ImportFlashcardsActivity : AppCompatActivity() {

    private lateinit var etJsonInput: EditText
    private lateinit var btnImport: Button
    private lateinit var btnCopyText: Button
    private lateinit var btnClearText: Button
    private lateinit var viewModel: ImportFlashcardsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_flashcards)

        etJsonInput = findViewById(R.id.et_xml_input)
        btnImport = findViewById(R.id.btn_import)
        btnCopyText = findViewById(R.id.btn_copy_text)
        btnClearText = findViewById(R.id.btn_clear_text)

        // Initialize the ViewModel with a custom factory
        val repository = FlashcardRepository(
            FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao()
        )
        val factory = ImportFlashcardsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ImportFlashcardsViewModel::class.java]

        val promptGenerateQuestions = getString(R.string.prompt_generate_questions)
        var promptExampleExistingQuestions = getString(R.string.prompt_example_existing_questions)
        Log.d("Debug", "Prompt String: $promptExampleExistingQuestions")
        promptExampleExistingQuestions = promptExampleExistingQuestions.replace("&quot;", "\"")

        val prefilledText = "$promptGenerateQuestions\n\n$promptExampleExistingQuestions"
        etJsonInput.setText(prefilledText)

        btnImport.setOnClickListener {
            val jsonInput = etJsonInput.text.toString().trim()
            if (jsonInput.isNotEmpty()) {
                try {
                    val flashcards: List<Flashcard> = FlashcardUtils.parseFlashcardsFromJson(jsonInput)
                    viewModel.saveFlashcards(flashcards)
                    Toast.makeText(
                        this,
                        "Imported ${'$'}{flashcards.size} flashcards!",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Invalid JSON input. Please check the format.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("FlashcardImport", "Error parsing JSON input", e)
                }
            } else {
                Toast.makeText(this, "Please enter JSON input.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCopyText.setOnClickListener {
            val textToCopy = etJsonInput.text.toString().trim()
            if (textToCopy.isNotEmpty()) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Flashcard JSON", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nothing to copy!", Toast.LENGTH_SHORT).show()
            }
        }

        btnClearText.setOnClickListener {
            etJsonInput.setText("")
        }
    }
}
