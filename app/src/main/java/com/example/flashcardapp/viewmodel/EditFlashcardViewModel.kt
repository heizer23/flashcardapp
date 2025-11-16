package com.example.flashcardapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFlashcardViewModel(
    private val repository: FlashcardRepository,
    private val flashcardId: Int
) : ViewModel() {

    val flashcardLiveData = MutableLiveData<Flashcard?>()

    init {
        // Immediately load the flashcard + topics
        loadFlashcard()
    }

    private fun loadFlashcard() {
        viewModelScope.launch(Dispatchers.IO) {
            val fc = repository.getFlashcardById(flashcardId)
            if (fc != null) {
                // Also load its topics
                val topicList = repository.getTopicsForFlashcard(fc.id)
                fc.topicNames = topicList.map { it.name }
            }
            withContext(Dispatchers.Main) {
                flashcardLiveData.value = fc
            }
        }
    }

    fun updateFlashcard(
        question: String,
        answer: String,
        searchTerm: String,
        userNote: String,
        topicNames: String,
        onComplete: () -> Unit
    ) {
        val currentFc = flashcardLiveData.value ?: return
        currentFc.question = question
        currentFc.answer = answer
        currentFc.searchTerm = searchTerm
        currentFc.userNote = userNote

        // changes: we now call one repository function for crossRef logic
        viewModelScope.launch(Dispatchers.IO) {
            val topicList = topicNames.split(",")
            repository.updateFlashcardWithTopics(currentFc, topicList)

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun deleteFlashcard(onDeleted: () -> Unit) {
        val currentFc = flashcardLiveData.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFlashcard(currentFc)
            withContext(Dispatchers.Main) {
                onDeleted()
            }
        }
    }
}