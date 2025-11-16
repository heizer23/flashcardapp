package com.example.flashcardapp.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button // changes: update
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.FlashcardRoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class SortField { QUESTION, ANSWER, NEXT_REVIEW, INTERVAL }

class ListFlashcardsActivity : AppCompatActivity() {

    private lateinit var flashcardRepository: FlashcardRepository
    private var flashcards: MutableList<Flashcard> = mutableListOf()
    private lateinit var flashcardAdapter: FlashcardAdapter

    private lateinit var tvHeaderQuestion: TextView
    private lateinit var tvHeaderAnswer: TextView
    private lateinit var tvHeaderTime: TextView
    private lateinit var tvHeaderInterval: TextView

    private var currentSortField: SortField = SortField.NEXT_REVIEW
    private var ascending: Boolean = true

    private lateinit var spinnerFilter: Spinner

    // changes: update
    private lateinit var btnGoTopics: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_flashcards)

        spinnerFilter = findViewById(R.id.spinner_filter)
        btnGoTopics = findViewById(R.id.btn_go_topics) // new button

        // Setup spinner items
        val filterOptions = listOf("Future", "Past", "All")
        val adapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapterSpinner
        spinnerFilter.setSelection(0) // default to Future

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> loadFlashcards("future")
                    1 -> loadFlashcards("past")
                    2 -> loadFlashcards("all")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        tvHeaderQuestion = findViewById(R.id.tv_header_question)
        tvHeaderAnswer   = findViewById(R.id.tv_header_answer)
        tvHeaderTime     = findViewById(R.id.tv_header_time)
        tvHeaderInterval = findViewById(R.id.tv_header_interval)

        tvHeaderQuestion.setOnClickListener {
            if (currentSortField == SortField.QUESTION) {
                ascending = !ascending
            } else {
                currentSortField = SortField.QUESTION
                ascending = true
            }
            sortAndRefresh()
        }
        tvHeaderAnswer.setOnClickListener {
            if (currentSortField == SortField.ANSWER) {
                ascending = !ascending
            } else {
                currentSortField = SortField.ANSWER
                ascending = true
            }
            sortAndRefresh()
        }
        tvHeaderTime.setOnClickListener {
            if (currentSortField == SortField.NEXT_REVIEW) {
                ascending = !ascending
            } else {
                currentSortField = SortField.NEXT_REVIEW
                ascending = true
            }
            sortAndRefresh()
        }
        tvHeaderInterval.setOnClickListener {
            if (currentSortField == SortField.INTERVAL) {
                ascending = !ascending
            } else {
                currentSortField = SortField.INTERVAL
                ascending = true
            }
            sortAndRefresh()
        }

        flashcardRepository = FlashcardRepository(
            FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao()
        )

        flashcardAdapter = FlashcardAdapter(this, flashcards)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_flashcards)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = flashcardAdapter

        // changes: update
        btnGoTopics.setOnClickListener {
            val intent = Intent(this, TopicSelectionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-load flashcards upon returning to make sure we see any newly updated selections
        when (spinnerFilter.selectedItemPosition) {
            0 -> loadFlashcards("future")
            1 -> loadFlashcards("past")
            2 -> loadFlashcards("all")
        }
    }

    private fun loadFlashcards(filter: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<Flashcard> = when (filter) {
                "past" -> flashcardRepository.getPastFlashcardsForSelectedTopics(System.currentTimeMillis())
                "future" -> flashcardRepository.getFutureFlashcardsForSelectedTopics(System.currentTimeMillis())
                else -> flashcardRepository.getAllFlashcardsForSelectedTopics()
            }
            withContext(Dispatchers.Main) {
                flashcards.clear()
                flashcards.addAll(list)
                sortAndRefresh()
            }
        }
    }

    private fun sortAndRefresh() {
        when (currentSortField) {
            SortField.QUESTION -> {
                if (ascending) {
                    flashcards.sortBy { it.question }
                } else {
                    flashcards.sortByDescending { it.question }
                }
            }
            SortField.ANSWER -> {
                if (ascending) {
                    flashcards.sortBy { it.answer }
                } else {
                    flashcards.sortByDescending { it.answer }
                }
            }
            SortField.NEXT_REVIEW -> {
                if (ascending) {
                    flashcards.sortBy { it.nextReview }
                } else {
                    flashcards.sortByDescending { it.nextReview }
                }
            }
            SortField.INTERVAL -> {
                if (ascending) {
                    flashcards.sortBy { it.interval }
                } else {
                    flashcards.sortByDescending { it.interval }
                }
            }
        }
        flashcardAdapter.notifyDataSetChanged()
    }
}
