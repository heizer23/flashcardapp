package com.example.flashcardapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportFlashcardsViewModel(private val repository: FlashcardRepository) : ViewModel() {

    private val generatedQuestions = MutableLiveData<List<Flashcard>>(arrayListOf())
    fun getGeneratedQuestions(): LiveData<List<Flashcard>> = generatedQuestions

    fun fetchExistingQuestions(callback: OnLoadCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            val list = repository.getAllFlashcards()
            withContext(Dispatchers.Main) {
                callback.onLoaded(list)
            }
        }
    }

    // changes: no local caching of topics; rely on repository
    fun saveFlashcards(flashcards: List<Flashcard>) {
        CoroutineScope(Dispatchers.IO).launch {
            for (f in flashcards) {
                if (f.question.isNotEmpty() && f.answer.isNotEmpty()) {
                    // If new flashcard
                    if (f.id == 0) {
                        val newId = repository.insertFlashcard(f)
                        f.id = newId.toInt()
                    } else {
                        // If existing flashcard, update
                        repository.updateFlashcard(f)
                    }
                    // Now associate flashcard with topics in one call
                    repository.updateFlashcardWithTopics(f, f.topicNames)
                }
            }
        }
    }

    interface OnLoadCallback {
        fun onLoaded(list: List<Flashcard>)
    }
}