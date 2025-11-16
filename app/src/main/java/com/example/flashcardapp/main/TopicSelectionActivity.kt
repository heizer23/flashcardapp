package com.example.flashcardapp.main

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.FlashcardRoomDatabase
import com.example.flashcardapp.data.FlashcardRepository
import com.example.flashcardapp.data.TopicWithCount
import com.example.flashcardapp.viewmodel.TopicSelectionViewModel
import com.example.flashcardapp.viewmodel.TopicSelectionViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class TopicSelectionActivity : AppCompatActivity() {

    private lateinit var adapter: TopicSelectionAdapter
    private val flashcardRepository by lazy {
        FlashcardRepository(
            FlashcardRoomDatabase.getDatabase(applicationContext).flashcardDao()
        )
    }

    // We changed the ViewModel to use topicsWithCount.
    private val topicSelectionViewModel: TopicSelectionViewModel by viewModels {
        TopicSelectionViewModelFactory(flashcardRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_selection)

        val rvTopics = findViewById<RecyclerView>(R.id.rv_topics)
        val btnSave = findViewById<Button>(R.id.btn_save)
        // NEW: Button to toggle all selections
        val btnToggleAll = findViewById<Button>(R.id.btn_toggle_all)

        rvTopics.layoutManager = LinearLayoutManager(this)

        // Observe the ViewModel's list of topicsWithCount
        topicSelectionViewModel.topicsWithCount.observe(this) { topicList ->
            // 'topicList' is now a List<TopicWithCount>
            adapter = TopicSelectionAdapter(topicList)
            rvTopics.adapter = adapter
        }

        // Load topics when the Activity starts
        topicSelectionViewModel.loadTopics()

        // Save button: commits changes to DB
        btnSave.setOnClickListener {
            topicSelectionViewModel.updateSelectedTopics(adapter.getSelectedTopics())
            finish()
        }

        // NEW: Toggling all on/off
        btnToggleAll.setOnClickListener {
            if (adapter.areAllSelected()) {
                adapter.deselectAll()
            } else {
                adapter.selectAll()
            }
        }
    }
}
