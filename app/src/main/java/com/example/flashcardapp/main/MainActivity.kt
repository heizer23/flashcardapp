package com.example.flashcardapp.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddFlashcard: Button
    private lateinit var btnReviewFlashcards: Button
    private lateinit var btnListFlashcards: Button
    private lateinit var btnImportExport: Button
    private lateinit var btnCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddFlashcard = findViewById(R.id.btn_action)
        btnReviewFlashcards = findViewById(R.id.btn_review_flashcards)
        btnListFlashcards = findViewById(R.id.btn_list_flashcards)
        btnImportExport = findViewById(R.id.btn_import_export)
        btnCreate = findViewById(R.id.btn_create)

        btnAddFlashcard.setOnClickListener {
            val intent = Intent(this, TopicSelectionActivity::class.java)
            startActivity(intent)
        }

        btnCreate.setOnClickListener {
            val intent = Intent(this, AddFlashcardActivity::class.java)
            startActivity(intent)
        }

        btnReviewFlashcards.setOnClickListener {
            val intent = Intent(this, ReviewFlashcardsActivity::class.java)
            startActivity(intent)
        }

        btnListFlashcards.setOnClickListener {
            val intent = Intent(this, ListFlashcardsActivity::class.java)
            startActivity(intent)
        }

        btnImportExport.setOnClickListener {
            val intent = Intent(this, ImportFlashcardsActivity::class.java)
            startActivity(intent)
        }
    }
}
