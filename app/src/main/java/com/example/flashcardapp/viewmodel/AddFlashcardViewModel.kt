package com.example.flashcardapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.data.Flashcard
import com.example.flashcardapp.data.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

    private val _saveComplete = MutableLiveData<Boolean>(false)
    val saveComplete: LiveData<Boolean> get() = _saveComplete

    fun saveFlashcard(question: String, answer: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Create new flashcard
            val flashcard = Flashcard(question, answer)
            // Insert into DB via repository
            repository.insertFlashcard(flashcard)
            _saveComplete.postValue(true)
        }
    }
}
